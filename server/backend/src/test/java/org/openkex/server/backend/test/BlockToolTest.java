/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.server.backend.test;

import org.junit.Assert;
import org.junit.Test;
import org.openkex.server.backend.BlockTool;

import java.util.Random;

public class BlockToolTest {

    private static final long SEED = 34234211L;

    @Test
    public void TestConvert() {
        long time = BlockTool.ROUND_ZERO_START;
        Assert.assertEquals(0, BlockTool.getRoundNr(time));

        time = BlockTool.ROUND_ZERO_START + BlockTool.ROUND_INTERVAL_SECONDS - 1;
        Assert.assertEquals(0, BlockTool.getRoundNr(time));

        time = BlockTool.ROUND_ZERO_START + BlockTool.ROUND_INTERVAL_SECONDS;
        Assert.assertEquals(1, BlockTool.getRoundNr(time));

        Assert.assertEquals(BlockTool.ROUND_ZERO_START, BlockTool.getRoundStart(0));
        Assert.assertEquals(BlockTool.ROUND_ZERO_START + BlockTool.ROUND_INTERVAL_SECONDS, BlockTool.getRoundStart(1));
    }

    @Test
    public void TestConvertRandom() {
        Random random = new Random(SEED);
        int count = 10000;
        for (int i = 0; i < count; i++) {
            long time = Math.abs(random.nextInt()); // positive value
            long round = BlockTool.getRoundNr(time);
            long start = BlockTool.getRoundStart(round);
            long diff = time - start;
            Assert.assertTrue(diff >= 0 && diff < BlockTool.ROUND_INTERVAL_SECONDS);
        }
        for (int i = 0; i < count; i++) {
            long time = i + 2343487;
            long round = BlockTool.getRoundNr(time);
            long start = BlockTool.getRoundStart(round);
            long diff = time - start;
            Assert.assertTrue(diff >= 0 && diff < BlockTool.ROUND_INTERVAL_SECONDS);
        }

    }
}
