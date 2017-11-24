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
import org.openkex.server.undertow.UndertowRunner;
import org.openkex.tools.Timer;

import java.util.ArrayList;

public class UndertowRunnerTestLoad {

    static {
        // may not work if log4j2 is already initialized
        System.setProperty("log4j2.level", "warn"); // see log4j2.xml
    }

    @Test
    public void testMultiHttp() throws Exception {
        int port1 = 9000;
        int count = 1000; // starts 2-3x faster than tomcat, takes approx 1GB, tomcat takes 2GB
        Timer t = new Timer("testing " + count + " undertows", "run", false);
        ArrayList<UndertowRunner> runners = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            UndertowRunner t1 = runUndertow("UT" + i, port1 + i, false, false);
            runners.add(t1);
            t1.run();
        }
        t.split("request", true);
        for (int i = 0; i < count; i++) {
            ClientApi api1 = new ClientProxy(getUrl(port1 + i, false), false, false, false, false);
            api1.getStatements(0x1000 + i);
            api1.getStatements(0x2000 + i);
        }
        t.split("stop", true);
        for (UndertowRunner r : runners) {
            r.stop();
        }
        t.stop(true);
    }

    private String getUrl(int port, boolean ssl) {
        return (ssl ? "https" : "http") + "://localhost:" + port + "/api/client";
    }

    private UndertowRunner runUndertow(String id, int port, boolean ssl, boolean cert) throws Exception {
        return new UndertowRunner(id, port, ssl, cert);
    }
}
