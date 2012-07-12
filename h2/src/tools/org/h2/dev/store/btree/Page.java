/*
 * Copyright 2004-2011 H2 Group. Multiple-Licensed under the H2 License,
 * Version 1.0, and under the Eclipse Public License, Version 1.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.dev.store.btree;

import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * A btree page implementation.
 */
class Page {

    private static final int MAX_SIZE = 20;

    private final BtreeMap<?, ?> map;
    private long id;
    private long storedId;
    private long transaction;
    private Object[] keys;
    private Object[] values;
    private long[] children;

    private Page(BtreeMap<?, ?> map) {
        this.map = map;
    }

    /**
     * Create a new page.
     *
     * @param map the map
     * @param keys the keys
     * @param values the values
     * @param children the children
     * @return the page
     */
    static Page create(BtreeMap<?, ?> map, Object[] keys, Object[] values, long[] children) {
        Page p = new Page(map);
        p.keys = keys;
        p.values = values;
        p.children = children;
        p.transaction = map.getTransaction();
        p.id = map.registerTempPage(p);
        return p;
    }

    /**
     * Read a page.
     *
     * @param map the map
     * @param id the page id
     * @param buff the source buffer
     * @return the page
     */
    static Page read(BtreeMap<?, ?> map, long id, ByteBuffer buff) {
        Page p = new Page(map);
        p.id = p.storedId = id;
        p.read(buff);
        return p;
    }

    private Page copyOnWrite() {
        // TODO avoid creating objects (arrays) that are then not used
        // possibly add shortcut for copy with add / copy with remove
        long t = map.getTransaction();
        if (transaction == t) {
            return this;
        }
        map.removePage(id);
        Page p2 = create(map, keys, values, children);
        p2.transaction = t;
        return p2;
    }

    public String toString() {
        StringBuilder buff = new StringBuilder();
        buff.append("nodeId: ").append(id).append("\n");
        for (int i = 0; i <= keys.length; i++) {
            if (i > 0) {
                buff.append(" ");
            }
            if (children != null) {
                buff.append("[" + children[i] + "] ");
            }
            if (i < keys.length) {
                buff.append(keys[i]);
                if (values != null) {
                    buff.append(':');
                    buff.append(values[i]);
                }
            }
        }
        return buff.toString();
    }

    /**
     * Get the page id.
     *
     * @return the page id
     */
    long getId() {
        return id;
    }

    /**
     * Get the value for the given key, or null if not found.
     *
     * @param key the key
     * @return the value or null
     */
    Object find(Object key) {
        int x = findKey(key);
        if (children != null) {
            if (x < 0) {
                x = -x - 1;
            } else {
                x++;
            }
            Page p = map.readPage(children[x]);
            return p.find(key);
        }
        if (x >= 0) {
            return values[x];
        }
        return null;
    }

    /**
     * Get the value for the given key, or null if not found.
     *
     * @param key the key
     * @return the page or null
     */
    Page findPage(Object key) {
        int x = findKey(key);
        if (children != null) {
            if (x < 0) {
                x = -x - 1;
            } else {
                x++;
            }
            Page p = map.readPage(children[x]);
            return p.findPage(key);
        }
        if (x >= 0) {
            return this;
        }
        return null;
    }

    private int findKey(Object key) {
        int low = 0, high = keys.length - 1;
        while (low <= high) {
            int x = (low + high) >>> 1;
            int compare = map.compare(key, keys[x]);
            if (compare > 0) {
                low = x + 1;
            } else if (compare < 0) {
                high = x - 1;
            } else {
                return x;
            }
        }
        return -(low + 1);
    }

    /**
     * Go to the first element for the given key.
     *
     * @param p the current page
     * @param parents the stack of parent page positions
     * @param key the key
     */
    static void min(Page p, ArrayList<CursorPos> parents, Object key) {
        while (p != null) {
            int x = key == null ? 0 : p.findKey(key);
            if (p.children != null) {
                if (x < 0) {
                    x = -x - 1;
                } else {
                    x++;
                }
                CursorPos c = new CursorPos();
                c.page = p;
                c.index = x;
                parents.add(c);
                p = p.map.readPage(p.children[x]);
            } else {
                if (x < 0) {
                    x = -x - 1;
                }
                CursorPos c = new CursorPos();
                c.page = p;
                c.index = x;
                parents.add(c);
                return;
            }
        }
    }

    /**
     * Get the next key.
     *
     * @param parents the stack of parent page positions
     * @return the next key
     */
    public static Object nextKey(ArrayList<CursorPos> parents) {
        if (parents.size() == 0) {
            return null;
        }
        while (true) {
            // TODO avoid remove/add pairs if possible
            CursorPos p = parents.remove(parents.size() - 1);
            int index = p.index++;
            if (index < p.page.keys.length) {
                parents.add(p);
                return p.page.keys[index];
            }
            while (true) {
                if (parents.size() == 0) {
                    return null;
                }
                p = parents.remove(parents.size() - 1);
                index = p.index++;
                if (index < p.page.children.length) {
                    parents.add(p);
                    Page x = p.page;
                    x = x.map.readPage(x.children[index]);
                    min(x, parents, null);
                    break;
                }
            }
        }
    }

