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
import org.openkex.tools.RandomTool;

public class RandomToolTest {

    private static final long SEED = 47110815;
    private static final String EXPECTED = "6D8869175A373241A03A5C0340BEA950EC3321917943C879CD1C479FD905";

    /**
     * validate that the pseudo random generator with fixed seed is system and JVM independent
     */
    @Test
    public void testFixedSeed() {
        RandomTool gen = new RandomTool(SEED);
        byte[] bytes = gen.getBytes(30);

        Assert.assertEquals(EXPECTED, Hex.toString(bytes));
    }

    @Test
    public void testSeed() {
        RandomTool gen = new RandomTool();  // seed is time based, 47110815 is long ago
        byte[] bytes = gen.getBytes(30);
        Assert.assertNotEquals(EXPECTED, Hex.toString(bytes));  // well, not that "significant"
    }

}
