/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.client.proxy.test;

import org.junit.Test;
import org.openkex.client.proxy.ServerProxy;
import org.openkex.server.api.ServerApi;

// integration test, requires running server
public class ServerProxyTestIT {

    @Test
    public void testProxy() throws Exception {
        ServerApi proxy = new ServerProxy("http://localhost:8080/api/server");
        proxy.getConfirmedStatements(14);
    }
}