    private int keyCount() {
        return keys.length;
    }

    private boolean isLeaf() {
        return children == null;
    }

    private Page splitLeaf(int at) {
        int a = at, b = keys.length - a;
        Object[] aKeys = new Object[a];
        Object[] bKeys = new Object[b];
        System.arraycopy(keys, 0, aKeys, 0, a);
        System.arraycopy(keys, a, bKeys, 0, b);
        keys = aKeys;
        Object[] aValues = new Object[a];
        Object[] bValues = new Object[b];
        bValues = new Object[b];
        System.arraycopy(values, 0, aValues, 0, a);
        System.arraycopy(values, a, bValues, 0, b);
        values = aValues;
        Page newPage = create(map, bKeys, bValues, null);
        return newPage;
    }

    private Page splitNode(int at) {
        int a = at, b = keys.length - a;
        Object[] aKeys = new Object[a];
        Object[] bKeys = new Object[b - 1];
        System.arraycopy(keys, 0, aKeys, 0, a);
        System.arraycopy(keys, a + 1, bKeys, 0, b - 1);
        keys = aKeys;
        long[] aChildren = new long[a + 1];
        long[] bChildren = new long[b];
        System.arraycopy(children, 0, aChildren, 0, a + 1);
        System.arraycopy(children, a + 1, bChildren, 0, b);
        children = aChildren;
        Page newPage = create(map, bKeys, null, bChildren);
        return newPage;
    }

    /**
     * Add or replace the key-value pair.
     *
     * @param map the map
     * @param p the page
     * @param key the key
     * @param value the value
     * @return the root page
     */
    static Page put(BtreeMap<?, ?> map, Page p, Object key, Object value) {
        if (p == null) {
            Object[] keys = { key };
            Object[] values = { value };
            p = create(map, keys, values, null);
            return p;
        }
        p = p.copyOnWrite();
        Page top = p;
        Page parent = null;
        int parentIndex = 0;
        while (true) {
            if (parent != null) {
                parent.children[parentIndex] = p.id;
            }
            if (!p.isLeaf()) {
                if (p.keyCount() >= MAX_SIZE) {
                    // TODO almost duplicate code
                    int pos = p.keyCount() / 2;
                    Object k = p.keys[pos];
                    Page split = p.splitNode(pos);
                    if (parent == null) {
                        Object[] keys = { k };
                        long[] children = { p.getId(), split.getId() };
                        top = create(map, keys, null, children);
                        p = top;
                    } else {
                        parent.insert(parentIndex, k, null, split.getId());
                        p = parent;
                    }
                }
            }
            int index = p.findKey(key);
            if (p.isLeaf()) {
                if (index >= 0) {
                    // create a copy
                    // TODO might not be required, but needs a "modified" flag
                    Object[] v2 = new Object[p.values.length];
                    System.arraycopy(p.values, 0, v2, 0, v2.length);
                    p.values = v2;
                    p.values[index] = value;
                    break;
                }
                index = -index - 1;
                p.insert(index, key, value, 0);
                if (p.keyCount() >= MAX_SIZE) {
                    int pos = p.keyCount() / 2;
                    Object k = p.keys[pos];
                    Page split = p.splitLeaf(pos);
                    if (parent == null) {
                        Object[] keys = { k };
                        long[] children = { p.getId(), split.getId() };
                        top = create(map, keys, null, children);
                    } else {
                        parent.insert(parentIndex, k, null, split.getId());
                    }
                }
                break;
            }
            if (index < 0) {
                index = -index - 1;
            }
            parent = p;
            parentIndex = index;
            p = map.readPage(p.children[index]);
            p = p.copyOnWrite();
        }
        return top;
    }

    /**
     * Remove a key-value pair.
     *
     * @param p the root node
     * @param key the key
     * @return the new root node
     */
    static Page remove(Page p, Object key) {
        int index = p.findKey(key);
        if (p.isLeaf()) {
            if (index >= 0) {
                if (p.keyCount() == 1) {
                    return null;
                }
                p = p.copyOnWrite();
                p.remove(index);
            } else {
                // not found
            }
            return p;
        }
        // node
        if (index < 0) {
            index = -index - 1;
        }
        Page c = p.map.readPage(p.children[index]);
        Page c2 = remove(c, key);
        if (c2 == c) {
            // not found
        } else if (c2 == null) {
            // child was deleted
            p = p.copyOnWrite();
            p.remove(index);
            if (p.keyCount() == 0) {
                p = c2;
            }
        } else {
            p = p.copyOnWrite();
            p.children[index] = c2.id;
        }
        return p;
    }

    private void insert(int index, Object key, Object value, long child) {
        Object[] newKeys = new Object[keys.length + 1];
        copyWithGap(keys, newKeys, keys.length, index);
        newKeys[index] = key;
        keys = newKeys;
        if (values != null) {
            Object[] newValues = new Object[values.length + 1];
            copyWithGap(values, newValues, values.length, index);
            newValues[index] = value;
            values = newValues;
        }
        if (children != null) {
            long[] newChildren = new long[children.length + 1];
            copyWithGap(children, newChildren, children.length, index + 1);
            newChildren[index + 1] = child;
            children = newChildren;
        }
    }

