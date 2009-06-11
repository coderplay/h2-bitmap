/*
 * Copyright 2004-2009 H2 Group. Multiple-Licensed under the H2 License,
 * Version 1.0, and under the Eclipse Public License, Version 1.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.store;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import org.h2.message.Message;
import org.h2.message.Trace;
import org.h2.util.IntArray;

/**
 * An output stream that writes into a page store.
 */
public class PageOutputStream extends OutputStream {

    private PageStore store;
    private final Trace trace;
    private int trunkPageId;
    private int trunkNext;
    private IntArray reservedPages = new IntArray();
    private PageStreamTrunk trunk;
    private PageStreamData data;
    private int reserved;
    private int remaining;
    private byte[] buffer = new byte[1];
    private boolean needFlush;
    private boolean writing;

    /**
     * Create a new page output stream.
     *
     * @param store the page store
     * @param trunkPage the first trunk page (already allocated)
     */
    public PageOutputStream(PageStore store, int trunkPage) {
        this.trace = store.getTrace();
        this.store = store;
        this.trunkPageId = trunkPage;
    }

    /**
     * Allocate the required pages so that no pages need to be allocated while
     * writing.
     *
     * @param minBuffer the number of bytes to allocate
     */
    void prepareWriting(int minBuffer) throws SQLException {
        if (reserved < minBuffer) {
            int pageSize = store.getPageSize();
            int capacityPerPage = PageStreamData.getCapacity(pageSize);
            int pages = PageStreamTrunk.getPagesAddressed(pageSize);
            // allocate x data pages
            int pagesToAllocate = pages;
            // the first trunk page is already allocated
            if (reservedPages.size() == 0) {
                reservedPages.add(trunkPageId);
            }
            int totalCapacity = pages * capacityPerPage;
            while (totalCapacity < minBuffer) {
                pagesToAllocate += pagesToAllocate;
                totalCapacity += totalCapacity;
            }
            // allocate the next trunk page as well
            pagesToAllocate++;
            for (int i = 0; i < pagesToAllocate; i++) {
                int page = store.allocatePage();
                reservedPages.add(page);
            }
        }
    }

    public void write(int b) throws IOException {
        buffer[0] = (byte) b;
        write(buffer);
    }

    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    private void initNextData() {
        int nextData = trunk == null ? -1 : trunk.getNextPage();
        if (nextData < 0) {
            int parent = trunkPageId;
            if (trunkNext == 0) {
                trunkPageId = reservedPages.get(0);
                reservedPages.remove(0);
            } else {
                trunkPageId = trunkNext;
            }
            int len = PageStreamTrunk.getPagesAddressed(store.getPageSize());
            int[] pageIds = new int[len];
            for (int i = 0; i < len; i++) {
                pageIds[i] = reservedPages.get(i);
            }
            trunkNext = reservedPages.get(len);
            trunk = new PageStreamTrunk(store, parent, trunkPageId, trunkNext, pageIds);
            reservedPages.removeRange(0, len + 1);
        }
        data = new PageStreamData(store, trunk.getNextPage(), trunk.getPos());
        data.initWrite();
    }

    public void write(byte[] b, int off, int len) throws IOException {
        if (len <= 0) {
            return;
        }
        if (writing) {
            Message.throwInternalError("writing while still writing");
        }
        writing = true;
        try {
            while (len >= 0) {
                int l = data.write(b, off, len);
                if (l <= len) {
                    data.write(null);
                    initNextData();
                }
                reserved -= l;
                off += l;
                len -= l;
            }
            needFlush = true;
            remaining -= len;
        } catch (SQLException e) {
            throw Message.convertToIOException(e);
        } finally {
            writing = false;
        }
    }

    private void storePage() throws IOException {
        try {
            if (trace.isDebugEnabled()) {
                trace.debug("pageOut.storePage " + data.getPos());
            }
            data.write(null);
        } catch (SQLException e) {
            throw Message.convertToIOException(e);
        }
    }

    public void flush() throws IOException {
        if (needFlush) {
            storePage();
            needFlush = false;
        }
    }

    public void close() throws IOException {
        flush();
        store = null;
    }

}
