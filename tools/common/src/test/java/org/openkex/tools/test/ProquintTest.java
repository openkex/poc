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
import org.openkex.tools.NumberByteConverter;
import org.openkex.tools.Proquint;
import org.openkex.tools.RandomTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProquintTest {

    private static final Logger LOG = LoggerFactory.getLogger(ProquintTest.class);

    @Test
    public void testEncodeDecode() {
        testEncodeDecodeInternalString("0000");
        testEncodeDecodeInternalString("0001");
        testEncodeDecodeInternalString("0002");
        testEncodeDecodeInternalString("AABB");
        testEncodeDecodeInternalString("AABBAABB");
        testEncodeDecodeInternalString("AABBCCDD");
        testEncodeDecodeInternalString("AABBCCDE");
        testEncodeDecodeInternalString("00112233445566778800AABBCCDDEEFF");
    }

    @Test
    public void testDecodeError() {
        testDecodeErrorInternal("");
        testDecodeErrorInternal("BABA");  // too short
        testDecodeErrorInternal("BABAA"); // wrong char
        testDecodeErrorInternal("BABBB"); // wrong char
        testDecodeErrorInternal("BABAB-BABA"); // too short
        testDecodeErrorInternal("BABAB+BABAB"); // wrong sep
    }

    private void testDecodeErrorInternal(String input) {
        try {
            Proquint.decode(input);
            Assert.fail();
        }
        catch (Exception e) {
            LOG.info("expected: " + e);
        }
    }

    @Test
    public void testEncodeError() {
        testEncodeErrorInternal(null);
        testEncodeErrorInternal(Hex.fromString("AA"));  // too short
        testEncodeErrorInternal(Hex.fromString("AABBCC"));  // wrong size
    }

    private void testEncodeErrorInternal(byte[] input) {
        try {
            Proquint.encode(input);
            Assert.fail();
        }
        catch (Exception e) {
            LOG.info("expected: " + e);
        }
    }

    @Test
    public void testAll() {
        // all 64k variants
        for (int i = 0; i < 0x10000; i++) {
            byte[] fromInt = NumberByteConverter.intToBytes(i);
            testEncodeDecodeInternal(fromInt, false);
        }
    }

    @Test
    public void testLong() {
        int size = 1;
        RandomTool rt = new RandomTool();
        for (int i = 0; i < 16; i++) {  // test up to 64kByte
            size *= 2;
            testEncodeDecodeInternal(rt.getBytes(size), false);
        }
    }

    @Test
    public void testRandom() {
        // show 50 random proquint strings
        RandomTool rt = new RandomTool();
        for (int i = 0; i < 50; i++) {
            byte[] fromInt = rt.getBytes(6);
            testEncodeDecodeInternal(fromInt, true);
        }
    }

    @Test
    public void testData() {
        // sample found in source tree https://github.com/dsw/proquint/blob/master/check_cor.txt
        String pq1 = Proquint.encode(Hex.fromString("7f000001"));
        Assert.assertEquals("lusab-babad", pq1);
        String pq2 = Proquint.encode(Hex.fromString("3f54dcc1"));
        Assert.assertEquals("gutih-tugad", pq2);
    }

    private void testEncodeDecodeInternalString(String hexString) {
        byte[] hex = Hex.fromString(hexString);
        testEncodeDecodeInternal(hex, true);
    }

    private void testEncodeDecodeInternal(byte[] hex, boolean log) {
        String encode = Proquint.encode(hex);
        byte[] decode = Proquint.decode(encode);
        Assert.assertArrayEquals(hex, decode);

        if (log) {
            LOG.info("hex=" + Hex.toString(hex) + " pq=" + encode + " pq(upper)=" + encode.toUpperCase());
        }
    }

}
