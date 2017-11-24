/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.server.tls;

import org.openkex.tools.DirectoryTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Method;

/**
 * this  CertGenerator will invoke "keytool"
 * <p>
 * Invocation happens currently via reflection (assuming class is "just there" at runtime).
 * More defensive would be to use ProcessBuilder to run keytool command.
 * <p>
 * Note that all this ugliness is caused by the fact that keytool does not offer a proper ("non command line") API
 */
public class CertGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(CertGenerator.class);

    private CertGenerator() {
    }

    public static void main(String[] args) throws Exception {

        boolean useClientCert = args.length > 0;
        boolean useClientCertChain = args.length > 1;

        // could also be argument ${project.build.directory} in pom.xml, but would not work with shaded jar.
        String path = DirectoryTool.getTargetDirectory(CertGenerator.class) + TlsConstants.CERTIFICATES_PATH;
        new File(path).mkdirs();

        // http://docs.oracle.com/javase/8/docs/technotes/tools/windows/keytool.html
        // https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#KeyStore

        String certGenParam =
                " -validity " + TlsConstants.TLS_CERT_VALIDITY_DAYS +
                // http://docs.oracle.com/javase/8/docs/technotes/guides/security/crypto/CryptoSpec.html#AppA
                " -keyalg " + TlsConstants.TLS_CERT_KEY_ALGORITM +
                " -sigalg " + TlsConstants.TLS_CERT_SIGNATURE_ALGORITHM +
                " -keysize " + TlsConstants.TLS_CERT_KEY_LENGTH;

        invokeKeyTool("-genkeypair" +
                " -keystore " + path + TlsConstants.TLS_SERVER_KEY_STORE +
                " -storepass " + TlsConstants.TLS_SERVER_KEY_PASS +
                " -storetype " + TlsConstants.STORE_TYPE +
                certGenParam +
                " -alias " + TlsConstants.TLS_SERVER_KEY_ALIAS +
                " -keypass " + TlsConstants.TLS_SERVER_KEY_PASS +
                " -dname CN=server01"
        );

        invokeKeyTool("-exportcert" +
                " -keystore " + path + TlsConstants.TLS_SERVER_KEY_STORE +
                " -storepass " + TlsConstants.TLS_SERVER_KEY_PASS +
                " -storetype " + TlsConstants.STORE_TYPE +
                " -alias " + TlsConstants.TLS_SERVER_KEY_ALIAS +
                " -file " + path + TlsConstants.TLS_SERVER_CERT  // what format?
        );

        invokeKeyTool("-importcert" +
                " -keystore " + path + TlsConstants.TLS_CLIENT_TRUST_STORE +
                " -storepass " + TlsConstants.TLS_CLIENT_TRUST_PASS +
                " -storetype " + TlsConstants.STORE_TYPE +
                " -alias " + TlsConstants.TLS_SERVER_KEY_ALIAS +
                " -file " + path + TlsConstants.TLS_SERVER_CERT +
                " -trustcacerts" +
                " -noprompt"
        );

        if (useClientCert) {
            // make this a CA for chain certificates
            // NOTE: this sample is working without this extensions.
            // String ext = useClientCertChain ? " -ext BC:critical=ca:true -ext KU:critical=keyCertSign" : "";
            invokeKeyTool("-genkeypair" +
                    " -keystore " + path + TlsConstants.TLS_CLIENT_KEY_STORE +
                    " -storepass " + TlsConstants.TLS_CLIENT_KEY_PASS +
                    " -storetype " + TlsConstants.STORE_TYPE +
                    certGenParam +
                    " -alias " + TlsConstants.TLS_CLIENT_KEY_ALIAS +
                    " -keypass " + TlsConstants.TLS_CLIENT_KEY_PASS +
                    " -dname CN=client01" // + ext
            );

            invokeKeyTool("-exportcert" +
                    " -keystore " + path + TlsConstants.TLS_CLIENT_KEY_STORE +
                    " -storepass " + TlsConstants.TLS_CLIENT_KEY_PASS +
                    " -storetype " + TlsConstants.STORE_TYPE +
                    " -alias " + TlsConstants.TLS_CLIENT_KEY_ALIAS +
                    " -file " + path + TlsConstants.TLS_CLIENT_CERT
            );

            invokeKeyTool("-importcert" +
                    " -keystore " + path + TlsConstants.TLS_SERVER_TRUST_STORE +
                    " -storepass " + TlsConstants.TLS_SERVER_TRUST_PASS +
                    " -storetype " + TlsConstants.STORE_TYPE +
                    " -alias " + TlsConstants.TLS_CLIENT_KEY_ALIAS +
                    " -file " + path + TlsConstants.TLS_CLIENT_CERT +
                    " -trustcacerts" +
                    " -noprompt"
            );
            // use client cert as "CA", request certificate to create "chain"
            if (useClientCertChain) {
                // crate chain certificate
                invokeKeyTool("-genkeypair" +
                        " -keystore " + path + TlsConstants.TLS_CLIENT_CHAIN_KEY_STORE +
                        " -storepass " + TlsConstants.TLS_CLIENT_CHAIN_KEY_PASS +
                        " -storetype " + TlsConstants.STORE_TYPE +
                        certGenParam +
                        " -keypass " + TlsConstants.TLS_CLIENT_CHAIN_KEY_PASS +
                        " -alias " + TlsConstants.TLS_CLIENT_CHAIN_KEY_ALIAS +
                        " -dname CN=client01chain"
                );
                // crate CSR (Certificate Signing Request) for chain certificate
                invokeKeyTool("-certreq" +
                        " -keystore " + path + TlsConstants.TLS_CLIENT_CHAIN_KEY_STORE +
                        " -storepass " + TlsConstants.TLS_CLIENT_CHAIN_KEY_PASS +
                        " -storetype " + TlsConstants.STORE_TYPE +
                        " -alias " + TlsConstants.TLS_CLIENT_CHAIN_KEY_ALIAS +
                        " -file " + path + TlsConstants.TLS_CLIENT_CHAIN_CSR
                );
                // client CA confirms request
                invokeKeyTool("-gencert" +
                        " -keystore " + path + TlsConstants.TLS_CLIENT_KEY_STORE +
                        " -storepass " + TlsConstants.TLS_CLIENT_KEY_PASS +
                        " -storetype " + TlsConstants.STORE_TYPE +
                        " -alias " + TlsConstants.TLS_CLIENT_KEY_ALIAS +
                        " -infile " + path + TlsConstants.TLS_CLIENT_CHAIN_CSR +
                        " -outfile " + path + TlsConstants.TLS_CLIENT_CHAIN_CERT
                        // " -ext " // do we need an extension?
                );

                // import root (otherwise chain import fails with: "Failed to establish chain from reply")
                invokeKeyTool("-importcert" +
                        " -keystore " + path + TlsConstants.TLS_CLIENT_CHAIN_KEY_STORE +
                        " -storepass " + TlsConstants.TLS_CLIENT_CHAIN_KEY_PASS +
                        " -storetype " + TlsConstants.STORE_TYPE +
                        " -alias " + TlsConstants.TLS_CLIENT_KEY_ALIAS +
                        " -file " + path + TlsConstants.TLS_CLIENT_CERT +
                        " -trustcacerts" +
                        " -noprompt"
                );

                // import certificate in client chain store
                invokeKeyTool("-importcert" +
                        " -keystore " + path + TlsConstants.TLS_CLIENT_CHAIN_KEY_STORE +
                        " -storepass " + TlsConstants.TLS_CLIENT_CHAIN_KEY_PASS +
                        " -storetype " + TlsConstants.STORE_TYPE +
                        // must use same alias: "Certificate reply was installed in keystore"
                        " -alias " + TlsConstants.TLS_CLIENT_CHAIN_KEY_ALIAS +
                        " -file " + path + TlsConstants.TLS_CLIENT_CHAIN_CERT +
                        " -trustcacerts" +
                        " -noprompt"
                );
            }
        }
    }

    /**
     * invoke keytool via reflection (avoid compile time dependency)
     *
     * @param argLine blank separated list of parameters (like on command line)
     * @throws Exception in case of problems
     */
    private static void invokeKeyTool(String argLine) throws Exception {
        LOG.info("invoke keytool with argLine: " + argLine);
        String[] args = argLine.split(" ");
        Class<?> clazz = Class.forName("sun.security.tools.keytool.Main");
        Method method = clazz.getMethod("main", String[].class);
        method.invoke(null, (Object) args);
    }
}
