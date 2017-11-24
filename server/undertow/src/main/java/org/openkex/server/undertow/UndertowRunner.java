/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.server.undertow;

import com.caucho.hessian.server.HessianServlet;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.RequestDumpingHandler;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.FilterInfo;
import org.openkex.server.tls.TlsConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnio.Options;
import org.xnio.Sequence;
import org.xnio.SslClientAuthMode;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.servlet.DispatcherType;
import java.io.InputStream;
import java.security.KeyStore;

/**
 * start server as embedded undertow
 */
public class UndertowRunner {

    private static final Logger LOG = LoggerFactory.getLogger(UndertowRunner.class);

    private Undertow undertow;
    private String id;
    private int port;
    private boolean useTLS;
    private boolean useClientCert;

    public UndertowRunner(String id, int port, boolean useTLS, boolean useClientCert) {
        this.id = id;
        this.port = port;
        this.useTLS = useTLS;
        this.useClientCert = useClientCert;
    }

    public void run() throws Exception {

        Undertow.Builder builder = Undertow.builder();
        if (useTLS) {
            KeyStore keyStore = KeyStore.getInstance(TlsConstants.STORE_TYPE);
            InputStream keyStoreStream = this.getClass().getResourceAsStream(
                    TlsConstants.CERTIFICATES_RESOURCE + TlsConstants.TLS_SERVER_KEY_STORE);
            keyStore.load(keyStoreStream, TlsConstants.TLS_SERVER_KEY_PASS.toCharArray());
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, TlsConstants.TLS_SERVER_KEY_PASS.toCharArray()); // password of key.

            TrustManagerFactory tmf = null;
            if (useClientCert) {
                KeyStore trustStore = KeyStore.getInstance(TlsConstants.STORE_TYPE);
                InputStream trustStoreStream = this.getClass().getResourceAsStream(
                        TlsConstants.CERTIFICATES_RESOURCE + TlsConstants.TLS_SERVER_TRUST_STORE);
                trustStore.load(trustStoreStream, TlsConstants.TLS_SERVER_TRUST_PASS.toCharArray());
                tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmf.init(trustStore);
            }
            // https://docs.oracle.com/javase/8/docs/api/javax/net/ssl/SSLParameters.html
            // https://docs.oracle.com/javase/8/docs/api/javax/net/ssl/SSLContext.html
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(kmf.getKeyManagers(), tmf == null ? null : tmf.getTrustManagers(), null);

            //SSLParameters sslParams = null; cannot "pack" this into SSLContext
            //sslParams.setCipherSuites(new String[] {TlsConstants.TLS_SUITE});
            //sslParams.setProtocols(new String[] {"TLSv1.2"});
            //sslParams.setNeedClientAuth(useClientCert);

            // org.xnio.Options SSL_ENABLED_CIPHER_SUITES,SSL_ENABLED_PROTOCOLS, SSL_CLIENT_AUTH_MODE
            // builder.setWorkerOption() and setServerOption() also work but do "NOTHING", this is a great API!
            builder.setSocketOption(Options.SSL_ENABLED_CIPHER_SUITES, Sequence.of(TlsConstants.TLS_SUITE));
            builder.setSocketOption(Options.SSL_ENABLED_PROTOCOLS, Sequence.of("TLSv1.2"));
            if (useClientCert) {
                builder.setSocketOption(Options.SSL_CLIENT_AUTH_MODE, SslClientAuthMode.REQUIRED);
            }
            builder = builder.addHttpsListener(port, "localhost", sslContext);
        }
        else {
            builder = builder.addHttpListener(port, "localhost");
        }

        undertow = builder.setBufferSize(1024 * 16)
                //this seems slightly faster in some configurations
                .setIoThreads(Runtime.getRuntime().availableProcessors())
                .setSocketOption(Options.BACKLOG, 10000)
                //don't send a keep-alive header for HTTP/1.1 requests, as it is not required
                .setServerOption(UndertowOptions.ALWAYS_SET_KEEP_ALIVE, false)
                .setServerOption(UndertowOptions.ALWAYS_SET_DATE, true)
                .setWorkerThreads(200)
                .setHandler(createServletHandler(false))
                .build();

        undertow.start();
        LOG.info("started undertow. id=" + id + " port=" + port + " ssl=" + useTLS + " cert=" + useClientCert);
    }

    private static HttpHandler createServletHandler(boolean dump) throws Exception {
        // http://undertow.io/undertow-docs/undertow-docs-1.4.0/#undertow-servlet
        // http://undertow.io/javadoc/1.4.x/io/undertow/servlet/api/DeploymentInfo.html
        String client = "client";
        String server = "server";
        String filter = "log";
        String path = "/api";
        DeploymentInfo servletBuilder = Servlets.deployment()
                .setClassLoader(UndertowMain.class.getClassLoader())
                .setContextPath(path)
                .setDeploymentName("servlet.war")
                .addServlet(
                        Servlets.servlet(client, HessianServlet.class)
                                .addMapping("/client")
                                .addInitParam("home-api", "org.openkex.client.api.ClientApi")
                                .addInitParam("home-class", "org.openkex.server.backend.ServerCore")
                                .setLoadOnStartup(1)
                )
                .addServlet(
                        Servlets.servlet(server, HessianServlet.class)
                                .addMapping("/server")
                                .addInitParam("home-api", "org.openkex.server.api.ServerApi")
                                .addInitParam("home-class", "org.openkex.server.backend.ServerCore")
                                .setLoadOnStartup(1)
                )
                .addFilter(new FilterInfo(filter, LogFilter.class))
                .addFilterServletNameMapping(filter, client, DispatcherType.REQUEST)
                .addFilterServletNameMapping(filter, server, DispatcherType.REQUEST);

        DeploymentManager manager = Servlets.defaultContainer().addDeployment(servletBuilder);
        manager.deploy();
        HttpHandler handler = Handlers.path(Handlers.redirect(path)).addPrefixPath(path, manager.start());
        if (dump) {
            return new RequestDumpingHandler(handler);
        }
        return handler;
    }

    public void await() throws Exception {
        // no such thing?
        Thread.sleep(Long.MAX_VALUE);
    }

    public void stop() throws Exception {
        undertow.stop();
    }

}
