/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.tools;

import java.util.Random;

public class RandomTool {

    private Random rand;

    /**
     * create new generator (random seed)
     */
    public RandomTool() {
        rand = new Random();
    }

    /**
     * create new generator with fixed seed
     *
     * @param seed seed for random
     */
    public RandomTool(long seed) {
        rand = new Random(seed);
    }

    /**
     * get a random byte array
     *
     * @param count number of bytes
     * @return the random bytes
     */
    public byte[] getBytes(int count) {
        byte[] ret = new byte[count];
        rand.nextBytes(ret);
        return ret;
    }

}