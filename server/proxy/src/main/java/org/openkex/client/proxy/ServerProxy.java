/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.client.proxy;

import com.caucho.hessian.client.HessianProxyFactory;

import org.openkex.dto.SignedStatements;
import org.openkex.server.api.ServerApi;

import java.util.List;

public class ServerProxy implements ServerApi {

    private ServerApi proxy;

    public ServerProxy(String url) throws Exception {
        HessianProxyFactory proxyFactory = new HessianProxyFactory();
        proxy = (ServerApi) proxyFactory.create(ServerApi.class, url);
    }

    @Override
    public List<SignedStatements> newStatements(long roundNr) throws Exception {
        return proxy.newStatements(roundNr);
    }

    @Override
    public byte[] getHash(long roundNr) throws Exception {
        return proxy.getSignature(roundNr);
    }

    @Override
    public byte[] getSignature(long roundNr) throws Exception {
        return proxy.getSignature(roundNr);
    }

    @Override
    public List<SignedStatements> getConfirmedStatements(long roundNr) throws Exception {
        return proxy.getConfirmedStatements(roundNr);
    }
}
