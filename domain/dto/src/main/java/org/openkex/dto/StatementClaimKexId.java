/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.dto;

import org.openkex.tools.Hex;

import java.util.Arrays;

/**
 * claim ownership of KexId with given public key
 * <p>
 * Note that kexId is already generic part of Statements.
 * <p>
 * Note: key update would require same statement. but signature is done with previous key
 */
public class StatementClaimKexId {

    private SignatureAlgorithm algorithm;

    /** public key according to algorithm */
    private byte[] key;

    public StatementClaimKexId() {
    }

    public StatementClaimKexId(SignatureAlgorithm algorithm, byte[] key) {
        this.algorithm = algorithm;
        this.key = key;
    }

    public SignatureAlgorithm getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(SignatureAlgorithm algorithm) {
        this.algorithm = algorithm;
    }

    public byte[] getKey() {
        return key;
    }

    public void setKey(byte[] key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return "StatementClaimKexId{" +
                "algorithm=" + algorithm +
                ", key=" + Hex.toString(key) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        StatementClaimKexId that = (StatementClaimKexId) o;

        if (algorithm != that.algorithm) {
            return false;
        }
        return Arrays.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        int result = algorithm.hashCode();
        result = 31 * result + Arrays.hashCode(key);
        return result;
    }
}
