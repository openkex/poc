/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.server.backend.test;

import org.junit.Assert;
import org.junit.Test;
import org.openkex.server.backend.ServerScheduleApi;
import org.openkex.server.backend.ServerState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerStateTest {

    private static final Logger LOG = LoggerFactory.getLogger(ServerStateTest.class);

    @Test
    public void testOK() throws Exception {
        ServerState state = new ServerState();
        long start = 333;
        state.processRound(start, ServerScheduleApi.Phase.INITIAL);
        for (int i = 1; i < 4; i++) {
            state.processRound(start + i, ServerScheduleApi.Phase.PUBLISH_STATEMENTS);
            state.processRound(start + i, ServerScheduleApi.Phase.VALIDATE);
            state.processRound(start + i, ServerScheduleApi.Phase.PUBLISH_HASH);
            state.processRound(start + i, ServerScheduleApi.Phase.CHECK_CONSENSUS);
            state.processRound(start + i, ServerScheduleApi.Phase.PUBLISH_SIGNATURE);
            state.processRound(start + i, ServerScheduleApi.Phase.FETCH_SIGNATURE);
        }
    }

    @Test
    public void testFail() throws Exception {
        ServerState state = new ServerState();
        long start = 3333;
        // must not start with wrong phase
        testFail(state, start, ServerScheduleApi.Phase.PUBLISH_STATEMENTS);
        testFail(state, start, ServerScheduleApi.Phase.VALIDATE);
        testFail(state, start, ServerScheduleApi.Phase.PUBLISH_HASH);
        testFail(state, start, ServerScheduleApi.Phase.CHECK_CONSENSUS);

        // OK start
        state.processRound(start, ServerScheduleApi.Phase.INITIAL);
        // must not proceed in initial phase
        testFail(state, start, ServerScheduleApi.Phase.PUBLISH_STATEMENTS);

        // proceed correctly
        state.processRound(start + 1, ServerScheduleApi.Phase.PUBLISH_STATEMENTS);
        // must not proceed early
        testFail(state, start + 2, ServerScheduleApi.Phase.PUBLISH_STATEMENTS);

        // proceed correctly
        state.processRound(start + 1, ServerScheduleApi.Phase.VALIDATE);
        // must not step back
        testFail(state, start + 1, ServerScheduleApi.Phase.PUBLISH_STATEMENTS);
        testFail(state, start, ServerScheduleApi.Phase.PUBLISH_STATEMENTS);

    }

    private void testFail(ServerState state, long round, ServerScheduleApi.Phase phase) {
        try {
            state.processRound(round, phase);
            Assert.fail();
        }
        catch (Exception e) {
            LOG.info("expected exception: " + e);
        }
    }
}
