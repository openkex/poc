/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.server.tls;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.security.KeyStore;

/**
 * helper class for HTTPS client
 */
public class TlsClientHelper {

    private TlsClientHelper() {
    }

    /**
     * get context for TLS client
     *
     * @param useClientCert if true use client certificate
     * @param useClientCertChain if true use client certificate chain
     * @throws Exception in case of problems
     */
    public static SSLContext get(boolean useClientCert, boolean useClientCertChain) throws Exception {
        KeyManagerFactory kmf = null;
        if (useClientCert) {
            KeyStore keyStore = KeyStore.getInstance(TlsConstants.STORE_TYPE);
            kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            if (useClientCertChain) {
                InputStream keyStoreStream = TlsClientHelper.class.getResourceAsStream(
                        TlsConstants.CERTIFICATES_RESOURCE + TlsConstants.TLS_CLIENT_CHAIN_KEY_STORE);
                keyStore.load(keyStoreStream, TlsConstants.TLS_CLIENT_CHAIN_KEY_PASS.toCharArray());
                kmf.init(keyStore, TlsConstants.TLS_CLIENT_CHAIN_KEY_PASS.toCharArray()); // password of key.
            }
            else {
                InputStream keyStoreStream = TlsClientHelper.class.getResourceAsStream(
                        TlsConstants.CERTIFICATES_RESOURCE + TlsConstants.TLS_CLIENT_KEY_STORE);
                keyStore.load(keyStoreStream, TlsConstants.TLS_CLIENT_KEY_PASS.toCharArray());
                kmf.init(keyStore, TlsConstants.TLS_CLIENT_KEY_PASS.toCharArray()); // password of key.
            }
        }

        KeyStore trustStore = KeyStore.getInstance(TlsConstants.STORE_TYPE);
        InputStream trustStoreStream = TlsClientHelper.class.getResourceAsStream(
                TlsConstants.CERTIFICATES_RESOURCE + TlsConstants.TLS_CLIENT_TRUST_STORE);
        trustStore.load(trustStoreStream, TlsConstants.TLS_CLIENT_TRUST_PASS.toCharArray());
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);

        // https://docs.oracle.com/javase/8/docs/api/javax/net/ssl/SSLParameters.html
        // https://docs.oracle.com/javase/8/docs/api/javax/net/ssl/SSLContext.html
        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        sslContext.init(kmf == null ? null : kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        return sslContext;
    }

    /*
     * perform "static" setup for TLS client
     *
     * @param useClientCert if true use client certificate
     * @param useClientCertChain if true use client certificate chain
     * @param debug if true enable TLS debugging (this logs a lot)
     * @throws Exception in case of problems
     */
    /*
    public static void setup(boolean useClientCert, boolean useClientCertChain, boolean debug) throws Exception {

        String certPath = DirectoryTool.getTargetDirectory(TlsClientHelper.class) + TlsConstants.CERTIFICATES_PATH;

        // http://docs.oracle.com/javase/8/docs/technotes/guides/security/jsse/JSSERefGuide.html
        System.setProperty("java.protocol.handler.pkgs", "javax.net.ssl");
        System.setProperty("javax.net.ssl.trustStore", certPath + TlsConstants.TLS_CLIENT_TRUST_STORE);
        System.setProperty("javax.net.ssl.trustStorePassword", TlsConstants.TLS_CLIENT_TRUST_PASS);
        System.setProperty("javax.net.ssl.trustStoreType", TlsConstants.STORE_TYPE);
        System.setProperty("jdk.tls.client.protocols", "TLSv1.2");
        System.setProperty("https.protocols", "TLSv1.2");
        System.setProperty("https.cipherSuites", TlsConstants.TLS_SUITE);
        if (debug) {
            System.setProperty("javax.net.debug", "ssl");
        }

        // disable host name validation
        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            public boolean verify(String s, SSLSession sslSession) {
                return true;
            }
        });

        if (useClientCert) {
            System.setProperty("javax.net.ssl.keyStoreType", TlsConstants.STORE_TYPE);
            if (useClientCertChain) {
                System.setProperty("javax.net.ssl.keyStore", certPath + TlsConstants.TLS_CLIENT_CHAIN_KEY_STORE);
                System.setProperty("javax.net.ssl.keyStorePassword", TlsConstants.TLS_CLIENT_CHAIN_KEY_PASS);
            }
            else {
                System.setProperty("javax.net.ssl.keyStore", certPath + TlsConstants.TLS_CLIENT_KEY_STORE);
                System.setProperty("javax.net.ssl.keyStorePassword", TlsConstants.TLS_CLIENT_KEY_PASS);
            }
        }
    }
    */
}
