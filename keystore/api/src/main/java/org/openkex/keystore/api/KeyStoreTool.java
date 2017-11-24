/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.keystore.api;

import org.openkex.dto.SignatureAlgorithm;
import org.openkex.tools.crypto.ECCTool;

import java.security.MessageDigest;
import java.security.PublicKey;
import java.util.Arrays;

/**
 * Helper class for KeyStore
 */
public class KeyStoreTool {

    // avoid instances
    private KeyStoreTool() {
    }

    /**
     * verify signature.
     *
     * @param algorithm the key algorithm
     * @param key the public key
     * @param message the message to verify
     * @param signature the signature to verify
     * @return true if signature is valid
     * @throws Exception in case of encoding problems
     */
    public static boolean verify(SignatureAlgorithm algorithm, byte[] key, byte[] message, byte[] signature) throws Exception {
        if (algorithm == SignatureAlgorithm.ECDSA_SECP256K1) {
            PublicKey publicKey = ECCTool.getPublicKeyFromCurvePoint(key, algorithm.getIdentifier());
            return ECCTool.verify(message, ECCTool.encodeRawSignature(signature), publicKey);
        }
        // todo: think about how to make this test code only
        else if (algorithm == SignatureAlgorithm.DUMMY) {
            // this is code duplication.
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(key);
            md.update(message);
            return Arrays.equals(signature, md.digest());
        }
        else {
            throw new Exception("unknown algorithm." + algorithm);
        }
    }

}
