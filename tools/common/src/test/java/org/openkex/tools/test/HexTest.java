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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HexTest {

    private static final Logger LOG = LoggerFactory.getLogger(HexTest.class);

    @Test
    public void testConvert() {
        // encode to String
        byte[] array = new byte[] {0x00, (byte) 0xff, 0x33};
        String hexStr = Hex.toString(array);
        Assert.assertEquals("00FF33", hexStr);
        String hexStr2 = Hex.toString(array, 1, 2);
        Assert.assertEquals("FF33", hexStr2);
        // decode from String
        byte[] arrayBack = Hex.fromString(hexStr);
        Assert.assertArrayEquals(array, arrayBack);
    }

    @Test
    public void testEmpty() {
        String nullHex = Hex.toString(null);
        Assert.assertNotNull(nullHex);
        Assert.assertTrue(nullHex.equals(Hex.NULL_ARRAY));

        String emptyHex = Hex.toString(new byte[0]);
        Assert.assertNotNull(emptyHex);
        Assert.assertTrue(emptyHex.length() == 0);
    }

    @Test
    public void testErrorsFromString() {
        try {
            Hex.fromString("AAABA");  // odd number of characters
            Assert.fail();
        }
        catch (Exception e) {
            LOG.info("expected exception: " + e);
        }
        try {
            Hex.fromString("AABBCCGG");  // bad characters
            Assert.fail();
        }
        catch (Exception e) {
            LOG.info("expected exception: " + e);
        }
    }

    @Test
    public void testErrorsToString() {
        try {
            Hex.toString(new byte[] {22, 22}, 0, 3);
            Assert.fail();
        }
        catch (Exception e) {
            LOG.info("expected exception: " + e);
        }
        try {
            Hex.toString(new byte[] {22, 22}, -1, 2);
            Assert.fail();
        }
        catch (Exception e) {
            LOG.info("expected exception: " + e);
        }
    }

    @Test
    public void testToStringBlock() throws Exception {
        String eight1 = "12345678";
        String eight2 = "abcdefgh";
        byte[] data = (eight1 + eight2 + eight1).getBytes();
        LOG.info("result0:\n" + Hex.toStringBlock(data));

        LOG.info("result1:\n" + Hex.toStringBlock(data, 16, 8, false));
        LOG.info("result2:\n" + Hex.toStringBlock(data, 12, 4, true));
        LOG.info("result3:\n" + Hex.toStringBlock(data, 16, 5, true));
        LOG.info("result4:\n" + Hex.toStringBlock(data, 16, 4, true));

        LOG.info("result5:\n" + Hex.toStringBlock(new byte[] {0, 1, 2, 3, 13, 10}, 16, 8, true));

        byte[] allBytes = new byte[256];
        for (int i = 0; i < 256; i++) {
            allBytes[i] = (byte) i;
        }
        LOG.info("result8:\n" + Hex.toStringBlock(allBytes, 16, 8, true));

        for (int i = 14; i < 32; i++) {
            byte[] bytes2 = new byte[i];
            System.arraycopy(allBytes, 0, bytes2, 0, i);
            LOG.info("result16/4:\n" + Hex.toStringBlock(bytes2, 16, 4, true));
            LOG.info("result16/3:\n" + Hex.toStringBlock(bytes2, 16, 3, true));
        }
    }

    @Test
    public void testJavaToHexString() {
        // java Long and Integer have method toHexString()
        LOG.info("Integer.toHexString(0x222): " + Integer.toHexString(0x222));
        // no trailing 0
        LOG.info("Integer.toHexString(3): " + Integer.toHexString(3));
        // is using lower case. e.g. "f"
        LOG.info("Integer.toHexString(0xABEF): " + Integer.toHexString(0xABEF));
        // unsigned: i.e. "ffffffff"
        LOG.info("Integer.toHexString(-1): " + Integer.toHexString(-1));
        // "ffffffffffffffff"
        LOG.info("Long.toHexString(-1): " + Long.toHexString(-1));
    }

}
