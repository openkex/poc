/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.client.proxy.test;

import org.junit.Test;
import org.openkex.client.api.ClientApi;
import org.openkex.client.proxy.ClientProxy;
import org.openkex.server.tls.TlsConstants;

// integration test, requires running server (in according mode)
public class ClientProxyTestIT {

    private static final String HTTP_URL = "http://localhost:8080/api/client";
    private static final String HTTPS_URL = "https://localhost:" + TlsConstants.TLS_SERVER_PORT + "/api/client";

    @Test
    public void testProxy() throws Exception {
        ClientApi proxy = new ClientProxy(HTTP_URL, false, false, false, false);
        proxy.getStatements(4711);
    }

    @Test
    public void testProxyTls() throws Exception {
        ClientApi proxy = new ClientProxy(HTTPS_URL, true, true, false, false);
        proxy.getStatements(4711);
    }

    @Test
    public void testProxyTlsClientCert() throws Exception {
        ClientApi proxy = new ClientProxy(HTTPS_URL, true, true, true, false);
        proxy.getStatements(4711);
    }

    @Test
    public void testProxyTlsClientCertChain() throws Exception {
        ClientApi proxy = new ClientProxy(HTTPS_URL, true, true, true, true);
        proxy.getStatements(4711);
    }
}
