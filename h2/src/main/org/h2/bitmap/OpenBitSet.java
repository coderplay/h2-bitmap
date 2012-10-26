/*
 * Copyright 2004-2011 H2 Group. Multiple-Licensed under the H2 License,
 * Version 1.0, and under the Eclipse Public License, Version 1.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */

package org.h2.bitmap;

import java.util.Arrays;

/**
 * Thos class is copied from Lucene and modified a little.
 * An "open" BitSet implementation that allows direct access to the array of
 * words storing the bits.
 * <p/>
 * Unlike java.util.bitset, the fact that bits are packed into an array of longs
 * is part of the interface. This allows efficient implementation of other
 * algorithms by someone other than the author. It also allows one to
 * efficiently implement alternate serialization or interchange formats.
 * <p/>
 * <code>OpenBitSet</code> is faster than <code>java.util.BitSet</code> in most
 * operations and *much* faster at calculating cardinality of sets and results
 * of set operations. It can also handle sets of larger cardinality (up to 64 *
 * 2**32-1)
 * <p/>
 * The goals of <code>OpenBitSet</code> are the fastest implementation possible,
 * and maximum code reuse. Extra safety and encapsulation may always be built on
 * top, but if that's built in, the cost can never be removed (and hence people
 * re-implement their own version in order to get better performance). If you
 * want a "safe", totally encapsulated (and slower and limited) BitSet class,
 * use <code>java.util.BitSet</code>.
 * <p/>
 * <h3>Performance Results</h3>
 * 
 * Test system: Pentium 4, Sun Java 1.5_06 -server -Xbatch -Xmx64M <br/>
 * BitSet size = 1,000,000 <br/>
 * Results are java.util.BitSet time divided by OpenBitSet time.
 * <table border="1">
 * <tr>
 * <th></th>
 * <th>cardinality</th>
 * <th>intersect_count</th>
 * <th>union</th>
 * <th>nextSetBit</th>
 * <th>get</th>
 * <th>iterator</th>
 * </tr>
 * <tr>
 * <th>50% full</th>
 * <td>3.36</td>
 * <td>3.96</td>
 * <td>1.44</td>
 * <td>1.46</td>
 * <td>1.99</td>
 * <td>1.58</td>
 * </tr>
 * <tr>
 * <th>1% full</th>
 * <td>3.31</td>
 * <td>3.90</td>
 * <td>&nbsp;</td>
 * <td>1.04</td>
 * <td>&nbsp;</td>
 * <td>0.99</td>
 * </tr>
 * </table>
 * <br/>
 * Test system: AMD Opteron, 64 bit linux, Sun Java 1.5_06 -server -Xbatch
 * -Xmx64M <br/>
 * BitSet size = 1,000,000 <br/>
 * Results are java.util.BitSet time divided by OpenBitSet time.
 * <table border="1">
 * <tr>
 * <th></th>
 * <th>cardinality</th>
 * <th>intersect_count</th>
 * <th>union</th>
 * <th>nextSetBit</th>
 * <th>get</th>
 * <th>iterator</th>
 * </tr>
 * <tr>
 * <th>50% full</th>
 * <td>2.50</td>
 * <td>3.50</td>
 * <td>1.00</td>
 * <td>1.03</td>
 * <td>1.12</td>
 * <td>1.25</td>
 * </tr>
 * <tr>
 * <th>1% full</th>
 * <td>2.51</td>
 * <td>3.49</td>
 * <td>&nbsp;</td>
 * <td>1.00</td>
 * <td>&nbsp;</td>
 * <td>1.02</td>
 * </tr>
 * </table>
 */
public class OpenBitSet implements BitSet, Cloneable {
    /*
     * BitSets are packed into arrays of "words." Currently a word is a long,
     * which consists of 64 bits, requiring 6 address bits. The choice of word
     * size is determined purely by performance concerns.
     */
    protected final static int ADDRESS_BITS_PER_WORD = 6;
    protected final static int BITS_PER_WORD = 1 << ADDRESS_BITS_PER_WORD;
    protected final static int BIT_INDEX_MASK = BITS_PER_WORD - 1;

