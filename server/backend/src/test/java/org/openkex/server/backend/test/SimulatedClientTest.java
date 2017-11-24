/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.server.backend.test;

import org.junit.Assert;
import org.junit.Test;
import org.openkex.serializer.ProtobufSerializer;
import org.openkex.serializer.SerializeService;
import org.openkex.server.backend.ServerCore;
import org.openkex.server.backend.ServerScheduleApi;
import org.openkex.tools.NumberByteConverter;
import org.openkex.tools.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SimulatedClientTest {

    private static final Logger LOG = LoggerFactory.getLogger(SimulatedClientTest.class);

    @Test
    public void simpleTest() throws Exception {

        String dummyServerId = "srv1";
        String dummyServerId2 = "srv2";
        int initialRound = 333;
        SimulatedServers servers = new SimulatedServers();
        ServerCore core = servers.addDummyCore(dummyServerId);
        ServerCore core2 = servers.addDummyCore(dummyServerId2);
        servers.processRound(initialRound, ServerScheduleApi.Phase.INITIAL);
        SerializeService serializer = new ProtobufSerializer();

        ArrayList<SimulatedClient> clients = new ArrayList<>();
        int clientCount = 3;
        for (int i = 0; i < clientCount; i++) {
            SimulatedClient client = new SimulatedClient(serializer, core, false);
            clients.add(client);
            LOG.info("created client with kexId=" + NumberByteConverter.longToSixByteString(client.getKexId()));
            client.login();
            Assert.assertEquals(initialRound + 2, client.sendInitialStatement());
        }

        servers.processRound(initialRound + 1, ServerScheduleApi.Phase.PUBLISH_STATEMENTS);
        servers.processRound(initialRound + 1, ServerScheduleApi.Phase.VALIDATE);
        servers.processRound(initialRound + 1, ServerScheduleApi.Phase.PUBLISH_HASH);
        servers.processRound(initialRound + 1, ServerScheduleApi.Phase.CHECK_CONSENSUS);
        servers.processRound(initialRound + 1, ServerScheduleApi.Phase.PUBLISH_SIGNATURE);
        servers.processRound(initialRound + 1, ServerScheduleApi.Phase.FETCH_SIGNATURE);

        servers.processRound(initialRound + 2, ServerScheduleApi.Phase.PUBLISH_STATEMENTS);

        for (SimulatedClient client : clients) {
            client.validateInitialStatement();
        }
    }

    @Test
    public void multiTest() throws Exception {
        multiTest(3, 9, 0, false);
    }

    @Test
    public void multiTestDummy() throws Exception {
        multiTest(2, 7, 0, true);
    }

    public static void multiTest(int serverCount, int clientCount, int worker, boolean dummySignature) throws Exception {

        Timer t = new Timer("simulate servers=" + serverCount + " clients=" + clientCount + " threads=" + worker,
                "create clients", true);

        SimulatedServers servers = new SimulatedServers();
        long initialRound = 55;
        servers.createDummyCores(serverCount);
        servers.processRound(initialRound, ServerScheduleApi.Phase.INITIAL, worker);
        SerializeService serializer = new ProtobufSerializer();

        List<SimulatedClient> clients = Collections.synchronizedList(new ArrayList<SimulatedClient>());
        if (worker == 0) {
            for (int i = 0; i < clientCount; i++) {
                int serverNr = i % serverCount;  // round robin
                createClientInternal(servers, initialRound, serializer, clients, serverNr, dummySignature);
            }
        }
        else {
            ExecutorService executor = Executors.newFixedThreadPool(worker);
            for (int i = 0; i < clientCount; i++) {
                int serverNr = i % serverCount;  // round robin
                Runner runner = new Runner(servers, initialRound, serializer, clients, serverNr, dummySignature);
                executor.execute(runner);
            }
            // wait until all clients are complete
            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.HOURS);
        }

        t.split("PUBLISH_STATEMENTS", true);
        servers.processRound(initialRound + 1, ServerScheduleApi.Phase.PUBLISH_STATEMENTS, worker);
        t.split("VALIDATE", true);
        servers.processRound(initialRound + 1, ServerScheduleApi.Phase.VALIDATE, worker);
        t.split("PUBLISH_HASH", true);
        servers.processRound(initialRound + 1, ServerScheduleApi.Phase.PUBLISH_HASH, worker);
        t.split("CHECK_CONSENSUS", true);
        servers.processRound(initialRound + 1, ServerScheduleApi.Phase.CHECK_CONSENSUS, worker);
        t.split("PUBLISH_SIGNATURE", true);
        servers.processRound(initialRound + 1, ServerScheduleApi.Phase.PUBLISH_SIGNATURE, worker);
        t.split("FETCH_SIGNATURE", true);
        servers.processRound(initialRound + 1, ServerScheduleApi.Phase.FETCH_SIGNATURE, worker);
        t.split("PUBLISH_STATEMENTS", true);
        servers.processRound(initialRound + 2, ServerScheduleApi.Phase.PUBLISH_STATEMENTS, worker);

        t.split("get statements", true);
        for (SimulatedClient client : clients) {
            client.validateInitialStatement();
        }
        t.stop(true);
    }

    // split function for multi threaded usage
    private static void createClientInternal(SimulatedServers servers, long initialRound, SerializeService serializer,
                                             List<SimulatedClient> clients, int serverNr, boolean dummySignature) throws Exception {
        SimulatedClient client = new SimulatedClient(serializer, servers.getDummyCore(serverNr), dummySignature);
        clients.add(client);
        LOG.info("created client with kexId=" + NumberByteConverter.longToSixByteString(client.getKexId()));
        client.login();
        Assert.assertEquals(initialRound + 2, client.sendInitialStatement());
    }

    private static class Runner implements Runnable {
        private SimulatedServers servers;
        private long initialRound;
        private SerializeService serializer;
        private List<SimulatedClient> clients;
        private int serverNr;
        private boolean dummySignature;

        Runner(SimulatedServers servers, long initialRound, SerializeService serializer,
               List<SimulatedClient> clients, int serverNr, boolean dummySignature) {
            this.servers = servers;
            this.initialRound = initialRound;
            this.serializer = serializer;
            this.clients = clients;
            this.serverNr = serverNr;
            this.dummySignature = dummySignature;
        }

        @Override
        public void run() {
            try {
                createClientInternal(servers, initialRound, serializer, clients, serverNr, dummySignature);
            }
            catch (Exception e) {
                LOG.error("runner failed. ", e);
            }
        }
    }

}
