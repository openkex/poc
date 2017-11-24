/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.keystore.api;

import org.openkex.dto.SignatureAlgorithm;

public class PublicKey {

    private String id;

    private SignatureAlgorithm algorithm;

    private byte[] publicKey;

    private byte[] vendorProve;

    public PublicKey(String id, SignatureAlgorithm algorithm, byte[] publicKey) {
        this.id = id;
        this.algorithm = algorithm;
        this.publicKey = publicKey;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public SignatureAlgorithm getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(SignatureAlgorithm algorithm) {
        this.algorithm = algorithm;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(byte[] publicKey) {
        this.publicKey = publicKey;
    }

    public byte[] getVendorProve() {
        return vendorProve;
    }

    public void setVendorProve(byte[] vendorProve) {
        this.vendorProve = vendorProve;
    }
}
