/*
 * Copyright 2004-2009 H2 Group. Multiple-Licensed under the H2 License,
 * Version 1.0, and under the Eclipse Public License, Version 1.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.index;

import java.sql.SQLException;
import org.h2.constant.SysProperties;
import org.h2.message.Message;
import org.h2.result.SearchRow;
import org.h2.store.Data;
import org.h2.store.DataPage;
import org.h2.store.PageStore;
import org.h2.util.MemoryUtils;

/**
 * A b-tree node page that contains index data.
 * Data is organized as follows: [leaf 0] (largest value of leaf 0) [leaf 1]
 * Format:
 * <ul><li>0-3: parent page id
 * </li><li>4-4: page type
 * </li><li>5-6: entry count
 * </li><li>7-10: row count of all children (-1 if not known)
 * </li><li>11-14: rightmost child page id
 * </li><li>15- entries: 4 bytes leaf page id, 4 bytes offset to data
 * </li></ul>
 */
class PageBtreeNode extends PageBtree {

    private static final int CHILD_OFFSET_PAIR_LENGTH = 8;
    private static final int CHILD_OFFSET_PAIR_START = 15;

    /**
     * The page ids of the children.
     */
    private int[] childPageIds;

//    private int rowCountStored = UNKNOWN_ROWCOUNT;

    private int rowCount = UNKNOWN_ROWCOUNT;

    PageBtreeNode(PageBtreeIndex index, int pageId, int parentPageId, Data data) {
        super(index, pageId, parentPageId, data);
        start = CHILD_OFFSET_PAIR_START;
    }

    void read() {
        data.setPos(4);
        int type = data.readByte();
        onlyPosition = (type & Page.FLAG_LAST) == 0;
        entryCount = data.readShortInt();
        rowCount = data.readInt();
        if (!PageStore.STORE_BTREE_ROWCOUNT) {
            rowCount = UNKNOWN_ROWCOUNT;
        }
        childPageIds = new int[entryCount + 1];
        childPageIds[entryCount] = data.readInt();
        rows = PageStore.newSearchRows(entryCount);
        offsets = MemoryUtils.newInts(entryCount);
        for (int i = 0; i < entryCount; i++) {
            childPageIds[i] = data.readInt();
            offsets[i] = data.readInt();
        }
        check();
        start = data.length();
    }

    private int addChildTry(SearchRow row) throws SQLException {
        if (entryCount < 2) {
            return 0;
        }
        int rowLength = index.getRowSize(data, row, onlyPosition);
        int pageSize = index.getPageStore().getPageSize();
        int last = entryCount == 0 ? pageSize : offsets[entryCount - 1];
        if (last - rowLength < start + CHILD_OFFSET_PAIR_LENGTH) {
            return (entryCount / 2) + 1;
        }
        return 0;
    }

    /**
     * Add a row. If it is possible this method returns 0, otherwise
     * the split point. It is always possible to add one row.
     *
     * @param row the now to add
     * @return the split point of this page, or 0 if no split is required
     */
    private void addChild(int x, int childPageId, SearchRow row) throws SQLException {
        int rowLength = index.getRowSize(data, row, onlyPosition);
        int pageSize = index.getPageStore().getPageSize();
        int last = entryCount == 0 ? pageSize : offsets[entryCount - 1];
        if (last - rowLength < start + CHILD_OFFSET_PAIR_LENGTH) {
            onlyPosition = true;
            // change the offsets (now storing only positions)
            int o = pageSize;
            for (int i = 0; i < entryCount; i++) {
                o -= index.getRowSize(data, getRow(i), onlyPosition);
                offsets[i] = o;
            }
            last = entryCount == 0 ? pageSize : offsets[entryCount - 1];
            rowLength = index.getRowSize(data, row, onlyPosition);
            if (SysProperties.CHECK && last - rowLength < start + CHILD_OFFSET_PAIR_LENGTH) {
                throw Message.throwInternalError();
            }
        }
        int offset = last - rowLength;
        int[] newOffsets = new int[entryCount + 1];
        SearchRow[] newRows = new SearchRow[entryCount + 1];
        int[] newChildPageIds = new int[entryCount + 2];
        if (childPageIds != null) {
            System.arraycopy(childPageIds, 0, newChildPageIds, 0, x + 1);
        }
        if (entryCount > 0) {
            System.arraycopy(offsets, 0, newOffsets, 0, x);
            System.arraycopy(rows, 0, newRows, 0, x);
            if (x < entryCount) {
                for (int j = x; j < entryCount; j++) {
                    newOffsets[j + 1] = offsets[j] - rowLength;
                }
                offset = (x == 0 ? pageSize : offsets[x - 1]) - rowLength;
                System.arraycopy(rows, x, newRows, x + 1, entryCount - x);
                System.arraycopy(childPageIds, x + 1, newChildPageIds, x + 2, entryCount - x);
            }
        }
        newOffsets[x] = offset;
        newRows[x] = row;
        newChildPageIds[x + 1] = childPageId;
        start += CHILD_OFFSET_PAIR_LENGTH;
        offsets = newOffsets;
        rows = newRows;
        childPageIds = newChildPageIds;
        entryCount++;
    }