    protected long[] words;
    protected int wlen; // number of words (elements) used in the array

    // Used only for assert:
    private long numBits;

    /**
     * Constructs an LuceneBitSet large enough to hold <code>numBits</code>.
     */
    public OpenBitSet(int nbits) {
        this.numBits = nbits;
        this.words = new long[wordIndex(nbits - 1) + 1];
        this.wlen = words.length;
    }

    public OpenBitSet() {
        this(BITS_PER_WORD);
    }

    /**
     * Constructs an OpenBitSet from an existing long[]. <br/>
     * The first 64 bits are in long[0], with bit index 0 at the least
     * significant bit, and bit index 63 at the most significant. Given a bit
     * index, the word containing it is long[index/64], and it is at bit number
     * index%64 within that word.
     * <p>
     * numWords are the number of elements in the array that contain set bits
     * (non-zero longs). numWords should be &lt= bits.length, and any existing
     * words in the array at position &gt= numWords should be zero.
     * 
     */
    public OpenBitSet(long[] bits) {
        this.words = bits;
        this.wlen = words.length;
        this.numBits = wlen << ADDRESS_BITS_PER_WORD;
    }
  
    // @Override
    // public DocIdSetIterator iterator() {
    // return new OpenBitSetIterator(bits, wlen);
    // }
  
    /** Expert: returns the long[] storing the bits */
    public long[] getBits() {
        return words;
    }

    /** Expert: sets a new long[] to use as the bit storage */
    public void setBits(long[] bits) {
        this.words = bits;
    }

    /** Expert: gets the number of longs in the array that are in use */
    public int getNumWords() {
        return wlen;
    }

    /** Expert: sets the number of longs in the array that are in use */
    public void setNumWords(int nWords) {
        this.wlen = nWords;
    }

    /**
     * Returns the current capacity in bits (1 greater than the index of the
     * last bit)
     */
    public int capacity() {
        return words.length << ADDRESS_BITS_PER_WORD;
    }

    @Override
    public int size() {
        return capacity();
    }

    @Override
    public boolean isEmpty() {
        return cardinality() == 0;
    }

    /**
     * Returns true or false for the specified bit index. The index should be
     * less than the OpenBitSet size
     */
    @Override
    public boolean get(int index) {
        assert index >= 0 && index < numBits;
        int i = wordIndex(index); // div 64
        // signed shift will keep a negative index and force an
        // array-index-out-of-bounds-exception, removing the need for an
        // explicit check.
        int bit = index & 0x3f; // mod 64
        long bitmask = 1L << bit;
        return (words[i] & bitmask) != 0;
    }

    /**
     * Sets the bit at the specified index. The index should be less than the
     * OpenBitSet size.
     */
    @Override
    public void set(int index) {
        assert index >= 0 && index < numBits;
        int wordNum = wordIndex(index); // div 64
        int bit = index & 0x3f; // mod 64
        long bitmask = 1L << bit;
        words[wordNum] |= bitmask;
    }

    /**
     * Sets a range of bits, expanding the set size if necessary
     * 
     * @param startIndex
     *            lower index
     * @param endIndex
     *            one-past the last bit to set
     */
    @Override
    public void set(int startIndex, int endIndex) {
        if (endIndex <= startIndex)
            return;

        int startWord = (int) wordIndex(startIndex);

        // since endIndex is one past the end, this is index of the last
        // word to be changed.
        int endWord = expandingWordNum(endIndex - 1);

        long startmask = -1L << startIndex;
        // 64-(endIndex&0x3f) is the same as -endIndex due to wrap
        long endmask = -1L >>> -endIndex; 

        if (startWord == endWord) {
            words[startWord] |= (startmask & endmask);
            return;
        }

        words[startWord] |= startmask;
        Arrays.fill(words, startWord + 1, endWord, -1L);
        words[endWord] |= endmask;
    }

    protected int expandingWordNum(int index) {
        int wordNum = wordIndex(index);
        if (wordNum >= wlen) {
            ensureCapacity(index + 1);
            wlen = wordNum + 1;
        }
        assert (numBits = Math.max(numBits, index + 1)) >= 0;
        return wordNum;
    }

