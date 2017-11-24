/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
// not the package needs to be hessian as the constructor of HessianURLConnection is package private
package com.caucho.hessian.client;

import org.openkex.tools.Validate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

/**
 * This class is an extended version of HessianURLConnectionFactory.
 * It enables better TLS configuration via SSLContext.
 */
public class HessianTlsURLConnectionFactory implements HessianConnectionFactory {
    private HessianProxyFactory _proxyFactory;
    private SSLContext sslContext;

    public HessianTlsURLConnectionFactory(SSLContext context, HessianProxyFactory proxy) {
        this.sslContext = context;
        this._proxyFactory = proxy;
    }

    public void setHessianProxyFactory(HessianProxyFactory factory) {
        _proxyFactory = factory;
    }

    /**
     * Opens a new or recycled connection to the HTTP server.
     */
    public HessianConnection open(URL url) throws IOException {

        URLConnection conn = url.openConnection();
        Validate.isTrue(conn instanceof HttpsURLConnection);
        HttpsURLConnection sslConn = (HttpsURLConnection) conn;
        sslConn.setSSLSocketFactory(sslContext.getSocketFactory());
        // disable host name validation
        sslConn.setHostnameVerifier(new HostnameVerifier() {
            public boolean verify(String s, SSLSession sslSession) {
                return true;
            }
        });

        long connectTimeout = _proxyFactory.getConnectTimeout();

        if (connectTimeout >= 0) {
            conn.setConnectTimeout((int) connectTimeout);
        }

        conn.setDoOutput(true);

        long readTimeout = _proxyFactory.getReadTimeout();

        if (readTimeout > 0) {
            try {
                conn.setReadTimeout((int) readTimeout);
            }
            catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        return new HessianURLConnection(url, conn);
    }
}
