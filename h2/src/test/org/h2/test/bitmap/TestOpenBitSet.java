/*
 * Copyright 2004-2011 H2 Group. Multiple-Licensed under the H2 License,
 * Version 1.0, and under the Eclipse Public License, Version 1.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.test.bitmap;

import java.util.Random;

import org.h2.bitmap.BitSet;
import org.h2.bitmap.OpenBitSet;
import org.h2.test.TestBase;

/**
 * @author Min Zhou (coderplay@gmail.com)
 */
public class TestOpenBitSet extends TestBase {

    private int nums = 1024 * 1024;
    private BitSet bitset;
    private BitSet[] bitmap;
    private int[] values;

    /**
     * Run just this test.
     * 
     * @param a
     *            ignored
     */
    public static void main(String... a) throws Exception {
        TestBase.createCaller().init().test();
    }

    public void test() throws Exception {
        prepareBitmap();
        testAnd();
        testOr();
        testXor();
        testAndNot();
        testSum();
        testRange();
    }

    private void prepareBitmap() {
        Random rand = new Random(System.nanoTime());

        bitset = new OpenBitSet(nums);
        for (int i = 0; i < (nums / 2); i++) {
            bitset.set(rand.nextInt(nums));
        }
        
        bitmap = new OpenBitSet[Integer.SIZE];
        for (int i = 0; i < bitmap.length; i++) {
            bitmap[i] = new OpenBitSet(nums);
        }

        values = new int[nums];


        for (int i = 0; i < nums; i++) {
            values[i] = rand.nextInt(0x7fffffff);
            for (int j = 0; j < bitmap.length; j++) {
                int v = (values[i] >> j) & 0x01;
                if (v == 0x01) {
                    bitmap[j].set(i);
                }
            }
        }
    }

    private void testAnd() {
        int originCard = bitset.cardinality();
        bitset.and(bitset);
        int afterCard = bitset.cardinality();
        assertEquals(originCard, afterCard);

        int card1 = bitset.andCardinality(bitmap[0]);
        bitset.and(bitmap[0]);
        int card2 = bitset.cardinality();
        assertEquals(card1, card2);
    }

    private void testOr() {
        int originCard = bitset.cardinality();
        bitset.or(bitset);
        int afterCard = bitset.cardinality();
        assertEquals(originCard, afterCard);

        int card1 = bitset.orCardinality(bitmap[1]);
        bitset.or(bitmap[1]);
        int card2 = bitset.cardinality();
        assertEquals(card1, card2);
    }

    private void testXor() {
        int card1 = bitset.xorCardinality(bitmap[2]);
        bitset.xor(bitmap[2]);
        int card2 = bitset.cardinality();
        assertEquals(card1, card2);
    }

    private void testAndNot() {
        int card1 = bitset.andNotCardinality(bitmap[3]);
        bitset.andNot(bitmap[3]);
        int card2 = bitset.cardinality();
        assertEquals(card1, card2);
    }
    
    /**
     * select sum(column)
     */
    private void testSum() {
        long start = System.nanoTime();
        long sumArray = 0L;
        for (int i = 0; i < nums; i++) {
            sumArray += values[i];
        }
        System.out.println("sum by array cost time: "
                + (System.nanoTime() - start));

        // The cardinality of a bitmap is always cached
        // so this aggregation should be super fast, O(1) time
        // however in this case, we assume that wasn't cached
        start = System.nanoTime();
        long sumBitmap = 0L;
        for (int j = 0; j < bitmap.length; j++) {
            sumBitmap += ((long) bitmap[j].cardinality()) << j;
        }
        System.out.println("sum by bitmap cost time: "
                + (System.nanoTime() - start));

        assertEquals(sumArray, sumBitmap);
    }

    /**
     * select count(1) from table where column >= lower and column <= upper
     */
    private void testRange() {
        int lower = 0x17777777;
        int upper = 0x5ddddddd;

        long start = System.nanoTime();
        int countArray = 0;
        for (int i = 0; i < nums; i++) {
            if ((values[i] >= lower) && (values[i] <= upper))
                countArray++;
        }
        System.out.println("count by array cost time: "
                + (System.nanoTime() - start));

        start = System.nanoTime();
        BitSet greaterThanLower = new OpenBitSet(nums);
        BitSet equalsToLower = new OpenBitSet(nums);
        equalsToLower.set(0, nums);
        
        BitSet lessThanUpper = new OpenBitSet(nums);
        BitSet equalsToUpper = new OpenBitSet(nums);
        equalsToUpper.set(0, nums);
  
        for(int i = 0; i < Integer.SIZE; i++) {
            // >= lower
            if( ((lower << i) & 0x80000000) == 0x80000000) {
                equalsToLower.and(bitmap[Integer.SIZE - 1 - i]);
            } else {
                BitSet tmp = equalsToLower.clone();
                tmp.and(bitmap[Integer.SIZE - 1 - i]);
                greaterThanLower.or(tmp);
                equalsToLower.andNot(bitmap[Integer.SIZE - 1 - i]);
            }
            
            // <= upper
            if( ((upper << i) & 0x80000000) == 0x80000000) {
                BitSet tmp = equalsToUpper.clone();
                tmp.andNot(bitmap[Integer.SIZE - 1 - i]);
                lessThanUpper.or(tmp);
                equalsToUpper.and(bitmap[Integer.SIZE - 1 - i]);
            } else {
                equalsToUpper.andNot(bitmap[Integer.SIZE - 1 - i]);
            }
        }

        BitSet result = greaterThanLower.clone();
        result.and(lessThanUpper);
        result.or(equalsToLower);
        result.or(equalsToUpper);

        int intBitmtap = result.cardinality();
        System.out.println("count by bitmap cost time: "
                + (System.nanoTime() - start));

        assertEquals(countArray, intBitmtap);
    }

}