    @Override
    public int cardinality() {
        return BitUtil.pop_array(words, 0, wlen);
    }

    @Override
    public void clear() {
        while (wlen > 0)
            words[--wlen] = 0;
    }

    /**
     * Sets the bit specified by the index to {@code false}.
     * 
     * @param bitIndex
     *            the index of the bit to be cleared
     * @throws IndexOutOfBoundsException
     *             if the specified index is negative
     * 
     * The index should be less than the OpenBitSet size.
     */
    @Override
    public void clear(int index) {
        assert index >= 0 && index < numBits;
        int wordNum = wordIndex(index);
        int bit = index & 0x03f;
        long bitmask = 1L << bit;
        words[wordNum] &= ~bitmask;
    }

    @Override
    public void clear(int startIndex, int endIndex) {
        if (endIndex <= startIndex)
            return;

        int startWord = wordIndex(startIndex);
        if (startWord >= wlen)
            return;

        // since endIndex is one past the end, this is index of the last
        // word to be changed.
        int endWord = wordIndex(endIndex - 1);

        long startmask = -1L << startIndex;
        // // 64-(endIndex&0x3f) is the same as -endIndex due to wrap
        long endmask = -1L >>> -endIndex; 

        // invert masks since we are clearing
        startmask = ~startmask;
        endmask = ~endmask;

        if (startWord == endWord) {
            words[startWord] &= (startmask | endmask);
            return;
        }

        words[startWord] &= startmask;

        int middle = Math.min(wlen, endWord);
        Arrays.fill(words, startWord + 1, middle, 0L);
        if (endWord < wlen) {
            words[endWord] &= endmask;
        }
    }

    /**
     * Sets a bit and returns the previous value. The index should be less than
     * the OpenBitSet size.
     */
    public boolean getAndSet(int index) {
        assert index >= 0 && index < numBits;
        int wordNum = wordIndex(index); // div 64
        int bit = index & 0x3f; // mod 64
        long bitmask = 1L << bit;
        boolean val = (words[wordNum] & bitmask) != 0;
        words[wordNum] |= bitmask;
        return val;
    }

    /**
     * flips a bit. The index should be less than the OpenBitSet size.
     */
    @Override
    public void flip(int index) {
        assert index >= 0 && index < numBits;
        int wordNum = wordIndex(index); // div 64
        int bit = index & 0x3f; // mod 64
        long bitmask = 1L << bit;
        words[wordNum] ^= bitmask;
    }

    /**
     * Flips a range of bits, expanding the set size if necessary
     * 
     * @param startIndex
     *            lower index
     * @param endIndex
     *            one-past the last bit to flip
     */
    @Override
    public void flip(int startIndex, int endIndex) {
        if (endIndex <= startIndex)
            return;
        int startWord = (int) wordIndex(startIndex);

        // since endIndex is one past the end, this is index of the last
        // word to be changed.
        int endWord = expandingWordNum(endIndex - 1);

        /*** Grrr, java shifting wraps around so -1L>>>64 == -1
         * for that reason, make sure not to use endmask if the bits to flip will
         * be zero in the last word (redefine endWord to be the last changed...)
        long startmask = -1L << (startIndex & 0x3f);     // example: 11111...111000
        long endmask = -1L >>> (64-(endIndex & 0x3f));   // example: 00111...111111
        ***/

        long startmask = -1L << startIndex;
        // 64-(endIndex&0x3f) is the same as -endIndex due to wrap
        long endmask = -1L >>> -endIndex; 
                                         

        if (startWord == endWord) {
            words[startWord] ^= (startmask & endmask);
            return;
        }

        words[startWord] ^= startmask;

        for (int i = startWord + 1; i < endWord; i++) {
            words[i] = ~words[i];
        }

        words[endWord] ^= endmask;
    }


