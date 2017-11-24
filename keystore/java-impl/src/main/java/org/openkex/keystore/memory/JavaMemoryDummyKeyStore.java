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

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * memory based key store for testing purpose. it uses only hashes as pseudo keys to speed up operation.
 * <p>
 * this is for TESTING ONLY
 */
public class JavaMemoryDummyKeyStore implements KeyStore {

    // short and fast hash for dummy
    private static final String HASH_ALGORITHM = "MD5";

    private ArrayList<SignatureAlgorithm> algorithms;

    private HashMap<String, PublicKey> dummyKeys;

    public JavaMemoryDummyKeyStore() {
        dummyKeys = new HashMap<>();
        algorithms = new ArrayList<>();
        // currently only one algorithm ...
        algorithms.add(SignatureAlgorithm.DUMMY);
    }

    public String getVendorName() {
        return "JavaMemoryDummyKeyStore (Testing Only)";
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
        return new ArrayList<>(dummyKeys.keySet());
    }

    public PublicKey getPublicKey(String keyId) throws Exception {
        return dummyKeys.get(keyId);
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
        if (algorithm != SignatureAlgorithm.DUMMY) {
            throw new RuntimeException("unsupported algorithm. " + algorithm);
        }
        if (dummyKeys.get(keyId) != null) {
            throw new Exception("key already exists. keyId=" + keyId);
        }
        // use random fake "key"
        byte[] key = new byte[16];
        new Random().nextBytes(key);
        PublicKey dummyKey = new PublicKey(keyId, algorithm, key);
        dummyKeys.put(keyId, dummyKey);
        return dummyKey;
    }

    public void purge() {
        dummyKeys = new HashMap<>();
    }

    public boolean deleteKey(String keyId) throws Exception {
        PublicKey key = dummyKeys.get(keyId);
        if (key == null) {
            return false;
        }
        Validate.notNull(dummyKeys.remove(keyId));
        return true;
    }

    public byte[] sign(String keyId, byte[] message) throws Exception {
        PublicKey key = dummyKeys.get(keyId);
        if (key == null) {
            throw new RuntimeException("found no key with id: " + keyId);
        }
        return getDummySignature(key, message);
    }

    private byte[] getDummySignature(PublicKey key, byte[] message) throws Exception {
        MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
        md.update(key.getPublicKey());
        md.update(message);
        return md.digest();
    }

    public boolean verify(String keyId, byte[] message, byte[] signature) throws Exception {
        PublicKey key = dummyKeys.get(keyId);
        if (key == null) {
            throw new RuntimeException("found no key with id: " + keyId);
        }
        byte[] dummySignature = getDummySignature(key, message);
        return Arrays.equals(dummySignature, signature);
    }

}
