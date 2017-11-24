/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.server.backend;

import org.openkex.client.api.ClientApi;
import org.openkex.dto.SignedStatements;
import org.openkex.keystore.api.KeyStore;
import org.openkex.keystore.api.KeyStoreTool;
import org.openkex.keystore.api.PublicKey;
import org.openkex.serializer.SerializeService;
import org.openkex.server.api.ServerApi;
import org.openkex.tools.ByteArrayTool;
import org.openkex.tools.Hex;
import org.openkex.tools.NumberByteConverter;
import org.openkex.tools.Validate;
import org.openkex.tools.merkle.MerkleTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerCore implements ClientApi, ServerApi, ServerScheduleApi {

    public static final String KEY_ID = "serverKey";

    private static final String HASH_ALGORITHM = "SHA-256";

    private static final Logger LOG = LoggerFactory.getLogger(ServerCore.class);

    private ServerState state;

    private ServerRegistry registry;

    private StatementValidator validator;

    private SerializeService serializer;

    private ServerData ownData;
    private String logPrefix;

    // keystore for servers private key
    private KeyStore keyStore;

    private List<SignedStatements> collecting;
    private List<SignedStatements> collected;
    private List<SignedStatements> allStatements;

    // validated statements per kexId (dummy store!)
    private Map<Long, List<SignedStatements>> validStatements;

    private byte[] roundHash;
    private byte[] roundSignature;

    public ServerCore() {  // used in TomcatMain via HessianServlet, need rework.
        this.validStatements = Collections.synchronizedMap(new HashMap<>());
        this.logPrefix = "";
    }

    public ServerCore(ServerRegistry registry, ServerData serverData, KeyStore keyStore, SerializeService serializer) throws Exception {
        state = new ServerState();

        this.ownData = serverData;
        this.registry = registry;
        this.serializer = serializer;
        this.keyStore = keyStore;
        this.keyStore = keyStore;
        this.validator = new StatementValidatorImpl(this, serializer);

        this.validStatements = Collections.synchronizedMap(new HashMap<>());
        this.collecting = Collections.synchronizedList(new ArrayList<>());

        this.logPrefix = ownData == null ? "" : "sid=" + ownData.getServerId() + " ";
    }

    public void validateData() throws Exception {
        // validate: onwData exists in registry
        ServerData regData = registry.getById(ownData.getServerId());
        Validate.isTrue(regData.equals(ownData));

        // validate: keystore contains according public key
        PublicKey publicKey = keyStore.getPublicKey(KEY_ID);
        Validate.isTrue(publicKey.getAlgorithm().equals(regData.getAlgorithm()));
        Validate.isTrue(Arrays.equals(publicKey.getPublicKey(), regData.getKey()));
    }

    @Override
    public void processRound(long roundNr, Phase phase) throws Exception {
        LOG.info(logPrefix + "processRound roundNr=" + roundNr + " phase=" + phase);

        state.processRound(roundNr, phase);

        switch (phase) {
            case INITIAL:
                // just collect..
                break;
            case PUBLISH_STATEMENTS:
                processPhasePublishStatements();
                break;
            case VALIDATE:
                processPhaseValidate();
                break;
            case PUBLISH_HASH:
                // nothing to do, allow getHash()
                break;
            case CHECK_CONSENSUS:
                processPhaseConsensus();
                break;
            case PUBLISH_SIGNATURE:
                // nothing to do, allow getSignature()
                break;
            case FETCH_SIGNATURE:
                processPhaseFetchSignature();
                break;
            default:
                throw new RuntimeException("unknown phase: " + phase);
        }
    }

    private void processPhasePublishStatements() {
        // TODO: this must be checked for (client and server api) concurrency issues.

        // enable "newStatements()", publish "collected"
        this.collected = this.collecting;
        // reset for next round...
        this.collecting = Collections.synchronizedList(new ArrayList<>());
        // fetch from other servers via "newStatements()"
        // this phase cause network delays
    }

    private void processPhaseValidate() throws Exception {
        // fetch check and sort all statements (own and fetched)
        allStatements = new ArrayList<>();
        allStatements.addAll(collected);
        List<ServerData> servers = registry.getAllServers();
        // should be "Parallel"
        for (ServerData server : servers) {
            // skip own data
            if (server.getServerId().equals(ownData.getServerId())) {
                continue;
            }
            ServerApi api = registry.getProxy(server.getServerId());
            List<SignedStatements> serverStatements = api.newStatements(state.getCurrentRound());
            LOG.info(logPrefix + "received " + serverStatements.size() + " statements from server " + server.getServerId() +
                    " for round " + state.getCurrentRound());
            for (SignedStatements statements : serverStatements) {
                try {
                    validator.validate(statements);
                    // add valid statement
                    allStatements.add(statements);
                }
                catch (StatementException ve) {
                    // this should not happen
                    LOG.error(logPrefix + "got invalid statement from server " + server.getServerId(), ve);
                }
            }
        }
        // TODO: find collisions (e.g. identical claimKexId).

        LOG.info(logPrefix + "processing total " + allStatements.size() + " statements for round=" + state.getCurrentRound());

        if (allStatements.size() == 0) {
            roundHash = null;
            return;
        }
        // possibly a function getRootHash(statements)
        // get hashes
        ArrayList<byte[]> hashes = new ArrayList<>();
        MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
        for (SignedStatements statements : allStatements) {
            md.reset();
            md.update(serializer.serialize(statements));
            hashes.add(md.digest());
        }
        // sort hashes
        hashes.sort(ByteArrayTool::compareByteArray);

        // calculate proposed block hash
        MerkleTree tree = new MerkleTree(hashes, HASH_ALGORITHM);
        roundHash = tree.getRootHash();
    }

    private void processPhaseConsensus() throws Exception {
        if (allStatements.size() == 0) {
            roundSignature = null;
            return;
        }
        // fetch from other servers via "getHash()"
        // this phase cause network delays
        List<ServerData> servers = registry.getAllServers();
        int failed = 0;
        // should be "Parallel"
        for (ServerData server : servers) {
            // skip own data
            if (server.getServerId().equals(ownData.getServerId())) {
                continue;
            }
            ServerApi api = registry.getProxy(server.getServerId());
            byte[] serverHash = api.getHash(state.getCurrentRound());
            if (!Arrays.equals(roundHash, serverHash)) {
                LOG.warn(logPrefix + "server " + server.getServerId() + " has different hash: " + Hex.toString(serverHash));
                failed++;
            }
        }
        // currently allow single server
        if (failed == 0 || failed < servers.size() / 4) {
            // sign block hash
            roundSignature = keyStore.sign(KEY_ID, roundHash);
            LOG.info(logPrefix + "potential consensus OK. round=" + state.getCurrentRound());
        }
        else {
            // TODO
            roundSignature = null;
            LOG.warn(logPrefix + "consensus failed. round=" + state.getCurrentRound());
        }
    }

    private void processPhaseFetchSignature() throws Exception {
        if (roundSignature == null) {
            return;
        }
        // fetch from other servers via "getHash()"
        // this phase cause network delays
        List<ServerData> servers = registry.getAllServers();
        int failed = 0;
        // should be "Parallel"
        for (ServerData server : servers) {
            // skip own data
            if (server.getServerId().equals(ownData.getServerId())) {
                continue;
            }
            ServerApi api = registry.getProxy(server.getServerId());
            // check signature ...
            byte[] signature = api.getSignature(state.getCurrentRound());
            // this should be encapsulated properly

            boolean valid = KeyStoreTool.verify(server.getAlgorithm(), server.getKey(), roundHash, signature);
            if (!valid) {
                failed++;
                LOG.error("signature check failed. serverId=" + server.getServerId());
            }
        }
        if (failed == 0) {
            LOG.info(logPrefix + "consensus OK. round=" + state.getCurrentRound());
            // hack store..
            for (SignedStatements statement : allStatements) {
                long kexId = statement.getStatements().getIssuerKexId();
                List<SignedStatements> kexStatements = validStatements.computeIfAbsent(kexId, k -> new ArrayList<>());
                kexStatements.add(statement);
            }
        }

        if (failed > 0) {
            LOG.error(logPrefix + "consensus signatures failed. round=" + state.getCurrentRound());
        }
    }

    @Override
    public byte[] getSessionChallenge(long kexId) throws Exception {
        LOG.info(logPrefix + "getSessionChallenge kexId=" + NumberByteConverter.longToSixByteString(kexId));
        return Hex.fromString("CACACA");  // todo: client session
    }

    @Override
    public byte[] startKexSession(long kexId, byte[] signature) throws Exception {
        LOG.info(logPrefix + "startKexSession kexId=" + NumberByteConverter.longToSixByteString(kexId) +
                " signature=" + Hex.toString(signature));
        return Hex.fromString("D0D0D0"); // todo: client session
    }

    @Override
    public long submitStatement(SignedStatements statement, byte[] authToken) throws Exception {
        LOG.info(logPrefix + "submitStatement " + statement + " authToken=" + Hex.toString(authToken));
        // todo: validate client session
        Validate.notNull(authToken);
        Validate.isTrue(state.getCurrentPhase() != Phase.INVALID, "invalid phase.");

        validator.validate(statement);

        // collect for current round
        collecting.add(statement);
        // validation in next round, publish a round later
        return state.getCurrentRound() + 2;
    }

    @Override
    public List<SignedStatements> getStatements(long kexId) throws Exception {
        List<SignedStatements> kexStatements = getStatementsInternal(kexId);
        LOG.info(logPrefix + "getStatements kexId=" + NumberByteConverter.longToSixByteString(kexId) +
                " return " + (kexStatements == null ? "none" : kexStatements.size()));
        // LOG.debug("returning " + kexStatements);
        return kexStatements;
    }

    // no logging for internal calls
    public List<SignedStatements> getStatementsInternal(long kexId) throws Exception {
        return validStatements.get(kexId);
    }

    @Override
    public List<SignedStatements> newStatements(long roundNr) throws Exception {
        LOG.info(logPrefix + "newStatements roundNr=" + roundNr);
        Validate.isTrue(state.getCurrentPhase() != Phase.PUBLISH_STATEMENTS ||
                state.getCurrentPhase() != Phase.VALIDATE, "invalid phase.");
        Validate.isTrue(roundNr == state.getCurrentRound());
        return collected;
    }

    @Override
    public byte[] getHash(long roundNr) throws Exception {
        LOG.info(logPrefix + "getHash roundNr=" + roundNr);
        Validate.isTrue(state.getCurrentPhase() != Phase.PUBLISH_HASH ||
                state.getCurrentPhase() != Phase.CHECK_CONSENSUS, "invalid phase.");
        Validate.isTrue(roundNr == state.getCurrentRound());
        return roundHash;
    }

    @Override
    public byte[] getSignature(long roundNr) throws Exception {
        LOG.info(logPrefix + "getSignature roundNr=" + roundNr);
        Validate.isTrue(state.getCurrentPhase() != Phase.PUBLISH_SIGNATURE ||
                state.getCurrentPhase() != Phase.FETCH_SIGNATURE, "invalid phase.");
        return roundSignature;
    }

    @Override
    public List<SignedStatements> getConfirmedStatements(long roundNr) throws Exception {
        LOG.info(logPrefix + "getConfirmedStatements roundNr=" + roundNr);
        return null;
    }

}