    @Override
    public void and(BitSet o) {
        OpenBitSet other = (OpenBitSet) o;
        int newLen = Math.min(this.wlen, other.wlen);
        long[] thisArr = this.words;
        long[] otherArr = other.words;
        // testing against zero can be more efficient
        int pos = newLen;
        while (--pos >= 0) {
            thisArr[pos] &= otherArr[pos];
        }
        if (this.wlen > newLen) {
            // fill zeros from the new shorter length to the old length
            Arrays.fill(words, newLen, this.wlen, 0);
        }
        this.wlen = newLen;
    }

    @Override
    public int andCardinality(BitSet other) {
        OpenBitSet o = (OpenBitSet) other;
        return BitUtil.pop_intersect(this.words, o.words, 0,
                Math.min(this.wlen, o.wlen));
    }
  
    @Override
    public void or(BitSet o) {
        OpenBitSet other = (OpenBitSet) o;
        int newLen = Math.max(wlen, other.wlen);
        ensureCapacity(newLen);
        assert (numBits = Math.max(other.numBits, numBits)) >= 0;

        long[] thisArr = this.words;
        long[] otherArr = other.words;
        int pos = Math.min(wlen, other.wlen);
        while (--pos >= 0) {
            thisArr[pos] |= otherArr[pos];
        }
        if (this.wlen < newLen) {
            System.arraycopy(otherArr, this.wlen, thisArr, this.wlen, newLen
                    - this.wlen);
        }
        this.wlen = newLen;
    }

    @Override
    public int orCardinality(BitSet other) {
        OpenBitSet o = (OpenBitSet) other;
        int tot = BitUtil.pop_union(this.words, o.words, 0,
                Math.min(this.wlen, o.wlen));
        if (this.wlen < o.wlen) {
            tot += BitUtil.pop_array(o.words, this.wlen, o.wlen - this.wlen);
        } else if (this.wlen > o.wlen) {
            tot += BitUtil.pop_array(this.words, o.wlen, this.wlen - o.wlen);
        }
        return tot;
    }

  @Override
  public void andNot(BitSet other) {
    OpenBitSet o = (OpenBitSet) other;
    int idx = Math.min(wlen,o.wlen);
    long[] thisArr = this.words;
    long[] otherArr = o.words;
    while(--idx>=0) {
      thisArr[idx] &= ~otherArr[idx];
    }
  }

    @Override
    public int andNotCardinality(BitSet other) {
        OpenBitSet o = (OpenBitSet) other;
        int tot = BitUtil.pop_andnot(this.words, o.words, 0,
                Math.min(this.wlen, o.wlen));
        if (this.wlen > o.wlen) {
            tot += BitUtil.pop_array(this.words, o.wlen, this.wlen - o.wlen);
        }
        return tot;
    }

    @Override
    public void xor(BitSet other) {
        OpenBitSet o = (OpenBitSet) other;
        int newLen = Math.max(wlen, o.wlen);
        ensureCapacity(newLen);
        assert (numBits = Math.max(o.numBits, numBits)) >= 0;

        long[] thisArr = this.words;
        long[] otherArr = o.words;
        int pos = Math.min(wlen, o.wlen);
        while (--pos >= 0) {
            thisArr[pos] ^= otherArr[pos];
        }
        if (this.wlen < newLen) {
            System.arraycopy(otherArr, this.wlen, thisArr, this.wlen, newLen
                    - this.wlen);
        }
        this.wlen = newLen;
    }

    @Override
    public int xorCardinality(BitSet other) {
        OpenBitSet o = (OpenBitSet) other;
        int tot = BitUtil.pop_xor(this.words, o.words, 0,
                Math.min(this.wlen, o.wlen));
        if (this.wlen < o.wlen) {
            tot += BitUtil.pop_array(o.words, this.wlen, o.wlen - this.wlen);
        } else if (this.wlen > o.wlen) {
            tot += BitUtil.pop_array(this.words, o.wlen, this.wlen - o.wlen);
        }
        return tot;
    }

    @Override
    public boolean intersects(BitSet o) {
        OpenBitSet other = (OpenBitSet) o;
        int pos = Math.min(this.wlen, other.wlen);
        long[] thisArr = this.words;
        long[] otherArr = other.words;
        while (--pos >= 0) {
            if ((thisArr[pos] & otherArr[pos]) != 0)
                return true;
        }
        return false;
    }

