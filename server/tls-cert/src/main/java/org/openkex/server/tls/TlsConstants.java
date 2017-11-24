/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.server.tls;

/**
 * configuration data for automatic ssl setup.
 * <p>
 * FOR TESTING PURPOSE ONLY (obviously)
 */
public interface TlsConstants {

    int TLS_CERT_KEY_LENGTH = 256;
    String TLS_CERT_KEY_ALGORITM = "EC";
    String TLS_CERT_SIGNATURE_ALGORITHM = "SHA256WithECDSA";
    int TLS_CERT_VALIDITY_DAYS = 365;
    String TLS_SUITE = "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256";
    int TLS_SERVER_PORT = 8443;
    // String STORE_TYPE = "jks";
    String STORE_TYPE = "pkcs12";

    String CERTIFICATES_RESOURCE = "/certificates/";
    String CERTIFICATES_PATH = "classes" + CERTIFICATES_RESOURCE;

    String TLS_SERVER_KEY_ALIAS = "server1";
    String TLS_SERVER_KEY_STORE = "serverKey." + STORE_TYPE;
    String TLS_SERVER_KEY_PASS = "secret1";
    String TLS_SERVER_CERT = "server.cer";
    String TLS_SERVER_TRUST_STORE = "serverTrust." + STORE_TYPE;
    String TLS_SERVER_TRUST_PASS = "secret2";

    String TLS_CLIENT_KEY_ALIAS = "client1";
    String TLS_CLIENT_KEY_STORE = "clientKey." + STORE_TYPE;
    String TLS_CLIENT_KEY_PASS = "secret3";
    String TLS_CLIENT_CERT = "client.cer";
    String TLS_CLIENT_TRUST_STORE = "clientTrust." + STORE_TYPE;
    String TLS_CLIENT_TRUST_PASS = "secret4";

    String TLS_CLIENT_CHAIN_KEY_ALIAS = "clientChain1";
    String TLS_CLIENT_CHAIN_KEY_STORE = "clientChainKey." + STORE_TYPE;
    String TLS_CLIENT_CHAIN_KEY_PASS = "secret5";
    String TLS_CLIENT_CHAIN_CSR = "clientChain.csr";
    String TLS_CLIENT_CHAIN_CERT = "clientChain.cer";

}
