/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.cardio.common.test;

import org.junit.Assert;
import org.junit.Test;
import org.openkex.cardio.common.PinTool;
import org.openkex.tools.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PinToolTest {

    private static final Logger LOG = LoggerFactory.getLogger(PinToolTest.class);

    @Test
    public void testEncode() {
        testEncodeInternal(new char[] {'1', '2', '3', '4', '5', '6'}, "26123456FFFFFFFF");
        testEncodeInternal(new char[] {'1', '3', '4', '5', '6'}, "2513456FFFFFFFFF");
        testEncodeInternal(new char[] {'3', '4', '5', '9'}, "243459FFFFFFFFFF");
        testEncodeInternal(new char[] {'1', '2', '3', '4', '5', '6', '1', '2', '3', '4', '5', '6'}, "2C123456123456FF");
    }

    private void testEncodeInternal(char[] password, String encodedHex) {
        PinTool tool = new PinTool();
        tool.setInput(password);
        byte[] encoded = tool.getEncoded();
        LOG.info("encoded=" + Hex.toString(encoded));
        byte[] expected = Hex.fromString(encodedHex);
        Assert.assertArrayEquals(expected, encoded);
        tool.purge();
    }

    @Test
    public void purgeTest() {
        PinTool tool = new PinTool();
        tool.purge();
        tool.setInput(new char[] {'1', '2', '3', '4'});
        tool.getEncoded();
        tool.purge();
        byte[] encoded = tool.getEncoded();
        Assert.assertArrayEquals(encoded, new byte[] {0, 0, 0, 0, 0, 0, 0, 0});
    }

    @Test
    public void testEncodeFail() {
        testEncodeFailInternal(new char[] {'1', '2', 'A', '4', '5', '6'});
        testEncodeFailInternal(new char[] {'1', '/', '1', '1'});
        testEncodeFailInternal(new char[] {':', '2', '1', '1'});
        testEncodeFailInternal(new char[] {'1', '2', '3', '4', '5', '6', '1', '2', '3', '4', '5', '6', '7'});
        testEncodeFailInternal(new char[] {'1', '2', '3'});
    }

    private void testEncodeFailInternal(char[] password) {
        try {
            PinTool tool = new PinTool();
            tool.setInput(password);
            Assert.fail();
        }
        catch (Exception e) {
            LOG.info("expected exception: " + e);
        }
    }
}

