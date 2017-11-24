/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.server.tomcat;

import com.caucho.hessian.server.HessianServlet;
import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.openkex.server.tls.TlsConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import java.io.File;

/**
 * start server as embedded tomcat
 */
public class TomcatRunner {

    private static final Logger LOG = LoggerFactory.getLogger(TomcatRunner.class);

    private Tomcat tomcat;
    private String id;
    private int port;
    private boolean useTLS;
    private boolean useClientCert;
    private String keyPath;
    private String docBase;
    private String baseDir;

    public TomcatRunner(String id, int port, boolean useTLS, boolean useClientCert, String keyPath, String docBase, String baseDir) {
        this.id = id;
        this.port = port;
        this.useTLS = useTLS;
        this.useClientCert = useClientCert;
        this.keyPath = keyPath;
        this.docBase = docBase;
        this.baseDir = baseDir;
    }

    public void run() throws Exception {

        tomcat = new Tomcat();
        // needed for "work" dir etc..
        tomcat.setBaseDir(baseDir);

        tomcat.setPort(port);

        tomcat.setHostname(id); // just a hack. resolves JMX errors when running multiple instances

        Connector connector = tomcat.getConnector();
        // must set for tomcat 9, with tomcat 8.5 this was default.
        connector.setAttribute("protocol", "HTTP/1.1");

        if (useTLS) {
            // https://tomcat.apache.org/tomcat-8.5-doc/config/http.html
            // https://tomcat.apache.org/tomcat-9.0-doc/config/http.html
            connector.setSecure(true);
            connector.setScheme("https");
            connector.setAttribute("SSLEnabled", true);
            connector.setAttribute("keyAlias", TlsConstants.TLS_SERVER_KEY_ALIAS);
            connector.setAttribute("keystorePass", TlsConstants.TLS_SERVER_KEY_PASS);
            connector.setAttribute("keystoreType", TlsConstants.STORE_TYPE);
            connector.setAttribute("keystoreFile", keyPath + TlsConstants.TLS_SERVER_KEY_STORE);
            connector.setAttribute("truststorePass", TlsConstants.TLS_SERVER_TRUST_PASS);
            connector.setAttribute("truststoreType", TlsConstants.STORE_TYPE);
            connector.setAttribute("truststoreFile", keyPath + TlsConstants.TLS_SERVER_TRUST_STORE);
            if (useClientCert) {
                // "Set to true if you want the SSL stack to require a valid
                // certificate chain from the client before accepting a connection."
                connector.setAttribute("clientAuth", "true");
            }
            connector.setAttribute("sslProtocol", "TLSv1.2");

            // http://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html
            // pick one single "proper" cipher suite
            connector.setAttribute("ciphers", TlsConstants.TLS_SUITE);
//                     "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256"); // working OK
//                     "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384"); // requires unlimited crypto!
        }

        File docBaseFile = new File(docBase); // only needed for "statics", just use working path path for now
        if (!docBaseFile.mkdirs()) {
            LOG.info("mkdirs failed. " + docBaseFile);
        }
        Context ctx = tomcat.addContext("/api", docBaseFile.getAbsolutePath());

        String clientName = "client";
        Servlet clientServlet = new HessianServlet();
        Wrapper clientWrapper = Tomcat.addServlet(ctx, clientName, clientServlet);
        clientWrapper.addInitParameter("home-api", "org.openkex.client.api.ClientApi");
        clientWrapper.addInitParameter("home-class", "org.openkex.server.backend.ServerCore");
        clientWrapper.setLoadOnStartup(1);
        ctx.addServletMappingDecoded("/client", clientName);

        String serverName = "server";
        Servlet serverServlet = new HessianServlet();
        Wrapper serverWrapper = Tomcat.addServlet(ctx, serverName, serverServlet);
        serverWrapper.addInitParameter("home-api", "org.openkex.server.api.ServerApi");
        serverWrapper.addInitParameter("home-class", "org.openkex.server.backend.ServerCore");
        serverWrapper.setLoadOnStartup(1);
        ctx.addServletMappingDecoded("/server", serverName);

        // add log filter
        String filterName = "log";
        FilterDef filterDef = new FilterDef();
        filterDef.setFilterName(filterName);
        filterDef.setFilterClass(LogFilter.class.getName());
        ctx.addFilterDef(filterDef);

        // map log filter
        FilterMap filterMap = new FilterMap();
        filterMap.addURLPattern("*");
        filterMap.setFilterName(filterName);
        ctx.addFilterMap(filterMap);

        tomcat.start();
        LOG.info("started tomcat. id=" + id + " port=" + port + " ssl=" + useTLS + " cert=" + useClientCert);
    }

    public void await() throws Exception {
        tomcat.getServer().await();
    }

    public void stop() throws Exception {
        tomcat.stop();
    }

}
