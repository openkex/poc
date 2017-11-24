/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.keystore.test;

import org.junit.Assert;
import org.junit.Test;
import org.openkex.dto.SignatureAlgorithm;
import org.openkex.keystore.api.KeyStore;
import org.openkex.keystore.api.KeyStoreTool;
import org.openkex.keystore.api.LockType;
import org.openkex.keystore.api.PublicKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class KeyStoreTest {

    protected static final Logger LOG = LoggerFactory.getLogger(KeyStoreTest.class);

    protected abstract KeyStore getKeystore() throws Exception;

    /**
     * override to handle correct unlocking.<br>
     * will be called before <code>generateKey</code> and <code>sign</code>
     */
    protected void unlock(KeyStore keystore) throws Exception {
        // empty default implementation
    }

    /**
     * override to skip specific tests that fail with dummy
     * @return true if dummy
     */
    protected boolean isDummy() {
        return false;
    }

    private SignatureAlgorithm getAlgorithm() {
        return isDummy() ? SignatureAlgorithm.DUMMY : SignatureAlgorithm.ECDSA_SECP256K1;
    }

    @Test
    public void testName() throws Exception {
        KeyStore store = getKeystore();
        LOG.info("store vendor name=" + store.getVendorName());
        LOG.info("store vendor id=" + store.getVendorId());
    }

    @Test
    public void testSimple() throws Exception {
        KeyStore store = getKeystore();

        unlock(store);  // need to unlock for read in case of JksKeyStore

        String key1 = "key1";
        String key2 = "key2";
        Assert.assertNull(store.getPublicKey(key1));
        Assert.assertNull(store.getPublicKey(key2));

        PublicKey key = store.generateKey(getAlgorithm(), key1);

        if (!isDummy()) {
            Assert.assertTrue(key.getPublicKey().length == 33); // compressed ECC 256, 1 byte header + 256 bit (32 byte) key
        }
        Assert.assertNull(store.getPublicKey(key2));

        byte[] message = "Hello KeyStore".getBytes("US-ASCII");

        unlock(store);
        byte[] signature = store.sign(key1, message);

        Assert.assertNotNull(signature);
        if (!isDummy()) {
            Assert.assertTrue(signature.length == 64); // 512 bit (64 byte) raw signature
        }
        boolean valid = store.verify(key1, message, signature);
        Assert.assertTrue(valid);

        boolean validTool = KeyStoreTool.verify(getAlgorithm(), key.getPublicKey(), message, signature);
        Assert.assertTrue(validTool);

        byte[] signatureBad = signature.clone();
        signatureBad[9] ^= 8;  // change one bit

        boolean validBadSignature = store.verify(key1, message, signatureBad);
        Assert.assertFalse(validBadSignature);

        byte[] messageBad = message.clone();
        messageBad[3] ^= 16;  // change one bit
        boolean validBadMessage = store.verify(key1, messageBad, signature);
        Assert.assertFalse(validBadMessage);

        Assert.assertTrue(store.deleteKey(key1));
        Assert.assertNull(store.getPublicKey(key1));
    }

    @Test
    public void testLock() throws Exception {
        KeyStore store = getKeystore();
        if (store.getLockType() == LockType.NOT_LOCKED) {
            return; // nothing to test
        }
        if (store.getLockType() == LockType.UNLOCK_EXTERNAL) {
            return; // no automatic test possible
        }
        if (store.getLockType() == LockType.UNLOCK_ONCE) {
            return; // TODO...
        }
        if (store.getLockType() == LockType.UNLOCK_EACH) {
            return; // TODO...
        }
    }

    @Test
    public void testMultiKey() throws Exception {
        KeyStore store = getKeystore();

        String key1 = "key1a";
        String key2 = "key2a";
        String key3 = "key3a";
        String key4 = "key4a";

        unlock(store);
        store.generateKey(getAlgorithm(), key1);
        store.generateKey(getAlgorithm(), key2);

        Assert.assertFalse(store.deleteKey(key3));  // cannot delete key3

        store.generateKey(getAlgorithm(), key3);
        Assert.assertEquals(3, store.getKeyIds().size());

        Assert.assertTrue(store.deleteKey(key2));
        Assert.assertEquals(2, store.getKeyIds().size());

        store.generateKey(getAlgorithm(), key4);
        Assert.assertEquals(3, store.getKeyIds().size());

        // delete all...
        Assert.assertTrue(store.deleteKey(key1));
        Assert.assertTrue(store.deleteKey(key3));
        Assert.assertTrue(store.deleteKey(key4));
        Assert.assertEquals(0, store.getKeyIds().size());
    }

}
