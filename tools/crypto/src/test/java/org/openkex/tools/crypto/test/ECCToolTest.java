/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.tools.crypto.test;

import org.junit.Assert;
import org.junit.Test;
import org.openkex.tools.Hex;
import org.openkex.tools.RandomTool;
import org.openkex.tools.Timer;
import org.openkex.tools.crypto.ASN1Tool;
import org.openkex.tools.crypto.ECCTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

public class ECCToolTest {

    private static final Logger LOG = LoggerFactory.getLogger(ECCToolTest.class);

    @Test
    public void testSimple() throws Exception {
        testSimpleCurve(ECCTool.CURVE_SECP256K1);
        testSimpleCurve(ECCTool.CURVE_SECP256R1);
        testSimpleCurve(ECCTool.CURVE_SECP384R1);
        testSimpleCurve(ECCTool.CURVE_SECT113R1);
        testSimpleCurve(ECCTool.CURVE_SECP112R1);
        testSimpleCurve(ECCTool.CURVE_BRAINPOOLP256R1);  // NOTE: works only with BouncyCastle
        testSimpleCurve(ECCTool.CURVE_BRAINPOOLP256T1);  // NOTE: works only with BouncyCastle
    }

    private void testSimpleCurve(String curve) throws Exception {
        KeyPair pair = ECCTool.generate(curve);
        byte[] message = "Hello World".getBytes("ASCII");

        byte[] signature = ECCTool.sign(message, pair.getPrivate());

        LOG.info("signature " + curve + " (" + signature.length + "bytes): " + Hex.toString(signature));

        boolean success = ECCTool.verify(message, signature, pair.getPublic());

        Assert.assertTrue(success);
    }

    @Test
    public void testEncode() throws Exception {

        KeyPair pair = ECCTool.generate(ECCTool.CURVE_SECP256K1);
        byte[] message = "Hello World".getBytes("ASCII");

//      this does not change the result....
//        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(publicKey.getEncoded());
//        publicEncoded = x509EncodedKeySpec.getEncoded();

        byte[] publicEncoded = pair.getPublic().getEncoded(); // implicit X509 encoding

        LOG.info("publicEncoded (" + publicEncoded.length + "bytes): " + Hex.toString(publicEncoded));
        // 88 bytes
        // 3056301006072a8648ce3d020106052b8104000a034200046e8c7e072a16c26da7e9fc61fb07e1ede3ce42a8
        // 741399f395dc55c8ae47dcc502b1d4d0ebf5f985433a85a273b57db092c2c4ec13283fbf701d22cec7a75b41

        ASN1Tool.dump("publicEncoded", publicEncoded);
        /*
        Sequence
            Sequence
                ObjectIdentifier(1.2.840.10045.2.1)
                ObjectIdentifier(1.3.132.0.10)
            DER Bit String[65, 0]
                046e8c7e072a16c26da7e9fc61fb07e1ede3ce42a8741399f395dc55c8ae47dc    n~*maBtUG
                c502b1d4d0ebf5f985433a85a273b57db092c2c4ec13283fbf701d22cec7a75b    C:s}(?p"[
                41                                                                  A
        */

        // http://www.oid-info.com/get/1.2.840.10045.2.1 Elliptic curve public key cryptography RFC 3279 and RFC 5753.
        // http://www.oid-info.com/get/1.3.132.0.10 secp256k1 (SEC 2)
        // DER Bit String is encoded curve point. see TR-03111, Chapter 3 (same as X9.62?)

        byte[] privateEncoded = pair.getPrivate().getEncoded();
        LOG.info("privateEncoded (" + privateEncoded.length + "bytes): " + Hex.toString(privateEncoded));
        ASN1Tool.dump("privateEncoded", privateEncoded);
        /*
        Sequence
            Integer(0)
            Sequence
                ObjectIdentifier(1.2.840.10045.2.1)
                ObjectIdentifier(1.3.132.0.10)
            DER Octet String[118]
                30740201010420f175a41d6907d36820232ce4dfa00a0f9249c8b67d29046422    0t uih #,I})d"
                99d5a5d624075aa00706052b8104000aa14403420004fd66ab3472c4cb59af38    $Z+DBf4rY8
                07b2253f28cc3aaf06bac21d728f2a1a98f1729996883455f607f50546ad0899    %?(:r*r4UF
                7ccfdce00063ba9cd6d91b0d5bfa93fbbf5c9d04f682                        |c[\
         */
        // DER Octet String is another nested ASN1 thing

        String octetString = "30740201010420f175a41d6907d36820232ce4dfa00a0f9249c8b67d29046422" +
                             "99d5a5d624075aa00706052b8104000aa14403420004fd66ab3472c4cb59af38" +
                             "07b2253f28cc3aaf06bac21d728f2a1a98f1729996883455f607f50546ad0899" +
                             "7ccfdce00063ba9cd6d91b0d5bfa93fbbf5c9d04f682";

        ASN1Tool.dump("privateEncodedNested", Hex.fromString(octetString));
        /*
        Sequence
            Integer(1)
            DER Octet String[32]
                f175a41d6907d36820232ce4dfa00a0f9249c8b67d2904642299d5a5d624075a    uih #,I})d"$Z
            Tagged [0]
                ObjectIdentifier(1.3.132.0.10)
            Tagged [1]
                DER Bit String[65, 0]
                    04fd66ab3472c4cb59af3807b2253f28cc3aaf06bac21d728f2a1a98f1729996    f4rY8%?(:r*r
                    883455f607f50546ad08997ccfdce00063ba9cd6d91b0d5bfa93fbbf5c9d04f6    4UF|c[\
                    82
         */

        // these bytes are a "constant value" f175a41d6907d36820232ce4dfa00a0f9249c8b67d2904642299d5a5d624075a

        byte[] signature = ECCTool.sign(message, pair.getPrivate());
        LOG.info("signature (" + signature.length + "bytes): " + Hex.toString(signature));
        // 72 bytes
        // 3046022100ad177938af520abd49567c8e72e9e733ed932d2a2ddf833fdf99939d5b83fa
        // 5c022100a522866a8014e2641cb1d671a13120f9d751aa206c00ff431fee8d94a030482a

        ASN1Tool.dump("signature", signature);
        /*
        Sequence
            Integer(78291596926923364189793105193577655679940671796260942546168683669302018308700)
            Integer(74692620521694800455093346503614287721861610322649942561157303821670164744234)
        */
    }

