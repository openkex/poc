/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.tools.test;

import org.junit.Assert;
import org.junit.Test;
import org.openkex.tools.ByteArrayTool;
import org.openkex.tools.Hex;
import org.openkex.tools.RandomTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class ByteArrayToolTest {

    private static final Logger LOG = LoggerFactory.getLogger(ByteArrayToolTest.class);

    @Test
    public void testCompare() {

        Assert.assertEquals(0, ByteArrayTool.compareByteArray(new byte[] {0}, new byte[] {0}));
        Assert.assertEquals(1, ByteArrayTool.compareByteArray(new byte[] {0, 0}, new byte[] {0}));
        Assert.assertEquals(-1, ByteArrayTool.compareByteArray(new byte[] {0}, new byte[] {0, 0}));

        Assert.assertEquals(1, ByteArrayTool.compareByteArray(new byte[] {0, 0, 2}, new byte[] {0, 0, 1}));
        Assert.assertEquals(-1, ByteArrayTool.compareByteArray(new byte[] {0, 0, 1}, new byte[] {0, 0, 2}));

        Assert.assertEquals(1, ByteArrayTool.compareByteArray(new byte[] {2, 0, 2}, new byte[] {2, 0, 1}));
        Assert.assertEquals(-1, ByteArrayTool.compareByteArray(new byte[] {3, 0, 1}, new byte[] {3, 0, 2}));
    }

    @Test
    public void showCompare() {
        int size = 33;
        RandomTool randomTool = new RandomTool(555);
        ArrayList<byte[]> hashes = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            hashes.add(randomTool.getBytes(8));
        }

        hashes.sort(ByteArrayTool::compareByteArray);

        for (int i = 0; i < size; i++) {
            LOG.info("sorted: " + Hex.toString(hashes.get(i)));
        }
    }

    @Test
    public void showCompareLength() {
        int size = 32;
        RandomTool randomTool = new RandomTool(555);
        ArrayList<byte[]> hashes = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            hashes.add(randomTool.getBytes(4 + i / 4));
        }

        hashes.sort(ByteArrayTool::compareByteArray);

        for (int i = 0; i < size; i++) {
            LOG.info("sorted: " + Hex.toString(hashes.get(i)));
        }
    }

    @Test
    public void testBitsDifferent() {
        byte[] a = {0, 0, 0};
        byte[] b = {0, 0, 0};
        byte[] c = {0, 8, 0};
        byte[] d = {0, 12, 0};
        byte[] e = {15, 15, 15};
        byte[] f = {14, 15, 14};
        byte[] g = {(byte) 0xff, (byte) 0xff, (byte) 0xff};

        Assert.assertEquals(0, ByteArrayTool.bitsDifferent(a, b));
        Assert.assertEquals(1, ByteArrayTool.bitsDifferent(b, c));
        Assert.assertEquals(2, ByteArrayTool.bitsDifferent(b, d));
        Assert.assertEquals(12, ByteArrayTool.bitsDifferent(a, e));
        Assert.assertEquals(2, ByteArrayTool.bitsDifferent(e, f));
        Assert.assertEquals(10, ByteArrayTool.bitsDifferent(a, f));

        Assert.assertEquals(24, ByteArrayTool.bitsDifferent(a, g));
        Assert.assertEquals(24, ByteArrayTool.bitsDifferent(g, a));
    }

    @Test
    public void testBitsDifferentProblem() {
        byte[] a = {0};
        byte[] b = {(byte) 0xFF};
        byte[] c = {(byte) 0x80};
        // initial problem with 8th bit -> result 32 instead of 8
        Assert.assertEquals(8, ByteArrayTool.bitsDifferent(a, b));
        Assert.assertEquals(8, ByteArrayTool.bitsDifferent(b, a));
        Assert.assertEquals(1, ByteArrayTool.bitsDifferent(a, c));
        Assert.assertEquals(1, ByteArrayTool.bitsDifferent(c, a));
    }

    @Test
    public void testToggleBit() {
        byte[] a = {0, 0, 0};
        ByteArrayTool.toggleBit(a, 3);
        Assert.assertEquals(a[0], 8);
        ByteArrayTool.toggleBit(a, 0);
        Assert.assertEquals(a[0], 9);
        ByteArrayTool.toggleBit(a, 8);
        Assert.assertEquals(a[1], 1);
        ByteArrayTool.toggleBit(a, 23);
        Assert.assertEquals(a[2], (byte) 0x80);
        try {
            ByteArrayTool.toggleBit(a, 24);
            Assert.fail();
        }
        catch (Exception e) {
            LOG.info("expected exception: " + e);
        }
        try {
            ByteArrayTool.toggleBit(a, -1);
            Assert.fail();
        }
        catch (Exception e) {
            LOG.info("expected exception: " + e);
        }
    }

    @Test
    public void testToggleBitRamp() {
        byte[] a = {0, 0, 0, 0};
        byte[] b = {0, 0, 0, 0};
        for (int i = 0; i < 32; i++) {
            ByteArrayTool.toggleBit(a, i);
            int different = ByteArrayTool.bitsDifferent(a, b);
            Assert.assertEquals(i + 1, different);
        }
    }

    @Test
    public void testSizeError() {
        try {
            byte[] a = {0, 0, 0, 0};
            byte[] b = {0, 0, 0};
            ByteArrayTool.bitsDifferent(a, b);
            Assert.fail();
        }
        catch (Exception e) {
            LOG.info("expected exception: " + e);
        }
    }

    @Test
    public void testAdd() {
        byte[] a = {1, 12, 0};
        byte[] b = {15, 3, 15, 3};
        byte[] e = {};
        byte[] sum = {1, 12, 0, 15, 3, 15, 3};

        Assert.assertArrayEquals(sum, ByteArrayTool.add(a, b));

        // add empty
        Assert.assertArrayEquals(a, ByteArrayTool.add(a, e));
        Assert.assertArrayEquals(a, ByteArrayTool.add(e, a));
    }

    @Test
    public void testAddFail() {
        byte[] a = {1, 12, 0};
        try {
            ByteArrayTool.add(a, null);
        }
        catch (Exception e) {
            LOG.info("expected exception: " + e);
        }
        try {
            ByteArrayTool.add(null, a);
        }
        catch (Exception e) {
            LOG.info("expected exception: " + e);
        }
    }

}
