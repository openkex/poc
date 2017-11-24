/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.server.backend;

public class BlockTool {

    // used to indicate "no round"
    public static final long INVALID_ROUND = 0;

    // unclear when to "start". 0 > 1.1.1970
    public static final long ROUND_ZERO_START = 0;

    // round time
    public static final long ROUND_INTERVAL_SECONDS = 60;

    private BlockTool() {
    }

    public static long getRoundNr(long seconds) {
        return (seconds - ROUND_ZERO_START) / ROUND_INTERVAL_SECONDS;
    }

    public static long getRoundStart(long roundNr) {
        return ROUND_ZERO_START + roundNr * ROUND_INTERVAL_SECONDS;
    }

}
