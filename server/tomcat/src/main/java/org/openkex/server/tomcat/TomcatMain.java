/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.server.tomcat;

import org.openkex.server.tls.TlsConstants;
import org.openkex.tools.DirectoryTool;

public class TomcatMain {

    private TomcatMain() {
    }

    public static void main(String[] args) throws Exception {

        System.setProperty("log4j2.disable.jmx", "true"); // too much JMX

        String target = DirectoryTool.getTargetDirectory(TomcatRunner.class);
        // tomcat cannot load certificates from classpath, why?
        String id = "T1";
        String keyPath = target + "../../tls-cert/target/" + TlsConstants.CERTIFICATES_PATH;
        String baseDir = target + "tmp";
        String docBase = target + "docs";

        boolean useTLS = args.length > 0;
        boolean useClientCert = args.length > 1;
        int port = 8080;
        if (useTLS) {
            port = TlsConstants.TLS_SERVER_PORT;
        }

        TomcatRunner runner = new TomcatRunner(id, port, useTLS, useClientCert, keyPath, docBase, baseDir);
        runner.run();
        runner.await();
    }
}
