/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.keystore.memory;

import org.openkex.dto.SignatureAlgorithm;
import org.openkex.keystore.api.KeyStore;
import org.openkex.keystore.api.LockType;
import org.openkex.keystore.api.PublicKey;
import org.openkex.tools.Validate;
import org.openkex.tools.crypto.ECCTool;

import java.security.KeyPair;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * memory based key store for testing purpose.
 */
public class JavaMemoryKeyStore implements KeyStore {

    private ArrayList<SignatureAlgorithm> algorithms;

    private HashMap<String, KeyPair> keyPairs;

    private String curveName = SignatureAlgorithm.ECDSA_SECP256K1.getIdentifier();

    public JavaMemoryKeyStore() {
        keyPairs = new HashMap<>();
        algorithms = new ArrayList<>();
        // currently only one algorithm ...
        algorithms.add(SignatureAlgorithm.ECDSA_SECP256K1);
    }

    public String getVendorName() {
        return "JavaMemoryKeyStore (Testing Only)";
    }

    public String getVendorId() {
        return null;
    }

    public int getKeyCapacity() {
        return 1000; // just "enough".
    }

    public List<SignatureAlgorithm> getAlgorithms() {
        return algorithms;
    }

    public List<String> getKeyIds() {
        return new ArrayList<>(keyPairs.keySet()); // think about sorting.
    }

    public PublicKey getPublicKey(String keyId) throws Exception {
        KeyPair pair = keyPairs.get(keyId);
        if (pair == null) {
            return null;
        }
        return convertToPublicKey(pair, keyId);
    }

    public boolean verifyVendorKey(PublicKey key) {
        throw new RuntimeException("verifyVendorKey not implemented.");
    }

    public boolean unlock(char[] pin) {
        throw new RuntimeException("unlock not implemented.");
    }

    public boolean lock() {
        throw new RuntimeException("lock not implemented.");
    }

    public LockType getLockType() {
        return LockType.NOT_LOCKED;
    }

    public PublicKey generateKey(SignatureAlgorithm algorithm, String keyId) throws Exception {
        if (algorithm != SignatureAlgorithm.ECDSA_SECP256K1) {
            throw new RuntimeException("unsupported algorithm. " + algorithm);
        }
        if (keyPairs.get(keyId) != null) {
            throw new Exception("key already exists. keyId=" + keyId);
        }
        KeyPair pair = ECCTool.generate(curveName);
        keyPairs.put(keyId, pair);
        return convertToPublicKey(pair, keyId);
    }

    public void purge() {
        keyPairs = new HashMap<>();
    }

    public boolean deleteKey(String keyId) throws Exception {
        KeyPair pair = keyPairs.get(keyId);
        if (pair == null) {
            return false;
        }
        Validate.notNull(keyPairs.remove(keyId));
        return true;
    }

    public byte[] sign(String keyId, byte[] message) throws Exception {
        KeyPair pair = keyPairs.get(keyId);
        if (pair == null) {
            throw new RuntimeException("found no key with id: " + keyId);
        }
        // return raw signature
        return ECCTool.decodeDerSignature(ECCTool.sign(message, pair.getPrivate()));
    }

    public boolean verify(String keyId, byte[] message, byte[] signature) throws Exception {
        KeyPair pair = keyPairs.get(keyId);
        if (pair == null) {
            throw new RuntimeException("found no key with id: " + keyId);
        }
        return ECCTool.verify(message, ECCTool.encodeRawSignature(signature), pair.getPublic());
    }

    private PublicKey convertToPublicKey(KeyPair pair, String id) throws Exception {
        // use compressed key
        byte[] key = ECCTool.encodeCurvePoint(pair.getPublic(), curveName, true);
        return new PublicKey(id, SignatureAlgorithm.ECDSA_SECP256K1, key);
    }

}
