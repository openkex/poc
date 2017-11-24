/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.keystore.api;

import org.openkex.dto.SignatureAlgorithm;

import java.util.List;

public interface KeyStore {

    /**
     * @return name of store vendor
     */
    String getVendorName();

    /**
     * @return vendor specific identifier of store (null if none)
     * @throws Exception if key store fails
     */
    String getVendorId() throws Exception;

    /**
     * @return number of key pair store can handle (Note: might depend on key algorithm).
     */
    int getKeyCapacity();

    /**
     * @return list of algorithms supported by key store
     */
    List<SignatureAlgorithm> getAlgorithms();

    /**
     * @return list of key ids present in store
     * @throws Exception if key store fails
     */
    List<String> getKeyIds() throws Exception;

    /**
     * get public key by id
     *
     * @param keyId id of key
     * @return public key, null if not found.
     * @throws Exception if key store fails
     */
    PublicKey getPublicKey(String keyId) throws Exception;

    /**
     * If key contains vendor prove validate it (e.g. the according key chain).
     * <p>
     * Note: this code does not require the key store to be present.
     *
     * @param key the key to verify
     * @return true if vendor prove was OK.
     */
    boolean verifyVendorKey(PublicKey key);

    /**
     * unlock the store (if required).
     *
     * @param pin pin or password to unlock store.
     * @return true if successful
     * @see LockType
     * @throws Exception if key store fails
     */
    boolean unlock(char[] pin) throws Exception;

    /**
     * lock the store
     *
     * @return true if succeeded
     * @throws Exception if key store fails
     */
    boolean lock() throws Exception;

    /**
     * @return lock type of store
     */
    LockType getLockType();

    /**
     * generate new key pair
     *
     * @param algorithm algorithm to use
     * @param keyId new key id
     * @return generated public key
     * @throws Exception if key id exists, if store is locked
     */
    PublicKey generateKey(SignatureAlgorithm algorithm, String keyId) throws Exception;

    /**
     * completely delete keystore.
     * <p>
     * use with care!
     *
     * @throws Exception if key id exists, if store is locked
     */
    void purge() throws Exception;

    /**
     * delete key pair
     *
     * @param keyId the key to delete
     * @return true if succeeded
     * @throws Exception if key store is locked
     */
    boolean deleteKey(String keyId) throws Exception;

    /**
     * sign message with specific key
     *
     * @param keyId the key to use
     * @param message the message to sign
     * @return signature
     * @throws Exception if key is not found, if store is locked
     */
    byte[] sign(String keyId, byte[] message) throws Exception;  // might require unlock

    /**
     * verify signature.
     * <p>
     * Note: verification requires only the public. this code does not require the key store to be present.
     *
     * @param keyId the key to use
     * @param message the message to verify
     * @param signature the signature to verify
     * @return true if signature is valid
     * @throws Exception if key is not found
     */
    boolean verify(String keyId, byte[] message, byte[] signature) throws Exception;

}