    @Test
    public void testDecode() throws Exception {
        // data create by single run of "testEncode", see comments.

        String signatureString = "3046022100ad177938af520abd49567c8e72e9e733ed932d2a2ddf833fdf99939d5b83fa" +
                                 "5c022100a522866a8014e2641cb1d671a13120f9d751aa206c00ff431fee8d94a030482a";
        byte[] signature = Hex.fromString(signatureString);

        byte[] message = "Hello World".getBytes("ASCII");

        checkSignatureWithFullKey(signature, message);
        checkSignatureWithCurvePoint(signature, message);
    }

    private void checkSignatureWithCurvePoint(byte[] signature, byte[] message) throws Exception {
        // create Public Key based on encoded curve point
        String pointString = "046e8c7e072a16c26da7e9fc61fb07e1ede3ce42a8741399f395dc55c8ae47dc" +
                             "c502b1d4d0ebf5f985433a85a273b57db092c2c4ec13283fbf701d22cec7a75b41";

        byte[] point = Hex.fromString(pointString);
        PublicKey publicDecoded2 = ECCTool.getPublicKeyFromCurvePoint(point, ECCTool.CURVE_SECP256K1);

        ASN1Tool.dump("publicDecoded", publicDecoded2.getEncoded());  // OIDs are "reconstructed" correctly

        boolean success2 = ECCTool.verify(message, signature, publicDecoded2);
        Assert.assertTrue(success2);
    }

    private void checkSignatureWithFullKey(byte[] signature, byte[] message) throws Exception {
        // create Public Key based on X509 encoding
        String publicEncodedString =
                "3056301006072a8648ce3d020106052b8104000a034200046e8c7e072a16c26da7e9fc61fb07e1ede3ce42a8" +
                "741399f395dc55c8ae47dcc502b1d4d0ebf5f985433a85a273b57db092c2c4ec13283fbf701d22cec7a75b41";
        byte[] publicEncoded = Hex.fromString(publicEncodedString);

        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicEncoded);
        PublicKey publicDecoded = keyFactory.generatePublic(publicKeySpec);

