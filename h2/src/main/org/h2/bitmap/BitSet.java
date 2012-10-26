/*
 * Copyright 2004-2011 H2 Group. Multiple-Licensed under the H2 License,
 * Version 1.0, and under the Eclipse Public License, Version 1.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.bitmap;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Min Zhou (coderplay@gmail.com)
 */
public interface BitSet {

    /**
     * Returns the number of bits of space actually in use by this {@code BitSet}
     * to represent bit values. The maximum element in the set is the size - 1st
     * element.
     * 
     * @return the number of bits currently in this bit set
     */
    public int size();
  
    /**
     * Returns true if this {@code BitSet} contains no bits that are set to
     * {@code true}.
     * 
     * @return boolean indicating whether this {@code BitSet} is empty
     */
    public boolean isEmpty();
  
    /**
     * Returns the value of the bit with the specified index. The value is
     * {@code true} if the bit with the index {@code bitIndex} is currently set in
     * this {@code BitSet}; otherwise, the result is {@code false}.
     * 
     * @param bitIndex the bit index
     * @return the value of the bit with the specified index
     * @throws IndexOutOfBoundsException if the specified index is negative
     */
    public boolean get(int bitIndex);
  
  //  /**
  //   * Returns a new {@code BitSet} composed of bits from this {@code BitSet} from
  //   * {@code fromIndex} (inclusive) to {@code toIndex} (exclusive).
  //   * 
  //   * @param fromIndex index of the first bit to include
  //   * @param toIndex index after the last bit to include
  //   * @return a new {@code BitSet} from a range of this {@code BitSet}
  //   * @throws IndexOutOfBoundsException if {@code fromIndex} is negative, or
  //   *           {@code toIndex} is negative, or {@code fromIndex} is larger than
  //   *           {@code toIndex}
  //   */
  //  public BitSet get(int fromIndex, int toIndex);
  
    /**
     * Sets the bit at the specified index to {@code true}.
     * 
     * @param bitIndex a bit index
     * @throws IndexOutOfBoundsException if the specified index is negative
     */
    public void set(int bitIndex);
  
  //  /**
  //   * Sets the bit at the specified index to the specified value.
  //   * 
  //   * @param bitIndex a bit index
  //   * @param value a boolean value to set
  //   * @throws IndexOutOfBoundsException if the specified index is negative
  //   */
  //  public void set(int bitIndex, boolean value);
  
    /**
     * Sets the bits from the specified {@code fromIndex} (inclusive) to the
     * specified {@code toIndex} (exclusive) to {@code true}.
     * 
     * @param fromIndex index of the first bit to be set
     * @param toIndex index after the last bit to be set
     * @throws IndexOutOfBoundsException if {@code fromIndex} is negative, or
     *           {@code toIndex} is negative, or {@code fromIndex} is larger than
     *           {@code toIndex}
     */
    public void set(int fromIndex, int toIndex);
  
  //  /**
  //   * Sets the bits from the specified {@code fromIndex} (inclusive) to the
  //   * specified {@code toIndex} (exclusive) to the specified value.
  //   * 
  //   * @param fromIndex index of the first bit to be set
  //   * @param toIndex index after the last bit to be set
  //   * @param value value to set the selected bits to
  //   * @throws IndexOutOfBoundsException if {@code fromIndex} is negative, or
  //   *           {@code toIndex} is negative, or {@code fromIndex} is larger than
  //   *           {@code toIndex}
  //   */
  //  public void set(int fromIndex, int toIndex, boolean value);
  
    /**
     * Returns the number of bits set to {@code true} in this {@code BitSet}.
     * 
     * @return the number of bits set to {@code true} in this {@code BitSet}
     */
    public int cardinality();
  
    /**
     * Sets the bit specified by the index to {@code false}.
     * 
     * @param bitIndex the index of the bit to be cleared
     * @throws IndexOutOfBoundsException if the specified index is negative
     */
    public void clear(int bitIndex);
  
    /**
     * Sets the bits from the specified {@code fromIndex} (inclusive) to the
     * specified {@code toIndex} (exclusive) to {@code false}.
     * 
     * @param fromIndex index of the first bit to be cleared
     * @param toIndex index after the last bit to be cleared
     * @throws IndexOutOfBoundsException if {@code fromIndex} is negative, or
     *           {@code toIndex} is negative, or {@code fromIndex} is larger than
     *           {@code toIndex}
     */
    public void clear(int fromIndex, int toIndex);
  
    /**
     * Sets all of the bits in this BitSet to {@code false}.
     */
    public void clear();
  
    /**
     * Sets the bit at the specified index to the complement of its current value.
     * 
     * @param bitIndex the index of the bit to flip
     * @throws IndexOutOfBoundsException if the specified index is negative
     */
    public void flip(int bitIndex);
  
    /**
     * Sets each bit from the specified {@code fromIndex} (inclusive) to the
     * specified {@code toIndex} (exclusive) to the complement of its current
     * value.
     * 
     * @param fromIndex index of the first bit to flip
     * @param toIndex index after the last bit to flip
     * @throws IndexOutOfBoundsException if {@code fromIndex} is negative, or
     *           {@code toIndex} is negative, or {@code fromIndex} is larger than
     *           {@code toIndex}
     */
    public void flip(int fromIndex, int toIndex);
  
