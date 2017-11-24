/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.tools.crypto;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DLSequence;
import org.bouncycastle.jcajce.provider.asymmetric.util.EC5Util;
import org.bouncycastle.jcajce.provider.asymmetric.util.ECUtil;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.ECPointUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.jce.spec.ECNamedCurveSpec;
import org.bouncycastle.math.ec.ECCurve;
import org.openkex.tools.Validate;

import javax.crypto.KeyAgreement;
import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.util.Arrays;

/**
 * implements ECC variants and encodings
 */
public class ECCTool {

    static {
//        Security.insertProviderAt(new BouncyCastleProvider(), 1); // first option (yes, 1 is first)
        Security.addProvider(new BouncyCastleProvider()); // last option
    }

    // just some common curve values, list is not complete

    /** curve secp256k1 (used by bitcoin) */
    public static final String CURVE_SECP256K1 = "secp256k1";

    public static final String CURVE_SECP256R1 = "secp256r1";
    public static final String CURVE_SECP384R1 = "secp384r1";
    public static final String CURVE_SECT113R1 = "sect113r1";
    public static final String CURVE_SECP112R1 = "secp112r1";

    public static final String CURVE_BRAINPOOLP256R1 = "brainpoolp256r1";
    public static final String CURVE_BRAINPOOLP256T1 = "brainpoolp256t1";

    /** bouncy castle */
    private static final String PROVIDER_BC = "BC";
    /** Oracle SUN */
//    private static final String PROVIDER_SUN = "SunEC";

    // currently only BC provider working
    private static final String PROVIDER = PROVIDER_BC;

    /** ECDSA */
//     private static final String ALGORITHM = "EC";  // "EC" for SunEC
    private static final String ALGORITHM = "ECDSA";  // "ECDSA" required for BC

    /** ECC signature with SHA 256 */
    private static final String EC_SIGN_256 = "SHA256withECDSA";

    private static final String ECDH = "ECDH";

    private ECCTool() {
    }

