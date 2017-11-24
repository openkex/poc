/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.server.backend.test;

import org.openkex.dto.SignatureAlgorithm;
import org.openkex.keystore.api.KeyStore;
import org.openkex.keystore.api.PublicKey;
import org.openkex.keystore.memory.JavaMemoryKeyStore;
import org.openkex.serializer.ProtobufSerializer;
import org.openkex.serializer.SerializeService;
import org.openkex.server.api.ServerApi;
import org.openkex.server.backend.ServerCore;
import org.openkex.server.backend.ServerData;
import org.openkex.server.backend.ServerRegistry;
import org.openkex.server.backend.ServerScheduleApi;
import org.openkex.tools.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * helper class to simulate set of servers
 */
public class SimulatedServers implements ServerRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(SimulatedClientTest.class);

    private static final String PREFIX = "srv_";

    private HashMap<String, KeyStore> serverKeys;
    private HashMap<String, ServerCore> serverCores;
    private HashMap<String, ServerData> servers;

    private SerializeService serializer;

    public SimulatedServers() {
        this.serializer = new ProtobufSerializer();
        this.serverKeys = new HashMap<>();
        this.serverCores = new HashMap<>();
        servers = new HashMap<>();
    }

    @Override
    public List<ServerData> getAllServers() {
        return new ArrayList<>(servers.values());
    }

    @Override
    public ServerData getById(String serverId) {
        return servers.get(serverId);
    }

    @Override
    public ServerApi getProxy(String serverId) {
        return serverCores.get(serverId);
    }

    public ServerData addServer(String serverId) throws Exception {
        Validate.isTrue(getById(serverId) == null);

        KeyStore store = new JavaMemoryKeyStore();
        PublicKey key = store.generateKey(SignatureAlgorithm.ECDSA_SECP256K1, ServerCore.KEY_ID);
        String url = "https://" + serverId + "com";
        String operator = "operator of" + serverId;
        ServerData data = new ServerData(serverId, url, operator,
                SignatureAlgorithm.ECDSA_SECP256K1, key.getPublicKey());
        // validate that entry is not yet existing
        Validate.isTrue(servers.put(serverId, data) == null);
        serverKeys.put(serverId, store);
        return data;
    }

    public KeyStore getKeyStore(String serverId) {
        return serverKeys.get(serverId);
    }

    public ServerCore addDummyCore(String serverId) throws Exception {
        addServer(serverId);

        ServerCore core = new ServerCore(this, getById(serverId), serverKeys.get(serverId), serializer);
        core.validateData();

        serverCores.put(serverId, core);
        return core;
    }

    public void createDummyCores(int serverCount) throws Exception {
        for (int i = 0; i < serverCount; i++) {
            addDummyCore(getDummyServerId(i));
        }
    }

    public String getDummyServerId(int number) {
        return PREFIX + String.format("%03d", number);
    }

    public ServerCore getDummyCore(int number) {
        return getDummyCore(getDummyServerId(number));
    }

    public ServerCore getDummyCore(String serverId) {
        return serverCores.get(serverId);
    }

    public void processRound(long roundNr, ServerScheduleApi.Phase phase) throws Exception {
        processRound(roundNr, phase, 0);
    }

    public void processRound(long roundNr, ServerScheduleApi.Phase phase, int workers) throws Exception {
        // no pool.
        if (workers == 0) {
            for (ServerCore core : serverCores.values()) {
                core.processRound(roundNr, phase);
            }
        }
        else {
            ExecutorService executor = Executors.newFixedThreadPool(workers);
            for (ServerCore core : serverCores.values()) {
                Runner runner = new Runner(core, roundNr, phase);
                executor.execute(runner);
            }
            // wait until all servers complete phase
            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.HOURS);
        }
    }

    private static class Runner implements Runnable {
        private ServerCore core;
        private long round;
        private ServerScheduleApi.Phase phase;

        Runner(ServerCore core, long round, ServerScheduleApi.Phase phase) {
            this.core = core;
            this.round = round;
            this.phase = phase;
        }

        @Override
        public void run() {
            try {
                core.processRound(round, phase);
            }
            catch (Exception e) {
                LOG.error("worker failed. ", e);
            }
        }
    }
}
