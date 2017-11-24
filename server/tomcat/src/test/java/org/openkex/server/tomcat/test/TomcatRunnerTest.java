/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.server.tomcat.test;

import org.junit.Assert;
import org.junit.Test;
import org.openkex.client.api.ClientApi;
import org.openkex.client.proxy.ClientProxy;
import org.openkex.server.tls.TlsClientHelper;
import org.openkex.server.tls.TlsConstants;
import org.openkex.server.tomcat.TomcatRunner;
import org.openkex.tools.DirectoryTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class TomcatRunnerTest {

    private static final Logger LOG = LoggerFactory.getLogger(TomcatRunnerTest.class);

    @Test
    public void testMultiHttp() throws Exception {
        int port1 = 9000;
        int count = 5;
        ArrayList<TomcatRunner> runners = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            TomcatRunner t1 = runTomcat("TC" + i, port1 + i, false, false);
            runners.add(t1);
            t1.run();
        }
        for (int i = 0; i < count; i++) {
            ClientApi api1 = new ClientProxy(getUrl(port1 + i, false), false, false, false, false);
            api1.getStatements(0x1000 + i);
            api1.getStatements(0x2000 + i);
        }
        for (TomcatRunner r : runners) {
            r.stop();
        }
    }

    @Test
    public void testHttp() throws Exception {
        int port = 8082; // avoid race condition with testTwoHttp, stopping is not immediate
        TomcatRunner t1 = runTomcat("T1", port, false, false);
        t1.run();
        ClientApi api = new ClientProxy(getUrl(port, false), false, false, false, false);
        api.getStatements(0x4000);
        api.getStatements(0x4001);
        t1.stop();
    }

    @Test
    public void testHttps() throws Exception {
        int port = 8443;
        TomcatRunner t1 = runTomcat("T1S", port, true, false);
        t1.run();
        ClientApi api = new ClientProxy(getUrl(port, true), true, false, false, false);
        api.getStatements(0x5000);
        api.getStatements(0x5001);
        t1.stop();
    }

    @Test
    public void testHttpsCert() throws Exception {
        int port = 8444;
        TomcatRunner t1 = runTomcat("T1SC", port, true, true);
        t1.run();
        ClientApi api = new ClientProxy(getUrl(port, true), true, false, true, false);
        api.getStatements(0x5000);
        api.getStatements(0x5001);
        t1.stop();
    }

    @Test
    public void testHttpsCertFail() throws Exception {
        int port = 8445;
        TomcatRunner t1 = runTomcat("T1SCF", port, true, true);
        t1.run();
        try {
            // omit client certificate that is required by server
            ClientApi api = new ClientProxy(getUrl(port, true), true, false, false, false);
            api.getStatements(0x5000);
            api.getStatements(0x5001);
            Assert.fail();
        }
        catch (Exception e) {
            LOG.info("expected exception: " + e);
        }
        t1.stop();
    }

    @Test
    public void testHttpsCertChain() throws Exception {
        int port = 8446;
        TomcatRunner t1 = runTomcat("T1SC", port, true, true);
        t1.run();
        ClientApi api = new ClientProxy(getUrl(port, true), true, false, true, true);
        api.getStatements(0x5000);
        api.getStatements(0x5001);
        t1.stop();
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
