/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.dto;

import org.openkex.tools.Hex;
import org.openkex.tools.NumberByteConverter;

import java.util.Arrays;

/**
 * Statements represent the list of statements that will be signed.
 * <br>
 * More technical: Statements will be serialized to bytes and the used as signature message input.
 */
public class Statements {

    // the "signer" = "issuer"
    private long issuerKexId;

    // seconds since 1.1.1970. not using millis here to save some space with protobuf
    private long signatureDate;

    /**
     * hash value of previous approved SignedStatements (building a chain for a specific kexId).
     * Null for initial Statements (i.e. StatementClaimKexId)
     */
    private byte[] previousHash;

    /**
     * number of previous block round. -1 for initial Statements
     */
    private long previousRound;

    // this is a trick for protobuf serializer. usually only one statement is not null.
    // could use "oneof" feature

    private StatementClaimKexId claimKexId;

    private StatementCertificateOwner certificateOwner;

    private StatementPublicAccountOwner publicAccountOwner;

    // might be required for some use cases
    // private SignedStatements nestedStatement;

    // -- generated code below --

    public Statements() {
    }

    public Statements(long issuerKexId, long signatureDate, byte[] previousHash, long previousRound, StatementClaimKexId claimKexId,
                      StatementCertificateOwner certificateOwner, StatementPublicAccountOwner publicAccountOwner) {
        this.issuerKexId = issuerKexId;
        this.signatureDate = signatureDate;
        this.previousHash = previousHash;
        this.previousRound = previousRound;
        this.claimKexId = claimKexId;
        this.certificateOwner = certificateOwner;
        this.publicAccountOwner = publicAccountOwner;
    }

    public long getIssuerKexId() {
        return issuerKexId;
    }

    public void setIssuerKexId(long issuerKexId) {
        this.issuerKexId = issuerKexId;
    }

    public long getSignatureDate() {
        return signatureDate;
    }

    public void setSignatureDate(long signatureDate) {
        this.signatureDate = signatureDate;
    }

    public byte[] getPreviousHash() {
        return previousHash;
    }

    public void setPreviousHash(byte[] previousHash) {
        this.previousHash = previousHash;
    }

    public long getPreviousRound() {
        return previousRound;
    }

    public void setPreviousRound(long previousRound) {
        this.previousRound = previousRound;
    }

    public StatementClaimKexId getClaimKexId() {
        return claimKexId;
    }

    public void setClaimKexId(StatementClaimKexId claimKexId) {
        this.claimKexId = claimKexId;
    }

    public StatementCertificateOwner getCertificateOwner() {
        return certificateOwner;
    }

    public void setCertificateOwner(StatementCertificateOwner certificateOwner) {
        this.certificateOwner = certificateOwner;
    }

    public StatementPublicAccountOwner getPublicAccountOwner() {
        return publicAccountOwner;
    }

    public void setPublicAccountOwner(StatementPublicAccountOwner publicAccountOwner) {
        this.publicAccountOwner = publicAccountOwner;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Statements that = (Statements) o;

        if (issuerKexId != that.issuerKexId) {
            return false;
        }
        if (signatureDate != that.signatureDate) {
            return false;
        }
        if (previousRound != that.previousRound) {
            return false;
        }
        if (!Arrays.equals(previousHash, that.previousHash)) {
            return false;
        }
        if (claimKexId != null ? !claimKexId.equals(that.claimKexId) : that.claimKexId != null) {
            return false;
        }
        if (certificateOwner != null ? !certificateOwner.equals(that.certificateOwner) : that.certificateOwner != null) {
            return false;
        }
        return publicAccountOwner != null ? publicAccountOwner.equals(that.publicAccountOwner) : that.publicAccountOwner == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (issuerKexId ^ (issuerKexId >>> 32));
        result = 31 * result + (int) (signatureDate ^ (signatureDate >>> 32));
        result = 31 * result + Arrays.hashCode(previousHash);
        result = 31 * result + (int) (previousRound ^ (previousRound >>> 32));
        result = 31 * result + (claimKexId != null ? claimKexId.hashCode() : 0);
        result = 31 * result + (certificateOwner != null ? certificateOwner.hashCode() : 0);
        result = 31 * result + (publicAccountOwner != null ? publicAccountOwner.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Statements{" +
                "issuerKexId=" + NumberByteConverter.longToSixByteString(issuerKexId) +
                ", signatureDate=" + signatureDate +
                ", previousHash=" + Hex.toString(previousHash) +
                ", previousRound=" + previousRound +
                ", claimKexId=" + claimKexId +
                ", certificateOwner=" + certificateOwner +
                ", publicAccountOwner=" + publicAccountOwner +
                '}';
    }
}
