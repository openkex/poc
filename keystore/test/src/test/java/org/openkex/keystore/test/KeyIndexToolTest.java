/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.keystore.test;

import org.junit.Assert;
import org.junit.Test;
import org.openkex.keystore.smartcardhsm.KeyIndexTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeyIndexToolTest {

    private static final Logger LOG = LoggerFactory.getLogger(KeyIndexToolTest.class);

    private static final String INDEX_SEPARATOR = ":"; // This is a copy from KeyIndexTool

    @Test
    public void testSimple() {
        testRoundTrip(new String[]{"Otto", "Mops"});
        testRoundTrip(new String[]{"Otto", "Mops", ""});
        testRoundTrip(new String[]{"Otto", "Mops", "", ""});
        testRoundTrip(new String[]{"Otto", "", "Mops"});

    }

    private void testRoundTrip(String[] strings) {
        byte[] bytes = KeyIndexTool.join(strings);
        String[] stringsBack = KeyIndexTool.split(bytes);
        Assert.assertArrayEquals(strings, stringsBack);
    }

    @Test
    public void testError() {
        try {
            KeyIndexTool.join(new String[]{"Otto", "Mops" + INDEX_SEPARATOR});
            Assert.fail();
        }
        catch (Exception e) {
            LOG.info("Expected: " + e);
        }
    }

    @Test
    public void testIndex() {
        String s1 = "LALA";
        String s2 = "LALO";
        String s3 = "LOLA";
        String s4 = "LOLO";
        String[] array = new String[] {s1, s2, s3};
        Assert.assertEquals(0, KeyIndexTool.getIndex(array, s1));
        Assert.assertEquals(1, KeyIndexTool.getIndex(array, s2));
        Assert.assertEquals(2, KeyIndexTool.getIndex(array, s3));
        Assert.assertEquals(-1, KeyIndexTool.getIndex(array, s4));
    }

    @Test
    public void testAdd() {
        String s1 = "LALA";
        String s2 = "LALO";
        String s3 = "LOLA";
        String s4 = "LOLO";
        String[] array = new String[] {s1, s2, s3};
        String[] arrayNew = KeyIndexTool.addValue(array, s4);
        Assert.assertEquals(4, arrayNew.length);
        Assert.assertEquals(s4, arrayNew[3]); // as last value
    }

    @Test
    public void testAddFree() {
        String s1 = "LALA";
        String s2 = "";
        String s3 = "LOLA";
        String s4 = "LOLO";
        String[] array = new String[] {s1, s2, s3};
        String[] arrayNew = KeyIndexTool.addValue(array, s4);
        Assert.assertEquals(3, arrayNew.length);
        Assert.assertEquals(s4, arrayNew[1]);
    }
}
