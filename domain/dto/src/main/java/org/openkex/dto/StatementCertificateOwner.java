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
 * statement to claim ownership of a certificate.
 * see #562
 */
public class StatementCertificateOwner {

    public enum CertificateType {
        X509,
        PGP
    }

    private CertificateType type;

    // could be chain for X509?
    private byte[] certificate;

    // current public kex key signed with certificate's key
    private byte[] signature;

    // -- generated code below --

    public StatementCertificateOwner() {
    }

    public StatementCertificateOwner(CertificateType type, byte[] certificate, byte[] signature) {
        this.type = type;
        this.certificate = certificate;
        this.signature = signature;
    }

    public CertificateType getType() {
        return type;
    }

    public void setType(CertificateType type) {
        this.type = type;
    }

    public byte[] getCertificate() {
        return certificate;
    }

    public void setCertificate(byte[] certificate) {
        this.certificate = certificate;
    }

    public byte[] getSignature() {
        return signature;
    }

    public void setSignature(byte[] signature) {
        this.signature = signature;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        StatementCertificateOwner that = (StatementCertificateOwner) o;

        if (type != that.type) {
            return false;
        }
        if (!Arrays.equals(certificate, that.certificate)) {
            return false;
        }
        return Arrays.equals(signature, that.signature);
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + Arrays.hashCode(certificate);
        result = 31 * result + Arrays.hashCode(signature);
        return result;
    }

    @Override
    public String toString() {
        return "StatementCertificateOwner{" +
                "type=" + type +
                ", certificate=" + Hex.toString(certificate) +
                ", signature=" + Hex.toString(signature) +
                '}';
    }
}
