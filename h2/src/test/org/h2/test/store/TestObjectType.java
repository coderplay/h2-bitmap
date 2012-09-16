/*
 * Copyright 2004-2011 H2 Group. Multiple-Licensed under the H2 License,
 * Version 1.0, and under the Eclipse Public License, Version 1.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.test.store;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;
import org.h2.test.TestBase;

/**
 * Test the ObjectType class.
 */
public class TestObjectType extends TestBase {

    /**
     * Run just this test.
     *
     * @param a ignored
     */
    public static void main(String... a) throws Exception {
        TestBase.createCaller().init().test();
    }

    @Override
    public void test() {
        testCommonValues();
    }

    private void testCommonValues() {
        BigInteger largeBigInt = BigInteger.probablePrime(200,  new Random(1));
        ObjectType ot = new ObjectType();
        assertEquals("o", ot.asString());
        Object[] array = {
                false, true,
                Byte.MIN_VALUE, (byte) -1, (byte) 0, (byte) 1, Byte.MAX_VALUE,
                Short.MIN_VALUE, (short) -1, (short) 0, (short) 1, Short.MAX_VALUE,
                Integer.MIN_VALUE, Integer.MIN_VALUE + 1,
                -1000, -100, -1, 0, 1, 2, 14,
                15, 16, 17, 100, Integer.MAX_VALUE - 1, Integer.MAX_VALUE,
                Long.MIN_VALUE, Long.MIN_VALUE + 1, -1000L, -1L, 0L, 1L, 2L, 14L,
                15L, 16L, 17L, 100L, Long.MAX_VALUE - 1, Long.MAX_VALUE,
                largeBigInt.negate(), BigInteger.valueOf(-1), BigInteger.ZERO,
                BigInteger.ONE, BigInteger.TEN, largeBigInt,
                Float.NEGATIVE_INFINITY, -Float.MAX_VALUE, -1f, -0f, 0f,
                Float.MIN_VALUE, 1f, Float.MAX_VALUE,
                Float.POSITIVE_INFINITY, Float.NaN,
                Double.NEGATIVE_INFINITY, -Double.MAX_VALUE, -1d, -0d, 0d,
                Double.MIN_VALUE, 1d, Double.MAX_VALUE,
                Double.POSITIVE_INFINITY, Double.NaN,
                BigDecimal.valueOf(Double.MAX_VALUE).negate(),
                new BigDecimal(largeBigInt).negate(),
                BigDecimal.valueOf(-100.0), BigDecimal.ZERO, BigDecimal.ONE,
                BigDecimal.TEN, BigDecimal.valueOf(Long.MAX_VALUE),
                new BigDecimal(largeBigInt),
                BigDecimal.valueOf(Double.MAX_VALUE),
                Character.MIN_VALUE, '0', 'a', Character.MAX_VALUE,
                "", " ", "  ", "123456789012345", "1234567890123456",
                new String(new char[100]).replace((char) 0, 'x'),
                new String(new char[100000]).replace((char) 0, 'x'), "y",
                "\u1234", "\u2345", "\u6789", "\uffff",
                new UUID(Long.MIN_VALUE, Long.MIN_VALUE),
                new UUID(Long.MIN_VALUE, 0), new UUID(0, 0),
                new UUID(Long.MAX_VALUE, Long.MAX_VALUE),
                new byte[0], new byte[1], new byte[15], new byte[16],
                new byte[10000], new byte[] { (byte) 1 },
                new int[0], new int[1], new int[15], new int[16],
                new int[10000], new int[] { (byte) 1 },
                new long[0], new long[1], new long[15], new long[16],
                new long[10000], new long[] { (byte) 1 },
                new char[0], new char[1], new char[10000], new char[] { (char) 1 },
                new java.util.Date(0), new java.util.Date(1000),
                new Timestamp(2000), new Timestamp(3000),
                new java.util.Date(4000), new java.util.Date(5000),
                new Object[0], new Object[] { 1 },
                new Object[] { 0.0, "Hello", null, Double.NaN },
                new Object[100]
            };
        Object otherType = false;
        Object last = null;
        for (Object x : array) {
            test(otherType, x);
            if (last != null) {
                int comp = ot.compare(x, last);
                if (comp <= 0) {
                    ot.compare(x, last);
                    fail(x.getClass().getName() + ": " + x.toString() + " " + comp);
                }
                assertTrue(x.toString(), ot.compare(last, x) < 0);
            }
            if (last != null && last.getClass() != x.getClass()) {
                otherType = last;
            }
            last = x;
        }
    }

    private void test(Object last, Object x) {
        ObjectType ot = new ObjectType();

        // switch to the last type before every operation,
        // to test switching types
        ot.getMemory(last);
        assertTrue(ot.getMemory(x) >= 0);

        ot.getMemory(last);
        assertTrue(ot.getMaxLength(x) >= 1);

        ot.getMemory(last);
        assertEquals(0, ot.compare(x, x));
        ByteBuffer buff = ByteBuffer.allocate(ot.getMaxLength(x) + 1);

        ot.getMemory(last);
        ot.write(buff, x);
        buff.put((byte) 123);
        buff.flip();

        ot.getMemory(last);
        Object y = ot.read(buff);
        assertEquals(123, buff.get());
        assertEquals(0, buff.remaining());
        assertEquals(x.getClass().getName(), y.getClass().getName());

        ot.getMemory(last);
        assertEquals(0, ot.compare(x,  y));
        if (x.getClass().isArray()) {
            if (x instanceof byte[]) {
                assertTrue(Arrays.equals((byte[]) x, (byte[]) y));
            } else if (x instanceof char[]) {
                assertTrue(Arrays.equals((char[]) x, (char[]) y));
            } else if (x instanceof int[]) {
                assertTrue(Arrays.equals((int[]) x, (int[]) y));
            } else if (x instanceof long[]) {
                assertTrue(Arrays.equals((long[]) x, (long[]) y));
            } else {
                assertTrue(Arrays.equals((Object[]) x, (Object[]) y));
            }
        } else {
            assertEquals(x.hashCode(), y.hashCode());
            assertTrue(x.equals(y));
        }
    }

}
