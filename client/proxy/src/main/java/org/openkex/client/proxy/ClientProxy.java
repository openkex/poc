/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.client.proxy;

import com.caucho.hessian.client.HessianProxyFactory;
import com.caucho.hessian.client.HessianTlsURLConnectionFactory;
import org.openkex.client.api.ClientApi;
import org.openkex.dto.SignedStatements;
import org.openkex.server.tls.TlsClientHelper;

import javax.net.ssl.SSLContext;
import java.util.List;

public class ClientProxy implements ClientApi {

    private ClientApi proxy;

    public ClientProxy(String url, boolean useTls, boolean debugTls, boolean useClientCert, boolean useClientCertChain) throws Exception {
        HessianProxyFactory proxyFactory = new HessianProxyFactory();
        if (useTls) {
            SSLContext ctx = TlsClientHelper.get(useClientCert, useClientCertChain);
            proxyFactory.setConnectionFactory(new HessianTlsURLConnectionFactory(ctx, proxyFactory));
        }
        proxy = (ClientApi) proxyFactory.create(ClientApi.class, url);
    }

    @Override
    public byte[] getSessionChallenge(long kexId) throws Exception {
        return proxy.getSessionChallenge(kexId);
    }

    @Override
    public byte[] startKexSession(long kexId, byte[] signature) throws Exception {
        return proxy.startKexSession(kexId, signature);
    }

    @Override
    public long submitStatement(SignedStatements statement, byte[] authToken) throws Exception {
        return proxy.submitStatement(statement, authToken);
    }

    @Override
    public List<SignedStatements> getStatements(long kexId) throws Exception {
        return proxy.getStatements(kexId);
    }
}
