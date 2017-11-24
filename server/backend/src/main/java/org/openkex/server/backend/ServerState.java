/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.server.backend;

import org.openkex.tools.Validate;

/**
 * this trivial state machine is hard coded
 */
public class ServerState implements ServerScheduleApi {

    private long currentRound;
    private ServerScheduleApi.Phase currentPhase;

    public ServerState() {
        currentRound = BlockTool.INVALID_ROUND;
        currentPhase = ServerScheduleApi.Phase.INVALID;
    }

    public long getCurrentRound() {
        return currentRound;
    }

    public ServerScheduleApi.Phase getCurrentPhase() {
        return currentPhase;
    }

    @Override
    public void processRound(long roundNr, Phase phase) throws Exception {
        Validate.isTrue(roundNr != BlockTool.INVALID_ROUND);

        // validate round and phase sequence....

        // initial round
        if (currentRound == BlockTool.INVALID_ROUND) {
            Validate.isTrue(phase == Phase.INITIAL);
            currentPhase = Phase.INITIAL;
            currentRound = roundNr;
        }
        // non initial ("normal") round
        else {
            // same round, next sequence
            if (roundNr == currentRound) {
                // no further steps in initial phase.
                Validate.isTrue(currentPhase != Phase.INITIAL);
                // sequential phases
                Validate.isTrue(phase.getSequence() == currentPhase.getSequence() + 1);
                currentPhase = phase;
            }
            // next round: must start with PUBLISH_STATEMENTS
            else if (roundNr == currentRound + 1) {
                Validate.isTrue(phase == Phase.PUBLISH_STATEMENTS);
                // initial or last phase
                Validate.isTrue(currentPhase == Phase.INITIAL || currentPhase == Phase.FETCH_SIGNATURE);
                currentRound = roundNr;
                currentPhase = phase;
            }
            else {
                throw new RuntimeException("invalid round " + roundNr + " current " + currentRound);
            }
        }

    }
}
