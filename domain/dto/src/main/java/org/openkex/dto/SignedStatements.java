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
 * class that represents Statements + Signature (similar to a certificate)
 * <p>
 * SignedStatements are the primary data object, each round will store a valid set of SignedStatements
 *
 * TODO: check less elegant alternative. move signatureBytes to Statements. for "signature input message" serialize with signatureBytes == null.
 */
public class SignedStatements {

    private Statements statements;

    // byte of actual signature
    private byte[] signatureBytes;

    // -- generated code below --

    public SignedStatements() {
    }

    public SignedStatements(Statements statements, byte[] signatureData) {
        this.statements = statements;
        this.signatureBytes = signatureData;
    }

    public Statements getStatements() {
        return statements;
    }

    public byte[] getSignatureBytes() {
        return signatureBytes;
    }

    public void setStatements(Statements statements) {
        this.statements = statements;
    }

    public void setSignatureBytes(byte[] signatureBytes) {
        this.signatureBytes = signatureBytes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SignedStatements that = (SignedStatements) o;

        if (statements != null ? !statements.equals(that.statements) : that.statements != null) {
            return false;
        }
        return Arrays.equals(signatureBytes, that.signatureBytes);
    }

    @Override
    public int hashCode() {
        int result = statements != null ? statements.hashCode() : 0;
        result = 31 * result + Arrays.hashCode(signatureBytes);
        return result;
    }

    @Override
    public String toString() {
        return "SignedStatements{" +
                "statements=" + statements +
                ", signatureBytes=" + Hex.toString(signatureBytes) +
                '}';
    }
}
