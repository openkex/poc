/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.server.backend.test;

import org.junit.Assert;
import org.junit.Test;
import org.openkex.server.backend.BlockStoreApi;
import org.openkex.tools.ByteArrayTool;
import org.openkex.tools.RandomTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public abstract class AbstractBlockStoreTest {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractBlockStoreTest.class);

    private static final long SEED = 33333;

    protected abstract BlockStoreApi getBlockStore() throws Exception;
    protected abstract int getSizeLimit();

    @Test
    public void testSimple() throws Exception {
        RandomTool random = new RandomTool(SEED);
        long key1 = 2345111;
        long key2 = 345111435555L;
        int data1Len = 324;
        int data2Len = 123;
        byte[] data1 = random.getBytes(data1Len);
        byte[] data2 = random.getBytes(data2Len);

        BlockStoreApi store = getBlockStore();

        // store is empty
        Assert.assertEquals(0, store.getBlockCount());
        // create
        Assert.assertFalse(store.writeBlock(key1, data1));
        // update
        Assert.assertTrue(store.writeBlock(key1, data1));
        Assert.assertEquals(1, store.getBlockCount());
        Assert.assertEquals(data1Len, store.getStoreSize());
        Assert.assertTrue(Arrays.equals(data1, store.readBlock(key1)));
        Assert.assertNull(store.readBlock(key2));

        Assert.assertFalse(store.writeBlock(key2, data2));
        Assert.assertEquals(2, store.getBlockCount());
        Assert.assertTrue(Arrays.equals(data2, store.readBlock(key2)));
    }

    @Test
    public void testAppend() throws Exception {
        RandomTool random = new RandomTool(SEED);
        long key1 = 2345111;
        int data1Len = 324;
        int data2Len = 123;
        byte[] data1 = random.getBytes(data1Len);
        byte[] data2 = random.getBytes(data2Len);

        BlockStoreApi store = getBlockStore();
        // create
        Assert.assertFalse(store.writeBlock(key1, data1));
        // append
        store.appendBlock(key1, data2);
        Assert.assertEquals(data1Len + data2Len, store.getStoreSize());
        Assert.assertTrue(Arrays.equals(ByteArrayTool.add(data1, data2), store.readBlock(key1)));
        // overwrite
        Assert.assertTrue(store.writeBlock(key1, data2));
        Assert.assertTrue(Arrays.equals(data2, store.readBlock(key1)));
    }

    @Test
    public void testSize() throws Exception {
        RandomTool random = new RandomTool(SEED);
        BlockStoreApi store = getBlockStore();

        int keyCount = 1000;
        int keyStep = 39;
        int startKey = 1000000;
        int blockSize = getSizeLimit() / keyCount;

        for (int i = 0; i < keyCount; i++) {
            Assert.assertFalse(store.writeBlock(i * keyStep + startKey, random.getBytes(blockSize)));
        }

        // reset seed for identical results ...
        random = new RandomTool(SEED);
        for (int i = 0; i < keyCount; i++) {
            Assert.assertTrue(Arrays.equals(random.getBytes(blockSize), store.readBlock(i * keyStep + startKey)));
        }

        Assert.assertEquals(keyCount, store.getBlockCount());
        Assert.assertEquals(blockSize * keyCount, store.getStoreSize());
    }

    @Test
    public void testError() throws Exception {
        BlockStoreApi store = getBlockStore();
        try {
            store.writeBlock(333, null);
            Assert.fail();
        }
        catch (Exception e) {
            LOG.info("expected exception: " + e);
        }
    }

}
