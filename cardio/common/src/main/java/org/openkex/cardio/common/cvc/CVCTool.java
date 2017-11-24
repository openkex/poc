/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.cardio.common.cvc;

import org.openkex.tools.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * tool to parse CVC certificates according to BSI TR-03110 (Part 3, Appendix C)
 * <p>
 * see: https://www.bsi.bund.de/EN/Publications/TechnicalGuidelines/TR03110/BSITR03110.html
 */
public class CVCTool {

    private static final Logger LOG = LoggerFactory.getLogger(CVCTool.class);

    private CVCTool() {
    }

    /**
     * dump CVC structure
     *
     * @param is stream with cvc certificate
     * @throws IOException if read fails from input stream
     */
    public static void dump(InputStream is) throws IOException {
        decode(is, "");
    }

    private static void decode(InputStream is, String prefix) throws IOException {
        while (is.available() > 0) {
            CVCTagLengthValue tlv = new CVCTagLengthValue(is, null);
            LOG.info(prefix + tlv);

            if (tlv.getTag().getType() == CVCTag.Type.Sequence) {
                ByteArrayInputStream isSeq = new ByteArrayInputStream(tlv.getContent());
                decode(isSeq, prefix + "    ");
            }
        }
    }

    public static CVCTagLengthValue getFirst(InputStream is, int tag) throws IOException {
        return find(is, tag);
    }

    private static CVCTagLengthValue find(InputStream is, int tag) throws IOException {
        while (is.available() > 0) {
            CVCTagLengthValue tlv = new CVCTagLengthValue(is, null);
            if (tlv.getTag().getValue() == tag) {
                return tlv;
            }
            if (tlv.getTag().getType() == CVCTag.Type.Sequence) {
                ByteArrayInputStream isSeq = new ByteArrayInputStream(tlv.getContent());
                return find(isSeq, tag);
            }
        }
        return null;
    }

    /**
     * read CVC certificate request from stream
     *
     * @return certificate request
     * @param is stream with cvc certificate request
     * @throws IOException if read fails from input stream
     */
    public static CVCertificateRequest parseRequest(InputStream is) throws IOException {
        CVCertificateRequest request = new CVCertificateRequest();
        CVCTagLengthValue requestTag = new CVCTagLengthValue(is, CVCTag.AUTHENTICATION);
        byte[] requestBytes = requestTag.getContent();
        ByteArrayInputStream requestStream = new ByteArrayInputStream(requestBytes);
        CVCertificate cert = parse(requestStream, true);
        request.setCertificate(cert);
        CVCTagLengthValue carTag = new CVCTagLengthValue(requestStream, CVCTag.CERTIFICATE_AUTHORITY_REFERENCE);
        request.setCertificateAuthorityReference(carTag.getContentString());
        request.setCarData(carTag.getHeaderAndContent());  // need for signature
        CVCTagLengthValue signatureTag = new CVCTagLengthValue(requestStream, CVCTag.SIGNATURE);
        request.setSignature(signatureTag.getContent());
        return request;
    }

        /**
         * read CVC certificate from stream
         *
         * @return certificate
         * @param is stream with cvc certificate
         * @param request cvc request if true
         * @throws IOException if read fails from input stream
         */
    public static CVCertificate parse(InputStream is, boolean request) throws IOException {
        CVCertificate cvc = new CVCertificate();
        CVCTagLengthValue certTag = new CVCTagLengthValue(is, CVCTag.CV_CERTIFICATE);
        byte[] certificateBytes = certTag.getContent();
        ByteArrayInputStream certificateStream = new ByteArrayInputStream(certificateBytes);
        CVCTagLengthValue bodyTag = new CVCTagLengthValue(certificateStream, CVCTag.CERTIFICATE_BODY);
        byte[] fullBody = bodyTag.getHeaderAndContent();
        cvc.setBody(fullBody); // need for signature
        cvc.setData(certTag.getHeaderAndContent());

        ByteArrayInputStream bodyStream = new ByteArrayInputStream(bodyTag.getContent());

        CVCTagLengthValue certificateProfileIdentifier = new CVCTagLengthValue(bodyStream, CVCTag.CERTIFICATE_PROFILE_IDENTIFIER);
        Validate.notNull(certificateProfileIdentifier); // fake use variable
        // ignore certificate profile ID...
        CVCTagLengthValue carTag = new CVCTagLengthValue(bodyStream, CVCTag.CERTIFICATE_AUTHORITY_REFERENCE);
        cvc.setCertificateAuthorityReference(carTag.getContentString());
        CVCTagLengthValue pkTag = new CVCTagLengthValue(bodyStream, CVCTag.PUBLIC_KEY);
        ByteArrayInputStream pkStream = new ByteArrayInputStream(pkTag.getContent());
        // read Public Key fields
        CVCTagLengthValue pkOidTag = new CVCTagLengthValue(pkStream, CVCTag.OID);
        Validate.notNull(pkOidTag); // fake use variable
        while (pkStream.available() > 0) {
            CVCTagLengthValue pkParamTag = new CVCTagLengthValue(pkStream, null);
            if (pkParamTag.getTag() == CVCTag.ECC_PUBLIC_POINT) {
                cvc.setPublicKey(pkParamTag.getContent());
            }
            // TODO more fields?
        }
        CVCTagLengthValue chrTag = new CVCTagLengthValue(bodyStream, CVCTag.CERTIFICATE_HOLDER_REFERENCE);
        cvc.setCertificateHolderReference(chrTag.getContentString());
        if (!request) {
            CVCTagLengthValue chatTag = new CVCTagLengthValue(bodyStream, CVCTag.CERTIFICATE_HOLDER_AUTHORIZATION_TEMPLATE);
            ByteArrayInputStream chatStream = new ByteArrayInputStream(chatTag.getContent());
            CVCTagLengthValue chatOidTag = new CVCTagLengthValue(chatStream, CVCTag.OID);
            Validate.notNull(chatOidTag); // fake use variable
            CVCTagLengthValue chatDataTag = new CVCTagLengthValue(chatStream, CVCTag.DISCRETIONARY_DATA);
            Validate.notNull(chatDataTag); // fake use variable
            Validate.isTrue(chatStream.available() == 0);
            // ignoring chat content...

            CVCTagLengthValue effectiveTag = new CVCTagLengthValue(bodyStream, CVCTag.CERTIFICATE_EFFECTIVE_DATE);
            cvc.setEffectiveDate(effectiveTag.getContentString());
            CVCTagLengthValue expirationTag = new CVCTagLengthValue(bodyStream, CVCTag.CERTIFICATE_EXPIRATION_DATE);
            cvc.setExpiryDate(expirationTag.getContentString());
        }
        if (bodyStream.available() > 0) {
            CVCTagLengthValue extensionTag = new CVCTagLengthValue(bodyStream, CVCTag.CERTIFICATE_EXTENSIONS);
            Validate.notNull(extensionTag); // fake use variable
            // ignore extensions...
        }
        Validate.isTrue(bodyStream.available() == 0);

        CVCTagLengthValue signatureTag = new CVCTagLengthValue(certificateStream, CVCTag.SIGNATURE);
        cvc.setSignature(signatureTag.getContent());

        Validate.isTrue(certificateStream.available() == 0);
        return cvc;
    }

}
