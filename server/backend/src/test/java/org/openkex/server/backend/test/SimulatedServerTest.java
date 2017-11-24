/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.server.backend.test;

import org.junit.Assert;
import org.junit.Test;
import org.openkex.server.backend.ServerCore;
import org.openkex.server.backend.ServerScheduleApi;

public class SimulatedServerTest {

    @Test
    public void simpleTest() throws Exception {

        int count = 10;
        SimulatedServers servers = new SimulatedServers();
        servers.createDummyCores(count);
        Assert.assertEquals(count, servers.getAllServers().size());
        Assert.assertNull(servers.getById("srv_x"));
        Assert.assertNotNull(servers.getById(servers.getDummyServerId(3)));

        ServerCore core = servers.getDummyCore(servers.getDummyServerId(4));
        Assert.assertNotNull(core);

        long initialRound = 333;
        servers.processRound(initialRound, ServerScheduleApi.Phase.INITIAL);

        servers.processRound(initialRound + 1, ServerScheduleApi.Phase.PUBLISH_STATEMENTS);
        servers.processRound(initialRound + 1, ServerScheduleApi.Phase.VALIDATE);
        servers.processRound(initialRound + 1, ServerScheduleApi.Phase.PUBLISH_HASH);
        servers.processRound(initialRound + 1, ServerScheduleApi.Phase.CHECK_CONSENSUS);
        servers.processRound(initialRound + 1, ServerScheduleApi.Phase.PUBLISH_SIGNATURE);
        servers.processRound(initialRound + 1, ServerScheduleApi.Phase.FETCH_SIGNATURE);

        servers.processRound(initialRound + 2, ServerScheduleApi.Phase.PUBLISH_STATEMENTS);
    }
}