        boolean success = ECCTool.verify(message, signature, publicDecoded);
        Assert.assertTrue(success);
    }

    @Test
    public void testPointDecodeEncode() throws Exception {

        String pointString = "046e8c7e072a16c26da7e9fc61fb07e1ede3ce42a8741399f395dc55c8ae47dc" +
                             "c502b1d4d0ebf5f985433a85a273b57db092c2c4ec13283fbf701d22cec7a75b41";

        byte[] point = Hex.fromString(pointString);
        PublicKey publicDecoded2 = ECCTool.getPublicKeyFromCurvePoint(point, ECCTool.CURVE_SECP256K1);

        byte[] pointEncoded = ECCTool.encodeCurvePoint(publicDecoded2, ECCTool.CURVE_SECP256K1, false);
        Assert.assertArrayEquals(point, pointEncoded);
    }

    @Test
    public void testPointEncodeDecode() throws Exception {
        testPointEncodeDecodeCurve(ECCTool.CURVE_SECP256R1);
        testPointEncodeDecodeCurve(ECCTool.CURVE_SECP384R1);
        testPointEncodeDecodeCurve(ECCTool.CURVE_SECP256K1);
        testPointEncodeDecodeCurve(ECCTool.CURVE_SECP112R1);
        testPointEncodeDecodeCurve(ECCTool.CURVE_BRAINPOOLP256R1);
        testPointEncodeDecodeCurve(ECCTool.CURVE_BRAINPOOLP256T1);
    }

    @Test
    public void testPointEncodeDecodeCompressed() throws Exception {
        testPointEncodeDecodeCurveCompressed(ECCTool.CURVE_SECP256R1);
        testPointEncodeDecodeCurveCompressed(ECCTool.CURVE_SECP384R1);
        testPointEncodeDecodeCurveCompressed(ECCTool.CURVE_SECP256K1);
        testPointEncodeDecodeCurveCompressed(ECCTool.CURVE_SECP112R1); // missing in "CustomNamedCurves" but exist in "SECNamedCurves"
        testPointEncodeDecodeCurveCompressed(ECCTool.CURVE_BRAINPOOLP256R1);
        testPointEncodeDecodeCurveCompressed(ECCTool.CURVE_BRAINPOOLP256T1);
    }

