/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.cardio.common.cvc;

import org.openkex.tools.Hex;

/**
 * Card Verifiable Certificate Request
 * <p>
 * see BSI TR-03110 (Part 3, Appendix C and D)
 */
public class CVCertificateRequest {

    private CVCertificate certificate;
    private String certificateAuthorityReference;
    private byte[] carData;
    private byte[] signature;

    public CVCertificateRequest() {
    }

    public CVCertificate getCertificate() {
        return certificate;
    }

    public void setCertificate(CVCertificate certificate) {
        this.certificate = certificate;
    }

    public String getCertificateAuthorityReference() {
        return certificateAuthorityReference;
    }

    public void setCertificateAuthorityReference(String certificateAuthorityReference) {
        this.certificateAuthorityReference = certificateAuthorityReference;
    }

    public byte[] getSignature() {
        return signature;
    }

    public void setSignature(byte[] signature) {
        this.signature = signature;
    }

    public byte[] getCarData() {
        return carData;
    }

    public void setCarData(byte[] carData) {
        this.carData = carData;
    }

    @Override
    public String toString() {
        return "CVCertificateRequest{" +
                "certificate=" + certificate +
                ", CAR='" + certificateAuthorityReference + '\'' +
                ", signature=" + Hex.toString(signature) +
                '}';
    }
}
