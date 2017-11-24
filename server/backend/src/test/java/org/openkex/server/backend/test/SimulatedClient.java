/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.server.backend.test;

import org.openkex.client.api.ClientApi;
import org.openkex.dto.SignatureAlgorithm;
import org.openkex.dto.SignedStatements;
import org.openkex.dto.StatementClaimKexId;
import org.openkex.dto.Statements;
import org.openkex.keystore.api.KeyStore;
import org.openkex.keystore.memory.JavaMemoryDummyKeyStore;
import org.openkex.keystore.memory.JavaMemoryKeyStore;
import org.openkex.serializer.SerializeService;
import org.openkex.server.backend.BlockTool;
import org.openkex.tools.Hex;
import org.openkex.tools.NumberByteConverter;
import org.openkex.tools.RandomCheck;
import org.openkex.tools.RandomTool;
import org.openkex.tools.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * class to simulate a test client
 */
public class SimulatedClient {

    private static final Logger LOG = LoggerFactory.getLogger(SimulatedClient.class);

    private static final String CLIENT_KEY_ID = "firstClientKey";

    private long kexId;
    private KeyStore keyStore;
    private SignatureAlgorithm algorithm;
    private ArrayList<SignedStatements> pending;
    private ArrayList<SignedStatements> confirmed;
    private SerializeService serializeService;
    private ClientApi clientApi;

    private byte[] token;

    // create Random client...
    public SimulatedClient(SerializeService serializeService, ClientApi clientApi, boolean dummySignature) throws Exception {

        this.serializeService = serializeService;
        this.clientApi = clientApi;

        // random kexId
        byte[] kexIdBytes = null;
        int rep = 0;
        do {
            kexIdBytes = new RandomTool().getBytes(6);
            rep++;
        }
        while (!RandomCheck.estimateRandom(kexIdBytes));  // check for rules...
//        if (rep > 1) {
//            LOG.warn("required multiple tries for sufficient random: " + rep);
//        }

        kexId = NumberByteConverter.sixBytesToLong(kexIdBytes);

        // create initial key
        keyStore = dummySignature ? new JavaMemoryDummyKeyStore() : new JavaMemoryKeyStore();
        algorithm = dummySignature ? SignatureAlgorithm.DUMMY : SignatureAlgorithm.ECDSA_SECP256K1;
        keyStore.generateKey(algorithm, CLIENT_KEY_ID);
        pending = new ArrayList<>();
        confirmed = new ArrayList<>();
    }

    public void login() throws Exception {
        byte[] challenge = clientApi.getSessionChallenge(kexId);
        byte[] signature2 = Hex.fromString("AEAEAE" + Hex.toString(challenge));
        token = clientApi.startKexSession(kexId, signature2);
    }

    public long sendInitialStatement() throws Exception {
        // create initial statement
        StatementClaimKexId statement = new StatementClaimKexId(
                algorithm,
                keyStore.getPublicKey(CLIENT_KEY_ID).getPublicKey()
        );
        Statements statements = new Statements(
                kexId,
                0, // todo date
                null,
                BlockTool.INVALID_ROUND,
                statement,
                null,
                null
        );

        byte[] statementBytes = serializeService.serialize(statements);
        byte[] signature = keyStore.sign(CLIENT_KEY_ID, statementBytes);
        SignedStatements signedStatements = new SignedStatements(
                statements,
                signature
        );
        return clientApi.submitStatement(signedStatements, token);
    }

    public void validateInitialStatement() throws Exception {
        List<SignedStatements> list = clientApi.getStatements(kexId);
        Validate.isTrue(list != null);
        Validate.isTrue(list.size() == 1);
        Statements statements = list.get(0).getStatements();
        Validate.isTrue(statements.getClaimKexId() != null);
        Validate.isTrue(statements.getIssuerKexId() == kexId);
    }

    public long getKexId() {
        return kexId;
    }
}
