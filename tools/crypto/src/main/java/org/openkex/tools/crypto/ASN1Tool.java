/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.tools.crypto;

import java.io.ByteArrayInputStream;
import java.util.Arrays;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.util.ASN1Dump;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ASN1Tool {

    private static final Logger LOG = LoggerFactory.getLogger(ASN1Tool.class);

    private ASN1Tool() {
    }

    /**
     * try to dump ASN.1 structure of byte array
     * <p>
     * DER encoding seems to work a little bit (not sure what BC actually does)
     *
     * @param name name for log
     * @param data data to dump
     * @param skipBytes bytes to skip
     */
    public static void dump(String name, byte[] data, int skipBytes) {
        if (skipBytes > 0) {
            data = Arrays.copyOfRange(data, skipBytes, data.length);
        }
        int i = 1;
        ASN1InputStream ais = new ASN1InputStream(new ByteArrayInputStream(data));
        try {
            while (ais.available() > 0) {
                ASN1Primitive obj = ais.readObject();
                LOG.info("asn1 content of " + name + " (seq:" + i++ + "):\n" + ASN1Dump.dumpAsString(obj, true));
            }
        }
        catch (Exception e) {
            LOG.warn("asn1 content of " + name + " (seq:" + i + ") failed with:" + e);
        }
    }

    /**
     * try to dump ASN.1 structure of byte array
     *
     * @param name name for log
     * @param data data to dump
     */
    public static void dump(String name, byte[] data) {
        dump(name, data, 0);
    }
}
