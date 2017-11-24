/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.keystore.smartcardhsm;

import org.openkex.cardio.common.TerminalTool;
import org.openkex.cardio.smartcardhsm.SmartCardHsm;
import org.openkex.dto.SignatureAlgorithm;
import org.openkex.keystore.api.KeyStore;
import org.openkex.keystore.api.LockType;
import org.openkex.keystore.api.PublicKey;
import org.openkex.tools.Hex;
import org.openkex.tools.crypto.ECCTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.smartcardio.Card;
import javax.smartcardio.CardTerminal;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * keystore wrapper fort SmartCardHsm
 */
public class SmartCardHsmKeyStore implements KeyStore {

    private static final Logger LOG = LoggerFactory.getLogger(SmartCardHsmKeyStore.class);

    private static final int KEY_COUNT = 30; // tested limit with ECC 256

    private static final String DIGEST = "SHA-256"; // TODO: check with ECDSA?

    // this file id has public read and authenticated write
    private static final byte[] FID_INDEX = Hex.fromString("3400");

    // index in array is internal Id, value is external keyId, empty String means unused (after delete).
    private String[] keyIds;

    private ArrayList<SignatureAlgorithm> algorithms;

    private CardTerminal terminal;
    private Card card;
    private SmartCardHsm smartCardHsm;

    public SmartCardHsmKeyStore() throws Exception {
        algorithms = new ArrayList<>();
        // currently only one algorithm ...
        algorithms.add(SignatureAlgorithm.ECDSA_SECP256K1);

        // setup terminal.
        TerminalTool terminalTool = new TerminalTool();
        if (terminalTool.getStatus() != TerminalTool.Status.OK) {
            throw new RuntimeException("could not get terminal. status=" + terminalTool.getStatus());
        }
        terminal = terminalTool.get();
        LOG.info("using terminal: " + terminal.getName());

        if (!terminal.isCardPresent()) {
            throw new RuntimeException("no card present.");
        }
        connect();
        keyIds = KeyIndexTool.split(smartCardHsm.readFile(FID_INDEX));
        LOG.info("found key Ids:" + Arrays.asList(keyIds));
    }

    private void connect() throws Exception {
        card = terminal.connect("*");
        byte[] atr = card.getATR().getBytes();
        LOG.info("Connect: found card with ATR=" + Hex.toString(atr));

        if (!SmartCardHsm.matchAtr(atr)) {
            throw new RuntimeException("Card is not a SmartCard HSM");
        }
        smartCardHsm = new SmartCardHsm(card.getBasicChannel());
        if (!smartCardHsm.isInitialized()) {
            throw new RuntimeException("card not initialized.");
        }
    }

    private void disconnect() throws Exception {
        LOG.info("disconnect");
        if (smartCardHsm != null) {
            smartCardHsm.close();
        }
        card.disconnect(true);
        smartCardHsm = null;
    }

    public String getVendorName() {
        return "SmartCardHSM";
    }

    public String getVendorId() throws Exception {
        connect(); // connect for each operation. slow but safe.
        String id = smartCardHsm.getCHR();
        disconnect();
        return id;
    }

    public int getKeyCapacity() {
        return KEY_COUNT;
    }

    public List<SignatureAlgorithm> getAlgorithms() {
        return algorithms;
    }

    public List<String> getKeyIds() throws Exception {
        ArrayList<String> list = new ArrayList<>();
        for (String keyId : keyIds) {
            if (keyId.length() > 0) {  // skip empty strings
                list.add(keyId);
            }
        }
        return list;
    }

    public void purge() throws Exception {
        // delete index file
        smartCardHsm.deleteFile(FID_INDEX);
        for (int i = 1; i <= KEY_COUNT; i++) {
            try {
                smartCardHsm.deleteFile(new byte[]{(byte) 0xCC, (byte) i});
                smartCardHsm.deleteFile(new byte[]{(byte) 0xCE, (byte) i});
            }
            catch (Exception e) {
                LOG.warn("failed to delete key: " + i + " ex=" + e);
            }
        }
    }

    public PublicKey getPublicKey(String keyId) throws Exception {
        int id = KeyIndexTool.getIndex(keyIds, keyId);
        if (id == -1) {
            return null;
        }
        byte[] key = smartCardHsm.getPublicKey(id + 1).getContent();
        byte[] shortKey = ECCTool.compressCurvePoint(key, SignatureAlgorithm.ECDSA_SECP256K1.getIdentifier());
        // todo: vendor prove
        return new PublicKey(keyId, SignatureAlgorithm.ECDSA_SECP256K1, shortKey);

    }

    public boolean verifyVendorKey(PublicKey key) {
        throw new RuntimeException("verifyVendorKey not implemented.");
    }

    public boolean unlock(char[] pin) throws Exception {
        byte[] pwdTmp = new byte[pin.length];
        for (int i = 0; i < pin.length; i++) {
            pwdTmp[i] = (byte) pin[i];  // this is rough but ok for numeric characters
        }
        boolean result = smartCardHsm.verifyPIN(pwdTmp) == -1;
        Arrays.fill(pwdTmp, (byte) 0); // wipe temp memory
        return result;
    }

    public boolean lock() throws Exception {
        disconnect();
        return true;
    }

    public LockType getLockType() {
        return LockType.NOT_LOCKED;
    }

    public PublicKey generateKey(SignatureAlgorithm algorithm, String keyId) throws Exception {
        if (algorithm != SignatureAlgorithm.ECDSA_SECP256K1) {
            throw new RuntimeException("unsupported algorithm: " + algorithm);
        }
        int id = KeyIndexTool.getIndex(keyIds, keyId);
        if (id != -1) {
            throw new Exception("key already exists. keyId=" + keyId);
        }
        keyIds = KeyIndexTool.addValue(keyIds, keyId);
        // update id
        id = KeyIndexTool.getIndex(keyIds, keyId);
        writeKeyIndex();
        smartCardHsm.generatePair(SignatureAlgorithm.ECDSA_SECP256K1.getIdentifier(), id + 1, 0);
        return getPublicKey(keyId);
    }

    public boolean deleteKey(String keyId) throws Exception {
        int id = KeyIndexTool.getIndex(keyIds, keyId);
        if (id == -1) {
            return false;
        }
        keyIds[id] = "";  // could shrink array if last entry was removed....
        smartCardHsm.deleteKey(id + 1);
        writeKeyIndex();
        return true;
    }

    private void writeKeyIndex() throws Exception {
        smartCardHsm.deleteFile(FID_INDEX);
        smartCardHsm.updateFile(FID_INDEX, KeyIndexTool.join(keyIds));
    }

    public byte[] sign(String keyId, byte[] message) throws Exception {
        int id = KeyIndexTool.getIndex(keyIds, keyId);
        if (id == -1) {
            throw new Exception("keyId unknown. " + keyId);
        }
        byte[] messageHash = MessageDigest.getInstance(DIGEST).digest(message);
        return ECCTool.decodeDerSignature(smartCardHsm.sign(messageHash, id + 1));
    }

    public boolean verify(String keyId, byte[] message, byte[] signature) throws Exception {
        PublicKey key = getPublicKey(keyId);
        java.security.PublicKey javaKey = ECCTool.getPublicKeyFromCurvePoint(key.getPublicKey(), SignatureAlgorithm.ECDSA_SECP256K1.getIdentifier());
        return ECCTool.verify(message, ECCTool.encodeRawSignature(signature), javaKey);
    }

}
