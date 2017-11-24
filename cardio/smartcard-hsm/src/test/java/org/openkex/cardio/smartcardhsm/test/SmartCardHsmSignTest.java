/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.cardio.smartcardhsm.test;

import org.junit.Assert;
import org.junit.Test;
import org.openkex.cardio.common.cvc.CVCTool;
import org.openkex.cardio.common.cvc.CVCertificate;
import org.openkex.cardio.smartcardhsm.SmartCardHsm;
import org.openkex.tools.Hex;
import org.openkex.tools.RandomTool;
import org.openkex.tools.crypto.ECCTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class SmartCardHsmSignTest {

    // device issuer certificate (read from card)
    private static final String DICA_CVC =
            "7F2181E27F4E819B5F290100420E44455352434143433130303030317F494F060A04007F0007020202020386410431B01BA46" +
            "616F790631F690BBDE2F0D0AE86B8FDE0D4B6F658BC7641F73CDE7A3977BE99D1AD400A7472EAC0DDD7464DB27279A37F3906" +
            "6A8A0BA400E5EC0FF15F200D44454449434D303130303030327F4C10060B2B0601040181C31F0301015301805F25060106000" +
            "102085F24060204000102075F37400D316D796A95712B6BA26C4D4D1500E6CC84F281B7C70D05BA61ECB257D1268D0FDE8206" +
            "9B8E4F1CF888338600BFE55C393BFC3AB6FB30827B0B9C01A3D16C63";

    // device certificate (read from card)
    private static final String DEVICE_CVC =
            "7F2181E47F4E819D5F290100420D44454449434D303130303030327F494F060A04007F000702020202038641044C968B29BC9" +
            "50EA61DFEF358231BBE7769623C7AADE12805BDDDE825FF135B7C74FF8C13A2756E6FD8F29BE1D19929513B66F5ADAE8FC6CF" +
            "F37FA9675E7E19385F20104445434D3031303434343830303030307F4C10060B2B0601040181C31F0301015301005F2506010" +
            "6000303015F24060204000102075F374030DB8A27F6AEA5685301C5E03392E8DCF1DB991C2662C24B07962A2913D3330F2DA0" +
            "0D448363C5AE08ACAD338C0CAA17E79E65E71D7B340A95C4E89532E9487F";

    private static final Logger LOG = LoggerFactory.getLogger(SmartCardHsmSignTest.class);

    /** test signature generated with OpenSC tools */
    @Test
    public void testOpenSC() throws Exception {
        // GENERATE KEY:
        // OpenSC: "pkcs11-tool --pin XXXXXX --keypairgen --key-type EC:secp256k1 --id 6 --label key05"
        //
        // public key: 0441 04ad15e88e8cf3db79579e376a1d2a35a3accff19a490d148d6e070e5b6a3f46a
        //                  87a313b149ccd26cf9cbacd865588cbb34bf359de9c78b6c374680a8b505cad93
        // "0441" = ASN1 byte array with 65 bytes.
        String publicRawString = "04ad15e88e8cf3db79579e376a1d2a35a3accff19a490d148d6e070e5b6a3f46a" +
                                 "87a313b149ccd26cf9cbacd865588cbb34bf359de9c78b6c374680a8b505cad93";
        byte[] publicRaw = Hex.fromString(publicRawString);

        String curveName = "secp256k1";

        PublicKey publicKey = ECCTool.getPublicKeyFromCurvePoint(publicRaw, curveName);

        byte[] message = new RandomTool(34342).getBytes(64); // 64 random bytes with fixed seed.
        LOG.info("message=" + Hex.toString(message));
        // message: 4C18A9A7041717DC150D570E176392972B76A182850A9861E78D7E7BB8500187
        //          6F46D2E315A0A2D2D04EEC54C6BB8814CC5E6774E41050C8486B1198C2864CA9
        byte[] messageHash = MessageDigest.getInstance("SHA-256").digest(message);
        LOG.info("message hash=" + Hex.toString(messageHash));
        // message.sha256: A7090ED075D5F3546581EBCF89B36EEBDAB648A6129F59DD6B54D6E4EFA8138D

        // OpenSC: "pkcs11-tool --pin XXXXXX --sign --id 6 --input-file message.sha256 --output-file sign06_01"
        // 256 byte signature in "sign06_01"
        String rawBytesString = "72ED281AA7F3C8E0483AE10522AD0DB8862513DF57538ADAA925845141CB9FE7" +
                                "5DB4F78FA7ECA848BFD8EAA6345CC045E94CE8B69BF9940B20A32761F430A108";
        byte[] signatureRawBytes = Hex.fromString(rawBytesString);

        byte[] signature = ECCTool.encodeRawSignature(signatureRawBytes);

        boolean valid = ECCTool.verify(message, signature, publicKey);

        Assert.assertTrue(valid);
    }

    @Test
    public void testRoot() throws Exception {

        // CHR=CAR=DESRCACC100001 Scheme Root CA (SRCA) aka "SmartCard-HSM Root CA"
        byte[] rootCVC = Hex.fromString(SmartCardHsm.ROOT_CVC);
        CVCTool.dump(new ByteArrayInputStream(rootCVC));

        CVCertificate cert = CVCTool.parse(new ByteArrayInputStream(rootCVC), false);
        LOG.info("rootCert=" + cert);
        // check for "root".
        Assert.assertEquals("not a root certificate!", cert.getCertificateAuthorityReference(), cert.getCertificateHolderReference());
    }

    @Test
    public void testRootValidityDate() throws Exception {

        byte[] rootCVC = Hex.fromString(SmartCardHsm.ROOT_CVC);
        CVCertificate cert = CVCTool.parse(new ByteArrayInputStream(rootCVC), false);
        LOG.info("valid from=" + cert.getEffectiveDate() + " to=" + cert.getExpiryDate());
        // from=2012-11-09 to=2032-11-08
        checkValidInternal(cert, "2012-11-09", true);
        checkValidInternal(cert, "2016-08-09", true);
        checkValidInternal(cert, "2032-11-07", true);  // edge case. is day included (till 23:59) or excluded (till 0:00)

        // invalid
        checkValidInternal(cert, "2012-11-08", false);
        checkValidInternal(cert, "2032-11-09", false);

    }

    private void checkValidInternal(CVCertificate cvc, String date, boolean valid) throws Exception {
        SimpleDateFormat format = new SimpleDateFormat(CVCertificate.DATE_FORMAT);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date testDate = format.parse(date);
        if (valid) {
            cvc.checkDate(testDate);
        }
        else {
            try {
                cvc.checkDate(testDate);
                Assert.fail();
            }
            catch (Exception e) {
                LOG.info("expected: " + e);
            }
        }
    }

    @Test
    public void testChain() throws Exception {

        // CHR=CAR=DESRCACC100001 Scheme Root CA (SRCA) aka "SmartCard-HSM Root CA"
        CVCertificate rootCert = CVCTool.parse(new ByteArrayInputStream(Hex.fromString(SmartCardHsm.ROOT_CVC)), false);
        LOG.info("rootCert=" + rootCert);

        PublicKey pkRoot = ECCTool.getPublicKeyFromCurvePoint(rootCert.getPublicKey(), ECCTool.CURVE_BRAINPOOLP256R1);

        boolean rootValid = ECCTool.verify(rootCert.getBody(), ECCTool.encodeRawSignature(rootCert.getSignature()), pkRoot);
        LOG.info("rootCert valid=" + rootValid);

        Assert.assertEquals("not a root certificate!", rootCert.getCertificateAuthorityReference(), rootCert.getCertificateHolderReference());

        // TODO: why curve guessing?
        // PK OID is not significant: 0.4.0.127.0.7.2.2.2.2.3 TA-ECDSA-SHA-256

        // CHR=DEDICM0100002 Device Issuer Certificate
        CVCertificate dicaCert = CVCTool.parse(new ByteArrayInputStream(Hex.fromString(DICA_CVC)), false);
        PublicKey pkDica = ECCTool.getPublicKeyFromCurvePoint(dicaCert.getPublicKey(), ECCTool.CURVE_BRAINPOOLP256R1);
        LOG.info("dicaCert=" + dicaCert);

        boolean dicaValid = ECCTool.verify(dicaCert.getBody(), ECCTool.encodeRawSignature(dicaCert.getSignature()), pkRoot);
        LOG.info("dicaCert valid=" + dicaValid);

        Assert.assertEquals("invalid chain!", dicaCert.getCertificateAuthorityReference(), rootCert.getCertificateHolderReference());

        // CHR=DECM010444800000
        CVCertificate deviceCert = CVCTool.parse(new ByteArrayInputStream(Hex.fromString(DEVICE_CVC)), false);
        LOG.info("deviceCert=" + deviceCert);
        // PublicKey pkDevice = ECCTool.getPublicKeyFromCurvePoint(deviceCert.getPublicKey(), ECCTool.CURVE_BRAINPOOLP256R1);

        boolean deviceValid = ECCTool.verify(deviceCert.getBody(), ECCTool.encodeRawSignature(deviceCert.getSignature()), pkDica);
        LOG.info("deviceCert valid=" + deviceValid);

        Assert.assertEquals("invalid chain!", deviceCert.getCertificateAuthorityReference(), dicaCert.getCertificateHolderReference());
    }
}
