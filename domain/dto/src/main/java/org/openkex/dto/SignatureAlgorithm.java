/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.dto;

/**
 * for each signature algorithm binary encodings for public key and signature are defined.
 */
public enum SignatureAlgorithm {

    ECDSA_SECP256K1(Specification.SEC, "secp256k1"),
    ED25519(Specification.EDDSA, "Ed25519"),
    ED448(Specification.EDDSA, "Ed448"),
    /** testing only. fast dummy */
    DUMMY(Specification.DUMMY, "Dummy");

    private Specification specification;
    private String identifier;

    SignatureAlgorithm(Specification specification, String identifier) {
        this.specification = specification;
        this.identifier = identifier;
    }

    public Specification getSpecification() {
        return specification;
    }

    public String getIdentifier() {
        return identifier;
    }

}
