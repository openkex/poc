/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.tools.test;

import org.junit.Assert;
import org.junit.Test;
import org.openkex.tools.Hex;
import org.openkex.tools.RandomCheck;
import org.openkex.tools.RandomTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;

public class RandomCheckTest {

    private static final Logger LOG = LoggerFactory.getLogger(RandomCheckTest.class);

    @Test
    public void testRandom() {

        RandomTool random = new RandomTool();
        int repeats = 100000;
        int length = 6;
        int falseCount = 0;

        for (int i = 0; i < repeats; i++) {
            byte[] sample = random.getBytes(length);
            if (!RandomCheck.estimateRandom(sample)) {
                falseCount++;
            }
        }
        LOG.info("average 'false' detection rate: " + 1.0 * falseCount / repeats);
    }

    @Test
    public void showOkRandom() {

        RandomTool random = new RandomTool();
        int repeats = 100;
        int length = 6;

        for (int i = 0; i < repeats; i++) {
            byte[] sample = random.getBytes(length);
            if (RandomCheck.estimateRandom(sample)) {
                LOG.info("OK Random: " + Hex.toString(sample));
            }
        }
    }

    @Test
    public void showFailRandom() {

        RandomTool random = new RandomTool();
        int count = 100;
        int length = 6;
        int c = 0;
        while (c < count) {
            byte[] sample = random.getBytes(length);
            if (!RandomCheck.estimateRandom(sample)) {
                LOG.info("NOK Random: " + Hex.toString(sample));
                c++;
            }
        }
    }

    @Test
    public void testRandomFail() {
        try {
            RandomCheck.estimateRandom(new byte[] {1, 2, 3, 4, 5});
            Assert.fail();
        }
        catch (Exception e) {
            LOG.info("expected: " + e);
        }
    }

    @Test
    public void testPatterns() {
        // patterns that must not be valid
        checkNotRandom("AABBCCAABBCC");
        checkNotRandom("010203040506");
        checkNotRandom("11223344AABB");
        checkNotRandom("1234567890AB");
        checkNotRandom("CAFEBABE4711");  // "funny" printing hex chars (like java class signature)
//        checkNotRandom("CAFE4711BABE");
        checkNotRandom("020406084733"); // sequence 2
        checkNotRandom("0306090c4733"); // sequence 3
        checkNotRandom("0000362B6FA4"); // domain rule

        Charset charset = Charset.forName("ASCII");
        checkNotRandom("Otto12".getBytes(charset));
        checkNotRandom("Hansi2".getBytes(charset));
        checkNotRandom("ALI666".getBytes(charset));
        checkNotRandom("abcdef".getBytes(charset));
        checkNotRandom("ABCDEF".getBytes(charset));
        checkNotRandom("123456".getBytes(charset));
    }

    private void checkNotRandom(byte[] pattern) {
        Assert.assertFalse(RandomCheck.estimateRandom(pattern));
    }

    private void checkNotRandom(String pattern) {
        Assert.assertFalse(RandomCheck.estimateRandom(Hex.fromString(pattern)));
    }

    @Test
    public void testMaxSequence() {
        String test = "ABCDEF1ABCDE3ABCDEEEE";
        int count = RandomCheck.countRangeSequence(test, "ABCDEF");
        Assert.assertEquals(8, count);
        test = "ABCDEF1ABCDE3ABCD2EE1EE";
        count = RandomCheck.countRangeSequence(test, "ABCDEF");
        Assert.assertEquals(6, count);
        test = "ABC1DEF1ABCDE3ABCD2EE1EE";
        count = RandomCheck.countRangeSequence(test, "ABCDEF");
        Assert.assertEquals(5, count);
    }

}
