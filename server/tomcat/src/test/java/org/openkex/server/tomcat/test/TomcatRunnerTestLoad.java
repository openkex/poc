/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.server.tomcat.test;

import org.junit.Test;
import org.openkex.client.api.ClientApi;
import org.openkex.client.proxy.ClientProxy;
import org.openkex.server.tls.TlsClientHelper;
import org.openkex.server.tls.TlsConstants;
import org.openkex.server.tomcat.TomcatRunner;
import org.openkex.tools.DirectoryTool;
import org.openkex.tools.Timer;

import java.util.ArrayList;

public class TomcatRunnerTestLoad {

    static {
        // may not work if log4j2 is already initialized
        System.setProperty("log4j2.level", "warn"); // see log4j2.xml
    }

    @Test
    public void testMultiHttp() throws Exception {
        int port1 = 9000;
        // int count = 500; // 2x 8s
        int count = 1000; // 2x 15s, takes approx 2GB
        Timer t = new Timer("testing " + count + " tomcats", "run", false);
        ArrayList<TomcatRunner> runners = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            TomcatRunner t1 = runTomcat("TC" + i, port1 + i, false, false);
            runners.add(t1);
            t1.run();
        }
        t.split("request", true);
        for (int i = 0; i < count; i++) {
            ClientApi api1 = new ClientProxy(getUrl(port1 + i, false), false, false, false, false);
            api1.getStatements(0x1000 + i);
            api1.getStatements(0x2000 + i);
        }
        /* stopping is the slowest part? omit it.
        t.split("stop", true);
        for (TomcatRunner r : runners) {
            r.stop();
        }
         */
        t.stop(true);
    }

    private String getUrl(int port, boolean ssl) {
        return (ssl ? "https" : "http") + "://localhost:" + port + "/api/client";
    }

    private TomcatRunner runTomcat(String id, int port, boolean ssl, boolean cert) throws Exception {
        String target = DirectoryTool.getTargetDirectory(TomcatRunner.class);
        // directories per tomcat id
        String baseDir = target + id + "/tmp";
        String docBase = target + id + "/docs";
        String certPath = DirectoryTool.getTargetDirectory(TlsClientHelper.class) + TlsConstants.CERTIFICATES_PATH;
        return new TomcatRunner(id, port, ssl, cert, certPath, docBase, baseDir);
    }
}
