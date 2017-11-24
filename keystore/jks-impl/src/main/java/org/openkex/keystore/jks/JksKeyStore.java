/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.keystore.jks;

import org.openkex.dto.SignatureAlgorithm;
import org.openkex.keystore.api.KeyStore;
import org.openkex.keystore.api.LockType;
import org.openkex.keystore.api.PublicKey;
import org.openkex.tools.Validate;
// CHECKSTYLE:OFF required for fake chain. may be possible with BC but is more work.
import org.openkex.tools.crypto.ECCTool;
import sun.security.x509.AlgorithmId;
import sun.security.x509.CertificateAlgorithmId;
import sun.security.x509.CertificateSerialNumber;
import sun.security.x509.CertificateValidity;
import sun.security.x509.CertificateVersion;
import sun.security.x509.CertificateX509Key;
import sun.security.x509.X500Name;
import sun.security.x509.X509CertImpl;
import sun.security.x509.X509CertInfo;
// CHECKSTYLE:ON

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * key store wrapper using "java keystore" java.security.KeyStore
 */
public class JksKeyStore implements KeyStore {

    // https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#KeyStore
    public enum Type {
        JKS,
        PKCS12
    }

    private ArrayList<SignatureAlgorithm> algorithms;

    // https://docs.oracle.com/javase/8/docs/api/java/security/KeyStore.html
    private java.security.KeyStore keyStore;
    private char[] password; // TODO: verify handling, check store vs key password

    private Type type;
    private String path;

    public JksKeyStore(Type type, String path) throws Exception {
        algorithms = new ArrayList<>();
        // currently only one algorithm ...
        algorithms.add(SignatureAlgorithm.ECDSA_SECP256K1);
        this.type = type;
        this.path = path;
    }

    public String getVendorName() {
        return "JksKeyStore type=" + type + " path=" + path;
    }

    public String getVendorId() {
        return "java.security.KeyStore";
    }

    public int getKeyCapacity() {
        return 1000; // just "enough".
    }

    public List<SignatureAlgorithm> getAlgorithms() {
        return algorithms;
    }

    public List<String> getKeyIds() throws Exception {
        checkNotLocked();
        return Collections.list(keyStore.aliases());
    }

    public PublicKey getPublicKey(String keyId) throws Exception {
        checkNotLocked();
        Certificate cert = keyStore.getCertificate(keyId);
        if (cert == null) {
            return null;
        }
        SignatureAlgorithm algorithm = SignatureAlgorithm.ECDSA_SECP256K1; // todo: currently not "flexible"
        String curve = algorithm.getIdentifier();
        byte[] pubBytes = ECCTool.encodeCurvePoint(cert.getPublicKey(), curve, true);
        return new PublicKey(keyId, algorithm, pubBytes);
    }

    public boolean verifyVendorKey(PublicKey key) {
        throw new RuntimeException("verifyVendorKey not implemented.");
    }

    public boolean unlock(char[] pin) throws Exception {
        password = pin;
        keyStore = java.security.KeyStore.getInstance(type.toString());

        File storeFile = new File(path);
        if (storeFile.exists()) {
            FileInputStream fis = new FileInputStream(storeFile);
            keyStore.load(fis, password);
            fis.close();
        }
        else {
            keyStore.load(null, password);  // strange way to "initialize"
        }
        return true;
    }

    private void store() throws Exception {
        File storeFile = new File(path);
        FileOutputStream os = new FileOutputStream(storeFile);
        keyStore.store(os, password);
        os.close();
    }

    private void checkNotLocked() {
        Validate.notNull(keyStore, "keystore is locked.");
    }

    public boolean lock() {
        if (password != null) {
            Arrays.fill(password, 'x'); // overwrite memory
            password = null;
        }
        if (keyStore != null) {
            keyStore = null;
            return true;
        }
        return false;
    }

    public LockType getLockType() {
        return LockType.UNLOCK_ONCE;
    }

    public PublicKey generateKey(SignatureAlgorithm algorithm, String keyId) throws Exception {
        if (algorithm != SignatureAlgorithm.ECDSA_SECP256K1) {
            throw new RuntimeException("unsupported algorithm. " + algorithm);
        }
        checkNotLocked();
        Validate.isTrue(!keyStore.containsAlias(keyId), "key already exists: " + keyId);

        KeyPair pair = ECCTool.generate(algorithm.getIdentifier());
        // how to create a fake "chain" for public key?
        Certificate[] chain = getFakeChain(pair);
        // this does not store certificate, only private key
        keyStore.setKeyEntry(keyId, pair.getPrivate(), password, chain);
        store();
        byte[] publicKey = ECCTool.encodeCurvePoint(pair.getPublic(), algorithm.getIdentifier(), true);
        return new PublicKey(keyId, algorithm, publicKey);
    }

    public void purge() throws Exception {
        File storeFile = new File(path);
        if (storeFile.exists()) {
            // Note: improve with overwrite file first (with random or zero).
            Validate.isTrue(storeFile.delete());
        }
    }

    public boolean deleteKey(String keyId) throws Exception {
        checkNotLocked();
        if (!keyStore.containsAlias(keyId)) {
            return false;
        }
        keyStore.deleteEntry(keyId);
        store();
        return true;
    }

    public byte[] sign(String keyId, byte[] message) throws Exception {
        checkNotLocked();
        PrivateKey key = (PrivateKey) keyStore.getKey(keyId, password);
        byte[] sig = ECCTool.sign(message, key);
        return ECCTool.decodeDerSignature(sig);
    }

    public boolean verify(String keyId, byte[] message, byte[] signature) throws Exception {
        checkNotLocked();
        PublicKey key = getPublicKey(keyId);
        Validate.notNull(key, "found no public key with id: " + keyId);

        java.security.PublicKey javaKey = ECCTool.getPublicKeyFromCurvePoint(key.getPublicKey(), key.getAlgorithm().getIdentifier());
        return ECCTool.verify(message, ECCTool.encodeRawSignature(signature), javaKey);
    }

    private Certificate[] getFakeChain(KeyPair pair) throws Exception {
        // this contains cut and waste code from sun.security.tools.keytool.CertAndKeyGen#getSelfCertificate
        // the original class is not able to initialize the KeyGen correctly (line 152) to select proper EC curve
        String sigAlg = "ECDSA";
        String dname = "CN=doNotCare";
        X500Name myname = new X500Name(dname);

        Date firstDate = new Date();
        Date lastDate = new Date();
        int days = 365;
        long validity = 24L * 24L * 60L * 1000L * days;
        lastDate.setTime(firstDate.getTime() + validity);

        CertificateValidity interval = new CertificateValidity(firstDate, lastDate);

        X509CertInfo info = new X509CertInfo();
        // Add all mandatory attributes
        info.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3));
        info.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(new java.util.Random().nextInt() & 0x7fffffff));
        AlgorithmId algID = AlgorithmId.get(sigAlg);
        info.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(algID));
        info.set(X509CertInfo.SUBJECT, myname);
        info.set(X509CertInfo.KEY, new CertificateX509Key(pair.getPublic()));
        info.set(X509CertInfo.VALIDITY, interval);
        info.set(X509CertInfo.ISSUER, myname);

        X509CertImpl cert = new X509CertImpl(info);
        cert.sign(pair.getPrivate(), sigAlg);
        // this chain is short.
        return new Certificate[] {cert};
    }

}
