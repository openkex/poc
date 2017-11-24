/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.tools.test;

import org.junit.Assert;
import org.junit.Test;
import org.openkex.tools.NumberByteConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class NumberByteConverterTest {

    private static final Logger LOG = LoggerFactory.getLogger(NumberByteConverterTest.class);

    private static final int RANDOM_REPEATS = 500000;

    @Test
    public void testPrint() {
        showIntString(0);
        showIntString(1);
        showIntString(-1);
        showIntString(0x100);
        showIntString(0x10000);
        showIntString(235234234);
        showIntString(0x11223344);

        showLongString(0);
        showLongString(1);
        showLongString(0x100);
        showLongString(0x10000);
        showLongString(-1);
        showLongString(12453266654333L);
        showLongString(0x1122334455667788L);

        showSixByteLongString(0);
        showSixByteLongString(1);
        showSixByteLongString(0x100);
        showSixByteLongString(0x10000);
        showSixByteLongString(0x100000);
        showSixByteLongString(0xFFFFFFFFFFFFL);
    }

    private void showIntString(int value) {
        LOG.info("int:" + value + " bytes=" + NumberByteConverter.intToByteString(value) + " toHexString=" + Integer.toHexString(value));
    }

    private void showLongString(long value) {
        LOG.info("long:" + value + " bytes=" + NumberByteConverter.longToByteString(value) + " toHexString=" + Long.toHexString(value));
    }

    private void showSixByteLongString(long value) {
        LOG.info("long6b:" + value + " bytes=" + NumberByteConverter.longToSixByteString(value) + " toHexString=" + Long.toHexString(value));
    }

    @Test
    public void testIntFail() {
        testIntFailInternal(null);
        testIntFailInternal(new byte[3]);
        testIntFailInternal(new byte[5]);
    }

    private void testIntFailInternal(byte[] bytes) {
        try {
            NumberByteConverter.bytesToInt(bytes);
            Assert.fail();
        }
        catch (Exception e) {
            LOG.info("expected:" + e);
        }
    }

    @Test
    public void testLongFail() {
        testLongFailInternal(null);
        testLongFailInternal(new byte[7]);
        testLongFailInternal(new byte[9]);
    }

    private void testLongFailInternal(byte[] bytes) {
        try {
            NumberByteConverter.bytesToLong(bytes);
            Assert.fail();
        }
        catch (Exception e) {
            LOG.info("expected:" + e);
        }
    }

    @Test
    public void testSixByteLongFail() {
        testSixByteLongFailInternal(null);
        testSixByteLongFailInternal(new byte[5]);
        testSixByteLongFailInternal(new byte[7]);
    }

    @Test
    public void testSixByteLongFail2() {
        // must no be negative
        testSixByteLongFailInternal2(-1);
        testSixByteLongFailInternal2(-1894734534533L);
        // max value 0xFFFFFFFFFFFF
        testSixByteLongFailInternal2(0xFFFFFFFFFFFFL + 1);
    }

    private void testSixByteLongFailInternal(byte[] bytes) {
        try {
            NumberByteConverter.sixBytesToLong(bytes);
            Assert.fail();
        }
        catch (Exception e) {
            LOG.info("expected:" + e);
        }
    }

    private void testSixByteLongFailInternal2(long value) {
        try {
            NumberByteConverter.longToSixBytes(value);
            Assert.fail();
        }
        catch (Exception e) {
            LOG.info("expected:" + e);
        }
    }

    @Test
    public void testInt() {
        testIntInternal(0);
        testIntInternal(1);
        testIntInternal(256);
        testIntInternal(256 * 256);
        testIntInternal(256 * 256 * 256);
        testIntInternal(-1);
        testIntInternal(-256);
        testIntInternal(-256 * 256);
        testIntInternal(-256 * 256 * 256);
    }

    @Test
    public void testIntRand() {
        Random rand = new Random(4711);  // fixed seed
        for (int i = 0; i < RANDOM_REPEATS; i++) {
            int value = rand.nextInt();
            testIntInternal(value);
        }
    }

    @Test
    public void testSixBytesLong() {
        testSixBytesLongInternal(0);
        testSixBytesLongInternal(1);
        testSixBytesLongInternal(256);
        testSixBytesLongInternal(256 * 256);
        testSixBytesLongInternal(256 * 256 * 256);
        testSixBytesLongInternal(256L * 256 * 256 * 256);
        testSixBytesLongInternal(256L * 256 * 256 * 256 * 256);
        testSixBytesLongInternal(0xFFFFFFFFFFFFL);
    }

    @Test
    public void testSixBytesRand() {
        Random rand = new Random(4713);  // fixed seed
        byte[] bytes = new byte[6];
        for (int i = 0; i < RANDOM_REPEATS; i++) {
            rand.nextBytes(bytes);
            long value = NumberByteConverter.sixBytesToLong(bytes);
            testSixBytesLongInternal(value);
        }
    }

    @Test
    public void testLong() {
        testLongInternal(0);
        testLongInternal(1);
        testLongInternal(256);
        testLongInternal(256 * 256);
        testLongInternal(256 * 256 * 256);
        testLongInternal(256L * 256 * 256 * 256);
        testLongInternal(256L * 256 * 256 * 256 * 256);
        testLongInternal(256L * 256 * 256 * 256 * 256 * 256);
        testLongInternal(-1);
        testLongInternal(-256);
        testLongInternal(-256 * 256);
        testLongInternal(-256 * 256 * 256);
    }

    @Test
    public void testLongRand() {
        Random rand = new Random(4712);  // fixed seed
        for (int i = 0; i < RANDOM_REPEATS; i++) {
            long value = rand.nextLong();
            testLongInternal(value);
        }
    }

    private void testIntInternal(int value) {
        byte[] intBytes = NumberByteConverter.intToBytes(value);
        int intFromBytes = NumberByteConverter.bytesToInt(intBytes);
        Assert.assertEquals(value, intFromBytes);
    }

    private void testLongInternal(long value) {
        byte[] intBytes = NumberByteConverter.longToBytes(value);
        long intFromBytes = NumberByteConverter.bytesToLong(intBytes);
        Assert.assertEquals(value, intFromBytes);
    }

    private void testSixBytesLongInternal(long value) {
        byte[] intBytes = NumberByteConverter.longToSixBytes(value);
        long intFromBytes = NumberByteConverter.sixBytesToLong(intBytes);
        Assert.assertEquals(value, intFromBytes);
    }

}