    /**
     * decode (BSI TR-03111/X9.62) binary encoded curve point to ECC public key
     *
     * @param encoded encoded curve point key (compressed or uncompressed)
     * @param curveName name of curve (e.g. "secp256k1")
     * @return decoded Public Key
     * @throws Exception in case of crypto problem
     */
    public static PublicKey getPublicKeyFromCurvePoint(byte[] encoded, String curveName) throws Exception {
        // http://www.bouncycastle.org/wiki/display/JA1/Elliptic+Curve+Key+Pair+Generation+and+Key+Factories
        // NOTE: this is an "ugly mix" of BC and Java classes. check for improvement.
        ECNamedCurveParameterSpec spec = ECNamedCurveTable.getParameterSpec(curveName);
        ECNamedCurveSpec params = new ECNamedCurveSpec(curveName, spec.getCurve(), spec.getG(), spec.getN());
        ECPoint point = ECPointUtil.decodePoint(params.getCurve(), encoded);
        ECPublicKeySpec pubKeySpec = new ECPublicKeySpec(point, params);

        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM, PROVIDER);
        return keyFactory.generatePublic(pubKeySpec);
    }

    /**
     * get (BSI TR-03111/X9.62) encoded curve point of ECC public key
     *
     * @param key ECC public key
     * @param compressed if true get compressed encoding
     * @param curveName name of curve to use
     * @return encoded curve point
     * @throws Exception in case of crypto problem
     */
    public static byte[] encodeCurvePoint(PublicKey key, String curveName, boolean compressed) throws Exception {
        Validate.isTrue(key instanceof ECPublicKey, "not an EC key.");
        ECPublicKey ecKey = (ECPublicKey) key;
        ECCurve curve = ECUtil.getNamedCurveByName(curveName).getCurve();
        org.bouncycastle.math.ec.ECPoint bcPoint = EC5Util.convertPoint(curve, ecKey.getW(), true);
        return bcPoint.getEncoded(compressed);
    }

    public static byte[] compressCurvePoint(byte[] uncompressed, String curveName) throws Exception {
        return changePointCompression(uncompressed, curveName, true);
    }

    public static byte[] decompressCurvePoint(byte[] compressed, String curveName) throws Exception {
        return changePointCompression(compressed, curveName, false);
    }

    private static byte[] changePointCompression(byte[] input, String curveName, boolean compressed) throws Exception {
        ECNamedCurveParameterSpec spec = ECNamedCurveTable.getParameterSpec(curveName);
        ECNamedCurveSpec params = new ECNamedCurveSpec(curveName, spec.getCurve(), spec.getG(), spec.getN());
        ECPoint point = ECPointUtil.decodePoint(params.getCurve(), input);
        ECCurve curve = ECUtil.getNamedCurveByName(curveName).getCurve();
        org.bouncycastle.math.ec.ECPoint bcPoint = EC5Util.convertPoint(curve, point, true);
        return bcPoint.getEncoded(compressed);
    }

    /**
     * convert "raw" 64bit signature (e.g. from austrian citizen card) to java compatible encoding (ASN1/DER sequence of two integers)
     *
     * @param signature signature to encode
     * @return java compatible signature (ASN1/DER)
     * @throws Exception in case of crypto problem
     */
    public static byte[] encodeRawSignature(byte[] signature) throws Exception {
        Validate.notNull(signature);
        Validate.isTrue(signature.length == 64, "wrong signature size: " + signature.length);
        ASN1EncodableVector vec = new ASN1EncodableVector();
        vec.add(new ASN1Integer(new BigInteger(1, Arrays.copyOfRange(signature, 0, 32))));
        vec.add(new ASN1Integer(new BigInteger(1, Arrays.copyOfRange(signature, 32, 64))));
        DERSequence seq = new DERSequence(vec);
        return seq.getEncoded();
    }

    /**
     * convert java compatible encoding (ASN1/DER sequence of two integers) to "raw" 64 byte signature.
     * i.e. two concatenated unsigned integers with 32 byte.
     *
     * @param signature ASN1/DER signature to decode
     * @return raw signature
     * @throws Exception in case of crypto problem
     */
    public static byte[] decodeDerSignature(byte[] signature) throws Exception {
        Validate.notNull(signature);
        ASN1InputStream ais = new ASN1InputStream(new ByteArrayInputStream(signature));
        // this is NOT elegant ("knowing")
        DLSequence seq = (DLSequence) ais.readObject();
        ASN1Integer int1 = (ASN1Integer) seq.getObjectAt(0);
        ASN1Integer int2 = (ASN1Integer) seq.getObjectAt(1);
        byte[] decoded = new byte[64];
        System.arraycopy(getBytes(int1), 0, decoded, 0, 32);
        System.arraycopy(getBytes(int2), 0, decoded, 32, 32);
        return decoded;
    }

    // get "raw" unsigned 32 byte integer, padded with leading zeros
    private static byte[] getBytes(ASN1Integer integer) {
        byte[] encoded = integer.getValue().toByteArray();
        Validate.isTrue(integer.getValue().signum() == 1);
        byte[] bytes = encoded;
        // hacking
        if (encoded.length == 33) {
            // strip leading 00
            Validate.isTrue(encoded[0] == 0);
            bytes = new byte[32];
            System.arraycopy(encoded, 1, bytes, 0, 32);
        }
        else if (encoded.length == 32) {
            return bytes;
        }
        else { // encoded.length < 32, fill with zero
            bytes = new byte[32];
            System.arraycopy(encoded, 0, bytes, 32 - encoded.length, encoded.length);
        }
        return bytes;
    }

    /**
     * generate key pair
     * <p>
     * TESTING ONLY. proper private key should be stored in "secure element" (i.e. never leaves hardware token)
     *
     * @param curveName name of curve (e.g. "secp256k1")
     * @return generated key pair
     * @throws Exception in case of crypto problem
     */
    public static KeyPair generate(String curveName) throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGORITHM, PROVIDER);
//         keyGen.initialize(new ECGenParameterSpec(curveName));
        keyGen.initialize(ECNamedCurveTable.getParameterSpec(curveName));  // this supports brainpool with BC
        return keyGen.generateKeyPair();
    }

    /**
     * sign message with algorithm SHA256withECDSA
     * <p>
     * TESTING ONLY. proper private key should be stored in "secure element" (i.e. never leaves hardware token)
     *
     * @param message the message to sign
     * @param key private key
     * @return signature
     * @throws Exception in case of crypto problem
     */
    public static byte[] sign(byte[] message, PrivateKey key) throws Exception {
        Signature signature = Signature.getInstance(EC_SIGN_256, PROVIDER);
        signature.initSign(key);
        signature.update(message);
        return signature.sign();
    }

    /**
     * verify signature with algorithm SHA256withECDSA
     *
     * @param message signed message
     * @param signature signature bytes
     * @param key public key
     * @return true if signature is valid
     * @throws Exception in case of crypto problem
     */
    public static boolean verify(byte[] message, byte[] signature, PublicKey key) throws Exception {
        Signature signature1 = Signature.getInstance(EC_SIGN_256, PROVIDER);
        signature1.initVerify(key);
        signature1.update(message);
        return signature1.verify(signature);
    }

    /**
     * get shared secret base on own private and others public key
     *
     * @param localKey own/local private key
     * @param remoteKey other's/remote public key
     * @return shared secret
     * @throws Exception in case of crypto problem
     */
    public static byte[] getECDHSecret(PrivateKey localKey, PublicKey remoteKey) throws Exception {
        KeyAgreement aKeyAgree = KeyAgreement.getInstance(ECDH, PROVIDER);
        aKeyAgree.init(localKey);
        aKeyAgree.doPhase(remoteKey, true);
        return aKeyAgree.generateSecret();
    }

}