    /**
     * Returns true if the specified {@code BitSet} has any bits set to
     * {@code true} that are also set to {@code true} in this {@code BitSet}.
     * 
     * @param set {@code BitSet} to intersect with
     * @return boolean indicating whether this {@code BitSet} intersects the
     *         specified {@code BitSet}
     */
    public boolean intersects(BitSet set);
  
    /**
     * Performs a logical <b>AND</b> of this target bit set with the argument bit
     * set. This bit set is modified so that each bit in it has the value
     * {@code true} if and only if it both initially had the value {@code true}
     * and the corresponding bit in the bit set argument also had the value
     * {@code true}.
     * 
     * @param set a bit set
     */
    public void and(BitSet set);
  
    /**
     * Returns the cardinality of the result of a bitwise <b>AND</b> of the values
     * of the current {@code BitSet} with some other {@code BitSet}. Avoids
     * needing to allocate an intermediate {@code BitSet} to hold the result of
     * this operation.
     * 
     * @param set {@code BitSet} to <b>AND</b> with
     * 
     */
    public int andCardinality(BitSet set);
  
    /**
     * Performs a logical <b>OR</b> of this bit set with the bit set argument.
     * This bit set is modified so that a bit in it has the value {@code true} if
     * and only if it either already had the value {@code true} or the
     * corresponding bit in the bit set argument has the value {@code true}.
     * 
     * @param set a bit set
     */
    public void or(BitSet set);
  
    /**
     * Returns the cardinality of the result of a bitwise <b>OR</b> of the values
     * of the current {@code BitSet} with some other {@code BitSet}. Avoids
     * needing to allocate an intermediate {@code BitSet} to hold the result of
     * this operation.
     * 
     * @param set {@code BitSet} to <b>OR</b> with
     * 
     */
    public int orCardinality(BitSet set);
  
    /**
     * Performs a logical <b>XOR</b> of this bit set with the bit set argument.
     * This bit set is modified so that a bit in it has the value {@code true} if
     * and only if one of the following statements holds:
     * <ul>
     * <li>The bit initially has the value {@code true}, and the corresponding bit
     * in the argument has the value {@code false}.
     * <li>The bit initially has the value {@code false}, and the corresponding
     * bit in the argument has the value {@code true}.
     * </ul>
     * 
     * @param set a bit set
     */
    public void xor(BitSet set);
  
    /**
     * Returns the cardinality of the result of a bitwise <b>XOR</b> of the values
     * of the current {@code BitSet} with some other {@code BitSet}. Avoids
     * needing to allocate an intermediate {@code BitSet} to hold the result of
     * this operation.
     * 
     * @param set {@code BitSet} to <b>XOR</b> with
     * 
     */
    public int xorCardinality(BitSet set);
  
    /**
     * Clears all of the bits in this {@code BitSet} whose corresponding bit is
     * set in the specified {@code BitSet}.
     * 
     * @param set the {@code BitSet} with which to mask this {@code BitSet}
     */
    public void andNot(BitSet set);
  
    /**
     * Returns the cardinality of the result of a bitwise andnot of the values of
     * the current {@code BitSet} with some other {@code BitSet}. Avoids needing
     * to allocate an intermediate {@code BitSet} to hold the result of this
     * operation.
     * 
     * @param set {@code BitSet} to andnot with
     * 
     */
    public int andNotCardinality(BitSet set);
  
    
    
    /**
     * Returns the index of the first bit that is set to {@code true}
     * that occurs on or after the specified starting index. If no such
     * bit exists then {@code -1} is returned.
     *
     * <p>To iterate over the {@code true} bits in a {@code BitSet},
     * use the following loop:
     *
     *  <pre> {@code
     * for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i+1)) {
     *     // operate on index i here
     * }}</pre>
     *
     * @param  fromIndex the index to start checking from (inclusive)
     * @return the index of the next set bit, or {@code -1} if there
     *         is no such bit
     * @throws IndexOutOfBoundsException if the specified index is negative
     */
    public int nextSetBit(int fromIndex);
  
    /**
     * Returns the index of the first bit that is set to {@code false}
     * that occurs on or after the specified starting index.
     *
     * @param  fromIndex the index to start checking from (inclusive)
     * @return the index of the next clear bit
     * @throws IndexOutOfBoundsException if the specified index is negative
     */
    public int nextClearBit(int fromIndex);
    
    
  //  /**
  //   * Save the state of the {@code BitSet} instance to a stream (i.e., serialize
  //   * it).
  //   */
  //  public void serialize(OutputStream s) throws IOException;
  //  
  //  
  //  public void deserialize(InputStream s) throws IOException;
  
    // hashCode
    // equals

    /**
     * Cloning this {@code BitSet} produces a new {@code BitSet}
     * that is equal to it.
     * The clone of the bit set is another bit set that has exactly the
     * same bits set to {@code true} as this bit set.
     *
     * @return a clone of this bit set
     */
    public BitSet clone();
}
  