    private void remove(int index) {
        Object[] newKeys = new Object[keys.length - 1];
        copyExcept(keys, newKeys, keys.length, index);
        keys = newKeys;
        if (values != null) {
            Object[] newValues = new Object[values.length - 1];
            copyExcept(values, newValues, values.length, index);
            values = newValues;
        }
        if (children != null) {
            long[] newChildren = new long[children.length - 1];
            copyExcept(children, newChildren, children.length, index);
            children = newChildren;
        }
    }

    private void read(ByteBuffer buff) {
        boolean node = buff.get() == 1;
        if (node) {
            int len = DataUtils.readVarInt(buff);
            children = new long[len];
            keys = new Object[len - 1];
            for (int i = 0; i < len; i++) {
                children[i] = buff.getLong();
                if (i < keys.length) {
                    keys[i] = map.getKeyType().read(buff);
                }
            }
        } else {
            int len = DataUtils.readVarInt(buff);
            keys = new Object[len];
            values = new Object[len];
            for (int i = 0; i < len; i++) {
                keys[i] = map.getKeyType().read(buff);
                values[i] = map.getValueType().read(buff);
            }
        }
    }

    /**
     * Store the page.
     *
     * @param buff the target buffer
     */
    void write(ByteBuffer buff) {
        if (children != null) {
            buff.put((byte) 1);
            int len = children.length;
            DataUtils.writeVarInt(buff, len);
            for (int i = 0; i < len; i++) {
                long c = map.readPage(children[i]).storedId;
                buff.putLong(c);
                if (i < keys.length) {
                    map.getKeyType().write(buff, keys[i]);
                }
            }
        } else {
            buff.put((byte) 0);
            int len = keys.length;
            DataUtils.writeVarInt(buff, len);
            for (int i = 0; i < len; i++) {
                map.getKeyType().write(buff, keys[i]);
                map.getValueType().write(buff, values[i]);
            }
        }
    }

    /**
     * Get the length in bytes, including temporary children.
     *
     * @return the length
     */
    int lengthIncludingTempChildren() {
        int byteCount = length();
        if (children != null) {
            int len = children.length;
            for (int i = 0; i < len; i++) {
                long c = children[i];
                if (c < 0) {
                    byteCount += map.readPage(c).lengthIncludingTempChildren();
                }
            }
        }
        return byteCount;
    }

    /**
     * Update the page ids recursively.
     *
     * @param pageId the new page id
     * @return the next page id
     */
    long updatePageIds(long pageId) {
        this.storedId = pageId;
        pageId += length();
        if (children != null) {
            int len = children.length;
            for (int i = 0; i < len; i++) {
                long c = children[i];
                if (c < 0) {
                    pageId = map.readPage(c).updatePageIds(pageId);
                }
            }
        }
        return pageId;
    }

    /**
     * Store this page.
     *
     * @param buff the target buffer
     * @return the page id
     */
    long storeTemp(ByteBuffer buff) {
        write(buff);
        if (children != null) {
            int len = children.length;
            for (int i = 0; i < len; i++) {
                long c = children[i];
                if (c < 0) {
                    children[i] = map.readPage(c).storeTemp(buff);
                }
            }
        }
        this.id = storedId;
        return id;
    }

    /**
     * Count the temporary pages recursively.
     *
     * @return the number of temporary pages
     */
    int countTemp() {
        int count = 1;
        if (children != null) {
            int size = children.length;
            for (int i = 0; i < size; i++) {
                long c = children[i];
                if (c < 0) {
                    count += map.readPage(c).countTemp();
                }
            }
        }
        return count;
    }

    /**
     * Get the length in bytes.
     *
     * @return the length
     */
    int length() {
        int byteCount = 1;
        if (children != null) {
            int len = children.length;
            byteCount += DataUtils.getVarIntLen(len);
            for (int i = 0; i < len; i++) {
                byteCount += 8;
                if (i < keys.length) {
                    byteCount += map.getKeyType().length(keys[i]);
                }
            }
        } else {
            int len = keys.length;
            byteCount += DataUtils.getVarIntLen(len);
            for (int i = 0; i < len; i++) {
                byteCount += map.getKeyType().length(keys[i]);
                byteCount += map.getValueType().length(values[i]);
            }
        }
        return byteCount;
    }

    private static void copyWithGap(Object src, Object dst, int oldSize, int gapIndex) {
        if (gapIndex > 0) {
            System.arraycopy(src, 0, dst, 0, gapIndex);
        }
        if (gapIndex < oldSize) {
            System.arraycopy(src, gapIndex, dst, gapIndex + 1, oldSize - gapIndex);
        }
    }

    private static void copyExcept(Object src, Object dst, int oldSize, int removeIndex) {
        if (removeIndex > 0 && oldSize > 0) {
            System.arraycopy(src, 0, dst, 0, removeIndex);
        }
        if (removeIndex < oldSize) {
            System.arraycopy(src, removeIndex + 1, dst, removeIndex, oldSize - removeIndex - 1);
        }
    }

}