    int addRowTry(SearchRow row) throws SQLException {
        while (true) {
            int x = find(row, false, false);
            PageBtree page = index.getPage(childPageIds[x]);
            int splitPoint = page.addRowTry(row);
            if (splitPoint == 0) {
                break;
            }
            SearchRow pivot = page.getRow(splitPoint - 1);
            int splitPoint2 = addChildTry(pivot);
            if (splitPoint2 != 0) {
                return splitPoint2;
            }
            PageBtree page2 = page.split(splitPoint);
            readAllRows();
            addChild(x, page2.getPos(), pivot);
            index.getPageStore().updateRecord(page, true, page.data);
            index.getPageStore().updateRecord(page2, true, page2.data);
            index.getPageStore().updateRecord(this, true, data);
        }
        updateRowCount(1);
        written = false;
        return 0;
    }

    private void updateRowCount(int offset) {
        if (PageStore.STORE_BTREE_ROWCOUNT) {
            rowCount += offset;
        }
    }

    PageBtree split(int splitPoint) throws SQLException {
        int newPageId = index.getPageStore().allocatePage();
        PageBtreeNode p2 = new PageBtreeNode(index, newPageId, parentPageId, index.getPageStore().createData());
        if (onlyPosition) {
            // TODO optimize: maybe not required
            p2.onlyPosition = true;
        }
        int firstChild = childPageIds[splitPoint];
        readAllRows();
        for (int i = splitPoint; i < entryCount;) {
            p2.addChild(p2.entryCount, childPageIds[splitPoint + 1], rows[splitPoint]);
            removeChild(splitPoint);
        }
        int lastChild = childPageIds[splitPoint - 1];
        removeChild(splitPoint - 1);
        childPageIds[splitPoint - 1] = lastChild;
        if (p2.childPageIds == null) {
            p2.childPageIds = new int[1];
        }
        p2.childPageIds[0] = firstChild;
        p2.remapChildren();
        return p2;
    }

    protected void remapChildren() throws SQLException {
        for (int child : childPageIds) {
            PageBtree p = index.getPage(child);
            p.setParentPageId(getPos());
            index.getPageStore().updateRecord(p, true, p.data);
        }
    }

    /**
     * Initialize the page.
     *
     * @param page1 the first child page
     * @param pivot the pivot key
     * @param page2 the last child page
     */
    void init(PageBtree page1, SearchRow pivot, PageBtree page2) throws SQLException {
        entryCount = 0;
        childPageIds = new int[] { page1.getPos() };
        rows = new SearchRow[0];
        offsets = MemoryUtils.EMPTY_INTS;
        addChild(0, page2.getPos(), pivot);
        rowCount = page1.getRowCount() + page2.getRowCount();
        check();
    }

    void find(PageBtreeCursor cursor, SearchRow first, boolean bigger) throws SQLException {
        int i = find(first, bigger, false);
        if (i > entryCount) {
            if (parentPageId == Page.ROOT) {
                return;
            }
            PageBtreeNode next = (PageBtreeNode) index.getPage(parentPageId);
            next.find(cursor, first, bigger);
            return;
        }
        PageBtree page = index.getPage(childPageIds[i]);
        page.find(cursor, first, bigger);
    }

    void last(PageBtreeCursor cursor) throws SQLException {
        int child = childPageIds[entryCount];
        index.getPage(child).last(cursor);
    }

    PageBtreeLeaf getFirstLeaf() throws SQLException {
        int child = childPageIds[0];
        return index.getPage(child).getFirstLeaf();
    }

    PageBtreeLeaf getLastLeaf() throws SQLException {
        int child = childPageIds[entryCount - 1];
        return index.getPage(child).getLastLeaf();
    }

    boolean remove(SearchRow row) throws SQLException {
        int at = find(row, false, false);
        // merge is not implemented to allow concurrent usage
        // TODO maybe implement merge
        PageBtree page = index.getPage(childPageIds[at]);
        boolean empty = page.remove(row);
        updateRowCount(-1);
        if (!empty) {
            // the first row didn't change - nothing to do
            return false;
        }
        // this child is now empty
        index.getPageStore().freePage(page.getPos(), true, page.data);
        if (entryCount < 1) {
            // no more children - this page is empty as well
            return true;
        }
        removeChild(at);
        index.getPageStore().updateRecord(this, true, data);
        return false;
    }

