/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.server.undertow;

import org.openkex.server.tls.TlsConstants;

public class UndertowMain {

    private UndertowMain() {
    }

    public static void main(String[] args) throws Exception {

        String id = "T1";
        boolean useTLS = args.length > 0;
        boolean useClientCert = args.length > 1;
        int port = 8080;
        if (useTLS) {
            port = TlsConstants.TLS_SERVER_PORT;
        }

        UndertowRunner runner = new UndertowRunner(id, port, useTLS, useClientCert);
        runner.run();
        runner.await();
    }
}
