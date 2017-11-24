/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.tools.test;

import org.junit.Test;
import org.openkex.tools.Entropy;
import org.openkex.tools.RandomTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class EntropyTest {

    private static final Logger LOG = LoggerFactory.getLogger(EntropyTest.class);

    @Test
    public void testShannon() throws Exception {
        byte[] bytes = new byte[4096];
        LOG.info("ent=" + Entropy.getShannonEntropy(bytes));  // 0.0
        Arrays.fill(bytes, (byte) 33);
        LOG.info("ent=" + Entropy.getShannonEntropy(bytes));  // 0.0
        bytes[4] = 32;
        LOG.info("ent=" + Entropy.getShannonEntropy(bytes));  // very low 0.00328

        byte[] rand = new RandomTool(33).getBytes(1024);
        LOG.info("ent=" + Entropy.getShannonEntropy(rand)); // 7.813
        rand = new RandomTool(4711).getBytes(1024);
        LOG.info("ent=" + Entropy.getShannonEntropy(rand)); // 7.821
        rand = new RandomTool(34516).getBytes(1024);
        LOG.info("ent=" + Entropy.getShannonEntropy(rand)); // 7.841

        rand = new RandomTool(34).getBytes(32768);
        LOG.info("ent=" + Entropy.getShannonEntropy(rand)); // 7.99 // getting close to 8 for larger random arrays

        bytes = new byte[256];
        for (int i = 0; i < 256; i++) {
            bytes[i] = (byte) i;
        }
        LOG.info("ent=" + Entropy.getShannonEntropy(bytes)); // 8.0 high entropy, but "far" from random
    }

    @Test
    public void testRandom() {

        RandomTool random = new RandomTool();
        int repeats = 1000;

        for (int length = 2; length < 20; length++) {
            double min = 8;
            double max = 0;
            double sum = 0;

            for (int i = 0; i < repeats; i++) {
                byte[] sample = random.getBytes(length);
                double entropy = Entropy.getShannonEntropy(sample);
                if (entropy > max) {
                    max = entropy;
                }
                if (entropy < min) {
                    min = entropy;
                }
                sum += entropy;
            }
            LOG.info("entropy for " + length + " random bytes: min=" + min + " max=" + max + " avg=" + sum / repeats);
        }
    }

}