/*  assumed to be irrelevant (using only BC now).
    @Test
    public void testPointEncodeDecodeFail() throws Exception {
        // getPublicKeyFromCurvePoint fails (with provider SunEC) with error:
        // java.security.spec.InvalidKeySpecException: java.security.InvalidKeyException: EC parameters error
        // ...
        // java.security.spec.InvalidParameterSpecException: Not a supported curve: org.bouncycastle.jce.spec.ECNamedCurveSpec@9629756
        // Q: how can this fail for this curve only?
        try {
            testPointEncodeDecodeCurve(ECCTool.CURVE_SECT113R1);
        }
        catch (Exception e) {
            LOG.warn("exception for sect113r1", e);
        }
    }
*/

    @Test
    public void testPointEncodeDecodeMix() throws Exception {
        // Q: will reading point fail for any wrong curve?
        testPointEncodeDecodeCurveFail(ECCTool.CURVE_SECP256K1, ECCTool.CURVE_SECP256R1);
        testPointEncodeDecodeCurveFail(ECCTool.CURVE_SECP256R1, ECCTool.CURVE_SECP256K1);
        testPointEncodeDecodeCurveFail(ECCTool.CURVE_BRAINPOOLP256R1, ECCTool.CURVE_SECP256K1);
        testPointEncodeDecodeCurveFail(ECCTool.CURVE_BRAINPOOLP256R1, ECCTool.CURVE_BRAINPOOLP256T1);

        testPointEncodeDecodeCurveFail(ECCTool.CURVE_SECP256R1, ECCTool.CURVE_SECP112R1);
    }

    private void testPointEncodeDecodeCurve(String curve) throws Exception {
        testPointEncodeDecodeCurve(curve, curve);
    }

    private void testPointEncodeDecodeCurve(String curveGen, String curveDecode) throws Exception {
        PublicKey key = ECCTool.generate(curveGen).getPublic();

        byte[] pointEncoded = ECCTool.encodeCurvePoint(key, curveGen, false);

        PublicKey keyFromPoint = ECCTool.getPublicKeyFromCurvePoint(pointEncoded, curveDecode);

        // same key reconstructed from point?
        Assert.assertArrayEquals(key.getEncoded(), keyFromPoint.getEncoded());
    }

    private void testPointEncodeDecodeCurveCompressed(String curve) throws Exception {
        PublicKey key = ECCTool.generate(curve).getPublic();

        byte[] pointEncodedCompressed = ECCTool.encodeCurvePoint(key, curve, true);
        byte[] pointEncoded = ECCTool.encodeCurvePoint(key, curve, false);

        PublicKey keyFromPointCompressed = ECCTool.getPublicKeyFromCurvePoint(pointEncodedCompressed, curve);
        PublicKey keyFromPoint = ECCTool.getPublicKeyFromCurvePoint(pointEncoded, curve);

        LOG.info("curve " + curve + " uncompressed " + pointEncoded.length + " compressed " + pointEncodedCompressed.length);
        LOG.info("compressed bytes " + Hex.toString(pointEncodedCompressed));

        // same key reconstructed from compressed point?
        Assert.assertArrayEquals(key.getEncoded(), keyFromPoint.getEncoded());
        Assert.assertArrayEquals(key.getEncoded(), keyFromPointCompressed.getEncoded());
    }

    private void testPointEncodeDecodeCurveFail(String curveGen, String curveDecode) {
        try {
            PublicKey key = ECCTool.generate(curveGen).getPublic();

            byte[] pointEncoded = ECCTool.encodeCurvePoint(key, curveGen, false);

            PublicKey keyFromPoint = ECCTool.getPublicKeyFromCurvePoint(pointEncoded, curveDecode);

            // same key reconstructed from point?
            Assert.assertArrayEquals(key.getEncoded(), keyFromPoint.getEncoded());
            Assert.fail();  // must fail
        }
        catch (Exception e) {
            LOG.info("encode:" + curveGen + " decode: " + curveDecode + " failed with: " + e);
        }
    }

    @Test
    public void testECDH() throws Exception {
        // two parties, each with (EC) keypair
        KeyPair local = ECCTool.generate(ECCTool.CURVE_SECP256K1);
        KeyPair remote = ECCTool.generate(ECCTool.CURVE_SECP256K1);

        // each party uses own secret and other's public key
        byte[] secretLocal = ECCTool.getECDHSecret(local.getPrivate(), remote.getPublic());
        byte[] secretRemote = ECCTool.getECDHSecret(remote.getPrivate(), local.getPublic());

        LOG.info("secret has " + secretLocal.length + " bytes, value=" + Hex.toString(secretLocal));

        // result is "same" secret for both parties.
        Assert.assertArrayEquals(secretLocal, secretRemote);
    }

    @Test
    public void testEncodeDecodeSignature() throws Exception {
        // 64 byte
        String rawString = "72ED281AA7F3C8E0483AE10522AD0DB8862513DF57538ADAA925845141CB9FE7" +
                           "DFB4F78FA7ECA848BFD8EAA6345CC045E94CE8B69BF9940B20A32761F430A108";
        byte[] raw = Hex.fromString(rawString);
        byte[] encoded = ECCTool.encodeRawSignature(raw);

        // 70-72 byte (6-8 byte overhead)
        // 3044022072ed281aa7f3c8e0483ae10522ad0db8862513df57538adaa925845141cb9f
        // e702205db4f78fa7eca848bfd8eaa6345cc045e94ce8b69bf9940b20a32761f430a108
        LOG.info("encoded " + encoded.length + " bytes, value=" + Hex.toString(encoded));
        ASN1Tool.dump("encoded", encoded);

        byte[] rawDec = ECCTool.decodeDerSignature(encoded);

        Assert.assertArrayEquals(raw, rawDec);
    }

    @Test
    public void testEncodeDecodeSignatureRandom() throws Exception {
        RandomTool rand = new RandomTool();
        for (int i = 0; i < 100; i++) {
            KeyPair kp = ECCTool.generate(ECCTool.CURVE_SECP256K1);
            byte[] msg = rand.getBytes(345);
            byte[] signature = ECCTool.sign(msg, kp.getPrivate());
            byte[] signatureRaw = ECCTool.decodeDerSignature(signature);
            byte[] decoded = ECCTool.encodeRawSignature(signatureRaw);
            Assert.assertTrue(Arrays.equals(signature, decoded));
        }
    }

    @Test
    public void testPointCompression() throws Exception {
        String pointString = "046e8c7e072a16c26da7e9fc61fb07e1ede3ce42a8741399f395dc55c8ae47dc" +
                             "c502b1d4d0ebf5f985433a85a273b57db092c2c4ec13283fbf701d22cec7a75b41";

        byte[] point = Hex.fromString(pointString);
        LOG.info("uncompressed " + point.length + " bytes");
        byte[] compressed = ECCTool.compressCurvePoint(point, ECCTool.CURVE_SECP256K1);
        LOG.info("compressed " + compressed.length + " bytes, value=" + Hex.toString(compressed));
        byte[] expanded = ECCTool.decompressCurvePoint(compressed, ECCTool.CURVE_SECP256K1);
        Assert.assertArrayEquals(point, expanded);
    }

    @Test
    public void testGenerateSpeed() throws Exception {
        int repeats = 100;
        String curve = ECCTool.CURVE_SECP256K1;
        RandomTool rand = new RandomTool();

        ECCTool.generate(curve); // warm up

        Timer timer = new Timer("generate " + repeats + " " + curve + " key pairs", false);
        for (int i = 0; i < repeats; i++) {
            ECCTool.generate(curve);
        }
        timer.stop(true);

        KeyPair pair = ECCTool.generate(curve);

        ECCTool.sign(new byte[] {11, 22}, pair.getPrivate()); // warm up

        timer = new Timer("sign " + repeats + " messages with " + curve, false);
        for (int i = 0; i < repeats; i++) {
            byte[] message = rand.getBytes(345);
            ECCTool.sign(message, pair.getPrivate());
        }
        timer.stop(true);
    }

}
