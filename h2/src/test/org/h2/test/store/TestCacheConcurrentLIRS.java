/*
 * Copyright 2004-2011 H2 Group. Multiple-Licensed under the H2 License, Version
 * 1.0, and under the Eclipse Public License, Version 1.0
 * (http://h2database.com/html/license.html). Initial Developer: H2 Group
 */
package org.h2.test.store;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import org.h2.dev.store.btree.CacheLongKeyLIRS;
import org.h2.test.TestBase;
import org.h2.util.Task;

/**
 * Tests the cache algorithm.
 */
public class TestCacheConcurrentLIRS extends TestBase {

    /**
     * Run just this test.
     *
     * @param a ignored
     */
    public static void main(String... a) throws Exception {
        TestBase.createCaller().init().test();
    }

    public void test() throws Exception {
        testConcurrent();
    }

    private static void testConcurrent() {
        final CacheLongKeyLIRS<Integer> test = CacheLongKeyLIRS.newInstance(100, 1);
        int threadCount = 8;
        final CountDownLatch wait = new CountDownLatch(1);
        final AtomicBoolean stopped = new AtomicBoolean();
        Task[] tasks = new Task[threadCount];
        final int[] getCounts = new int[threadCount];
        final int offset = 1000000;
        for (int i = 0; i < 100; i++) {
            test.put(offset + i, i);
        }
        final int[] keys = new int[1000];
        Random random = new Random(1);
        for (int i = 0; i < keys.length; i++) {
            int key;
            do {
                key = (int) Math.abs(random.nextGaussian() * 50);
            } while (key > 100);
            keys[i] = key;
        }
        for (int i = 0; i < threadCount; i++) {
            final int x = i;
            Task t = new Task() {
                @Override
                public void call() throws Exception {
                    Random random = new Random(x);
                    wait.await();
                    int i = 0;
                    for (; !stopped.get(); i++) {
                        int key = keys[random.nextInt(keys.length)];
                        test.get(offset + key);
                        if ((i & 127) == 0) {
                            test.put(offset + random.nextInt(100), random.nextInt());
                        }
                    }
                    getCounts[x] = i;
                }
            };
            t.execute("t" + i);
            tasks[i] = t;
        }
        wait.countDown();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        stopped.set(true);
        for (Task t : tasks) {
            t.get();
        }
        int totalCount = 0;
        for (int x : getCounts) {
            totalCount += x;
        }
        System.out.println("requests: " + totalCount);
    }

}
