/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.cardio.common.cvc;

import org.openkex.tools.Hex;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Card Verifiable Certificate
 * <p>
 * see BSI TR-03110 (Part 3, Appendix C and D)
 */
public class CVCertificate {

    /**
     * format of date representation. Time Zone is UTC
     */

    public static final String DATE_FORMAT = "yyyy-MM-dd";

    private byte[] body;
    private byte[] data;  // full certificate

    private String certificateAuthorityReference;
    private String certificateHolderReference;

    private byte[] publicKey;

    private String effectiveDate;
    private String expiryDate;

    private String algorithm;
    private byte[] signature;

    public CVCertificate() {
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public String getCertificateAuthorityReference() {
        return certificateAuthorityReference;
    }

    public void setCertificateAuthorityReference(String certificateAuthorityReference) {
        this.certificateAuthorityReference = certificateAuthorityReference;
    }

    public String getCertificateHolderReference() {
        return certificateHolderReference;
    }

    public void setCertificateHolderReference(String certificateHolderReference) {
        this.certificateHolderReference = certificateHolderReference;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(byte[] publicKey) {
        this.publicKey = publicKey;
    }

    public String getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(String effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public byte[] getSignature() {
        return signature;
    }

    public void setSignature(byte[] signature) {
        this.signature = signature;
    }

    /**
     * check if certificate is valid for given date
     *
     * @param date considered date
     * @throws RuntimeException if certificate is invalid
     */
    public void checkDate(Date date) {
        if (effectiveDate == null && expiryDate == null) {
            // nothing to check.
            return;
        }
        SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        String dateString = format.format(date);

        if (effectiveDate != null && dateString.compareTo(effectiveDate) < 0) {
            throw new RuntimeException("Certificate with CHR + " + certificateHolderReference +
                    " Invalid. Date " + dateString + " is before effective date " + effectiveDate);
        }
        if (expiryDate != null && dateString.compareTo(expiryDate) > 0) {
            throw new RuntimeException("Certificate with CHR + " + certificateHolderReference +
                    " Invalid. Date " + dateString + " is after expiry date " + expiryDate);
        }
    }

    @Override
    public String toString() {
        return "CVC{" +
                "CAR='" + certificateAuthorityReference + '\'' +
                ", CHR='" + certificateHolderReference + '\'' +
                ", publicKey=" + Hex.toString(publicKey) +
                ", effectiveDate='" + effectiveDate + '\'' +
                ", expiryDate='" + expiryDate + '\'' +
                ", algorithm='" + algorithm + '\'' +
                ", signature=" + Hex.toString(signature) +
                '}';
    }
}
