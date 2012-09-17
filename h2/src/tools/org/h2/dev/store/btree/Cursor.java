/*
 * Copyright 2004-2011 H2 Group. Multiple-Licensed under the H2 License,
 * Version 1.0, and under the Eclipse Public License, Version 1.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.dev.store.btree;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * A cursor to iterate over elements in ascending order.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public class Cursor<K, V> implements Iterator<K> {

    protected final MVMap<K, V> map;
    protected final Page root;
    protected final K from;
    protected ArrayList<CursorPos> parents;
    protected CursorPos currentPos;
    protected K current;

    Cursor(MVMap<K, V> map, Page root, K from) {
        this.map = map;
        this.root = root;
        this.from = from;
    }

    public K next() {
        if (!hasNext()) {
            return null;
        }
        K c = current;
        if (c != null) {
            fetchNext();
        }
        return c == null ? null : c;
    }

    /**
     * Fetch the next key.
     */
    @SuppressWarnings("unchecked")
    protected void fetchNext() {
        current = (K) map.nextKey(currentPos, this);
    }

    public boolean hasNext() {
        if (parents == null) {
            // not initialized yet: fetch the first key
            parents = new ArrayList<CursorPos>();
            currentPos = min(root, from);
            if (currentPos != null) {
                fetchNext();
            }
        }
        return current != null;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    /**
     * Add a cursor position to the stack.
     *
     * @param p the cursor position
     */
    public void push(CursorPos p) {
        parents.add(p);
    }

    /**
     * Remove the latest cursor position from the stack and return it.
     *
     * @return the cursor position, or null if none
     */
    public CursorPos pop() {
        int size = parents.size();
        return size == 0 ? null : parents.remove(size - 1);
    }

    /**
     * Visit  the first key that is greater or equal the given key.
     *
     * @param p the page
     * @param from the key, or null
     * @return the cursor position
     */
    public CursorPos min(Page p, K from) {
        return map.min(p, this, from);
    }

    /**
     * Visit the first key within this child page.
     *
     * @param p the page
     * @param childIndex the child index
     * @return the cursor position
     */
    public CursorPos visitChild(Page p, int childIndex) {
        p = p.getChildPage(childIndex);
        currentPos = min(p, null);
        return currentPos;
    }

}

