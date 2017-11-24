/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.tools;

/**
 * see https://en.wikipedia.org/wiki/Entropy_(information_theory)
 * see https://rosettacode.org/wiki/Entropy
 */
public class Entropy {

    private Entropy() {
    }

    /**
     * get shannon entropy in bits per symbol (i.e. byte).
     * <p>
     * result will be in range of 0-8.
     *
     * @param bytes bytes to analyze
     * @return entropy value
     */
    public static double getShannonEntropy(byte[] bytes) {

        int[] counts = new int[256];  // count for each byte value

        for (byte b : bytes) {
            counts[b & 0xFF]++;
        }

        double e = 0.0;
        for (int i = 0; i < 256; i++) {
            if (counts[i] == 0) {  // non existing "symbol" (avoid "zero * infinity" later)
                continue;
            }
            double p = 1.0 * counts[i] / bytes.length;
            e += p * Math.log(p) / Math.log(2);
        }
        return Math.abs(-e);  // get rid of "-0.0"?
    }
}