    /**
     * Ensures that the BitSet can hold enough words.
     * 
     * @param wordsRequired
     *            the minimum acceptable number of words.
     */
    private void ensureCapacity(int wordsRequired) {
        if (words.length < wordsRequired) {
            // Allocate larger of doubled size or required size
            int request = Math.max(2 * words.length, wordsRequired);
            words = Arrays.copyOf(words, request);
        }
    }

    /**
     * Lowers numWords, the number of words in use, by checking for trailing
     * zero words.
     */
    public void trimTrailingZeros() {
        int idx = wlen - 1;
        while (idx >= 0 && words[idx] == 0)
            idx--;
        wlen = idx + 1;
    }

    /** returns the number of 64 bit words it would take to hold numBits */
    public static int bits2words(int numBits) {
        return (int) (wordIndex(numBits - 1) + 1);
    }

    /**
     * Given a bit index, return word index containing it.
     */
    private static int wordIndex(int bitIndex) {
        return bitIndex >> ADDRESS_BITS_PER_WORD;
    }

    /** returns true if both sets have the same bits set */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof OpenBitSet))
            return false;
        OpenBitSet a;
        OpenBitSet b = (OpenBitSet) o;
        // make a the larger set.
        if (b.wlen > this.wlen) {
            a = b;
            b = this;
        } else {
            a = this;
        }

        // check for any set bits out of the range of b
        for (int i = a.wlen - 1; i >= b.wlen; i--) {
            if (a.words[i] != 0)
                return false;
        }

        for (int i = b.wlen - 1; i >= 0; i--) {
            if (a.words[i] != b.words[i])
                return false;
        }

        return true;
    }


    @Override
    public int hashCode() {
        // Start with a zero hash and use a mix that results in zero if the
        // input is zero.
        // This effectively truncates trailing zeros without an explicit check.
        long h = 0;
        for (int i = words.length; --i >= 0;) {
            h ^= words[i];
            h = (h << 1) | (h >>> 63); // rotate left
        }
        // fold leftmost bits into right and add a constant to prevent
        // empty sets from returning 0, which is too common.
        return (int) ((h >> 32) ^ h) + 0x98761234;
    }


    /**
     * Returns the index of the first set bit starting at the index specified.
     * -1 is returned if there are no more set bits.
     */
    @Override
    public int nextSetBit(int index) {
        int i = wordIndex(index);
        if (i >= wlen)
            return -1;
        int subIndex = index & 0x3f; // index within the word
        long word = words[i] >> subIndex; // skip all the bits to the right of
                                          // index

        if (word != 0) {
            return (i << ADDRESS_BITS_PER_WORD) + subIndex + BitUtil.ntz(word);
        }

        while (++i < wlen) {
            word = words[i];
            if (word != 0)
                return (i << ADDRESS_BITS_PER_WORD) + BitUtil.ntz(word);
        }

        return -1;
    }

    @Override
    public int nextClearBit(int fromIndex) {
//         // Neither spec nor implementation handle bitsets of maximal length.
//         // See 4816253.
//         if (fromIndex < 0)
//         throw new IndexOutOfBoundsException("fromIndex < 0: " + fromIndex);
//        
//         checkInvariants();
//        
//         int u = wordIndex(fromIndex);
//         if (u >= wordsInUse)
//         return fromIndex;
//        
//         long word = ~words[u] & (WORD_MASK << fromIndex);
//        
//         while (true) {
//         if (word != 0)
//         return (u * BITS_PER_WORD) + Long.numberOfTrailingZeros(word);
//         if (++u == wordsInUse)
//         return wordsInUse * BITS_PER_WORD;
//         word = ~words[u];
//         }
        return 0;
    }

    @Override
    public OpenBitSet clone() {
        try {
            OpenBitSet obs = (OpenBitSet) super.clone();
            // hopefully an array clone is as fast(er) than arraycopy
            obs.words = obs.words.clone();
            return obs;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

}