    int getRowCount() throws SQLException {
        if (rowCount == UNKNOWN_ROWCOUNT) {
            int count = 0;
            for (int child : childPageIds) {
                PageBtree page = index.getPage(child);
                count += page.getRowCount();
            }
            rowCount = count;
        }
        return rowCount;
    }

    void setRowCountStored(int rowCount) {
        this.rowCount = rowCount;
    }

    private void check() {
        for (int child : childPageIds) {
            if (child == 0) {
                Message.throwInternalError();
            }
        }
    }

    public int getByteCount(DataPage dummy) {
        return index.getPageStore().getPageSize();
    }

    public void write(DataPage buff) throws SQLException {
        check();
        write();
        index.getPageStore().writePage(getPos(), data);
    }

    private void write() throws SQLException {
        if (written) {
            return;
        }
        readAllRows();
        data.reset();
        data.writeInt(parentPageId);
        data.writeByte((byte) (Page.TYPE_BTREE_NODE | (onlyPosition ? 0 : Page.FLAG_LAST)));
        data.writeShortInt(entryCount);
        data.writeInt(rowCount);
        data.writeInt(childPageIds[entryCount]);
        for (int i = 0; i < entryCount; i++) {
            data.writeInt(childPageIds[i]);
            data.writeInt(offsets[i]);
        }
        for (int i = 0; i < entryCount; i++) {
            index.writeRow(data, offsets[i], rows[i], onlyPosition);
        }
        written = true;
    }

    void freeChildren() throws SQLException {
        for (int i = 0; i <= entryCount; i++) {
            int childPageId = childPageIds[i];
            PageBtree child = index.getPage(childPageId);
            index.getPageStore().freePage(childPageId, false, null);
            child.freeChildren();
        }
    }

    private void removeChild(int i) throws SQLException {
        readAllRows();
        entryCount--;
        written = false;
        if (entryCount < 0) {
            Message.throwInternalError();
        }
        SearchRow[] newRows = PageStore.newSearchRows(entryCount);
        int[] newOffsets = MemoryUtils.newInts(entryCount);
        int[] newChildPageIds = new int[entryCount + 1];
        System.arraycopy(offsets, 0, newOffsets, 0, Math.min(entryCount, i));
        System.arraycopy(rows, 0, newRows, 0, Math.min(entryCount, i));
        System.arraycopy(childPageIds, 0, newChildPageIds, 0, i);
        if (entryCount > i) {
            System.arraycopy(rows, i + 1, newRows, i, entryCount - i);
            int startNext = i > 0 ? offsets[i - 1] : index.getPageStore().getPageSize();
            int rowLength = startNext - offsets[i];
            for (int j = i; j < entryCount; j++) {
                newOffsets[j] = offsets[j + 1] + rowLength;
            }
        }
        System.arraycopy(childPageIds, i + 1, newChildPageIds, i, entryCount - i + 1);
        offsets = newOffsets;
        rows = newRows;
        childPageIds = newChildPageIds;
        start -= CHILD_OFFSET_PAIR_LENGTH;
    }

    /**
     * Set the cursor to the first row of the next page.
     *
     * @param cursor the cursor
     * @param ROW the current row
     */
    void nextPage(PageBtreeCursor cursor, int pageId) throws SQLException {
        int i;
        // TODO maybe keep the index in the child page (transiently)
        for (i = 0; i < childPageIds.length; i++) {
            if (childPageIds[i] == pageId) {
                i++;
                break;
            }
        }
        if (i > entryCount) {
            if (parentPageId == Page.ROOT) {
                cursor.setCurrent(null, 0);
                return;
            }
            PageBtreeNode next = (PageBtreeNode) index.getPage(parentPageId);
            next.nextPage(cursor, getPos());
            return;
        }
        PageBtree page = index.getPage(childPageIds[i]);
        PageBtreeLeaf leaf = page.getFirstLeaf();
        cursor.setCurrent(leaf, 0);
    }

    /**
     * Set the cursor to the last row of the previous page.
     *
     * @param cursor the cursor
     * @param ROW the current row
     */
    void previousPage(PageBtreeCursor cursor, int pageId) throws SQLException {
        int i;
        // TODO maybe keep the index in the child page (transiently)
        for (i = childPageIds.length - 1; i >= 0; i--) {
            if (childPageIds[i] == pageId) {
                i--;
                break;
            }
        }
        if (i < 0) {
            if (parentPageId == Page.ROOT) {
                cursor.setCurrent(null, 0);
                return;
            }
            PageBtreeNode previous = (PageBtreeNode) index.getPage(parentPageId);
            previous.previousPage(cursor, getPos());
            return;
        }
        PageBtree page = index.getPage(childPageIds[i]);
        PageBtreeLeaf leaf = page.getLastLeaf();
        cursor.setCurrent(leaf, leaf.entryCount - 1);
    }


    public String toString() {
        return "page[" + getPos() + "] b-tree node table:" + index.getId() + " entries:" + entryCount;
    }


}