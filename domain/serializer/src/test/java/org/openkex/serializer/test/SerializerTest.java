/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.serializer.test;

import org.junit.Assert;
import org.junit.Test;
import org.openkex.dto.ClientTokenInfo;
import org.openkex.dto.SignatureAlgorithm;
import org.openkex.dto.SignedStatements;
import org.openkex.dto.StatementAttributes;
import org.openkex.dto.StatementCertificateOwner;
import org.openkex.dto.StatementClaimKexId;
import org.openkex.dto.StatementPublicAccountOwner;
import org.openkex.dto.Statements;
import org.openkex.serializer.ProtobufSerializer;
import org.openkex.serializer.SerializeService;
import org.openkex.tools.Hex;
import org.openkex.tools.NumberByteConverter;
import org.openkex.tools.RandomCheck;
import org.openkex.tools.RandomTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SerializerTest {

    private static final Logger LOG = LoggerFactory.getLogger(SerializerTest.class);

    @Test
    public void testStatementClaimKexId() throws Exception {
        SerializeService serializer = new ProtobufSerializer();
        byte[] key = {0, 1, 2, 3, 5};  // just a dummy key
        StatementClaimKexId statement = new StatementClaimKexId(SignatureAlgorithm.ED25519, key);
        testEncodeDecode(statement, serializer, true);
    }

    @Test
    public void testStatementPublicAccountOwner() throws Exception {
        SerializeService serializer = new ProtobufSerializer();
        StatementPublicAccountOwner statement = new StatementPublicAccountOwner(StatementPublicAccountOwner.Type.GITHUB, "igitti");
        testEncodeDecode(statement, serializer, true);
    }

    @Test
    public void testStatementCertificateOwner() throws Exception {
        SerializeService serializer = new ProtobufSerializer();
        byte[] cert = {0, 1, 2, 3, 5};  // just a dummy cert
        byte[] sig = {4, 2, 33, 3, 5};  // just a dummy sing
        StatementCertificateOwner statement = new StatementCertificateOwner(
                StatementCertificateOwner.CertificateType.PGP, cert, sig
        );
        testEncodeDecode(statement, serializer, true);
    }

    @Test
    public void testStatementAttributes() throws Exception {
        SerializeService serializer = new ProtobufSerializer();
        StatementAttributes statement = new StatementAttributes(StatementAttributes.Type.EMAIL, "ali@scha.de", null);
        testEncodeDecode(statement, serializer, true);
    }

    @Test
    public void testStatements() throws Exception {
        SerializeService serializer = new ProtobufSerializer();
        byte[] key = {0, 1, 2, 3, 5};  // just a dummy key
        StatementClaimKexId statement = new StatementClaimKexId(SignatureAlgorithm.ED25519, key);
        byte[] kexIdBytes = {1, 2, 3, 4, 51, 6};
        long kexId = NumberByteConverter.sixBytesToLong(kexIdBytes);
        byte[] previousHash = {1, 2, 3, 41, 51, 6};
        Statements statements = new Statements(
                kexId,
                System.currentTimeMillis() / 1000,
                previousHash,
                333,
                statement,
                null,
                null
        );
        testEncodeDecode(statements, serializer, true);

        byte[] signature = {0, 1, 2, 3, 5, 1, 2, 3};  // just a dummy signature
        SignedStatements signedStatements = new SignedStatements(statements, signature);

        testEncodeDecode(signedStatements, serializer, true);
    }

    private void testEncodeDecode(Object obj, SerializeService serializer, boolean log) throws Exception {
        byte[] encoded = serializer.serialize(obj);
        if (log) {
            LOG.info("input: " + obj);
            LOG.info("encoded (" + encoded.length + " bytes): " + Hex.toString(encoded));
        }
        Object obj2 = serializer.deserialize(encoded, obj.getClass());
        Assert.assertEquals(obj, obj2);
    }

    @Test
    public void testClientTokenInfo() throws Exception {
        SerializeService serializer = new ProtobufSerializer();
        byte[] kexIdBytes = Hex.fromString("F516DF2C3341");
        Assert.assertTrue(RandomCheck.estimateRandom(kexIdBytes));
        long kexId = NumberByteConverter.sixBytesToLong(kexIdBytes);
        long expiry = System.currentTimeMillis() / 1000 + 60;
        ClientTokenInfo info = new ClientTokenInfo(kexId, expiry);
        testEncodeDecode(info, serializer, true);

        // check sample bytes: 08 C1E6B0F9EDA23D 10 FFC0CACD05
        // kexId using 7 byte and time_t using 5 byte (both type "uint64" with "varint" encoding), confirming #718-6
        byte[] test = Hex.fromString("08" + "C1E6B0F9EDA23D" + "10" + "FFC0CACD05");
        LOG.info("testing: " + serializer.deserialize(test, ClientTokenInfo.class));
    }

    @Test
    public void testSizeAndSpeed() throws Exception {
        // test with correct sizes...
        RandomTool random = new RandomTool();
        SerializeService serializer = new ProtobufSerializer();
        byte[] key = random.getBytes(32);
        StatementClaimKexId statement = new StatementClaimKexId(SignatureAlgorithm.ED25519, key);
        byte[] kexIdBytes = random.getBytes(6);
        long kexId = NumberByteConverter.sixBytesToLong(kexIdBytes);
        byte[] previousHash = random.getBytes(32);
        Statements statements = new Statements(
                kexId,
                System.currentTimeMillis() / 1000,
                previousHash,
                333,
                statement,
                null,
                null
        );
        testEncodeDecode(statements, serializer, true);

        byte[] signature = random.getBytes(64); // just a dummy signature
        SignedStatements signedStatements = new SignedStatements(statements, signature);

        testEncodeDecode(signedStatements, serializer, true);

        int repeats = 200000;
        long start = System.nanoTime();
        for (int i = 0; i < repeats; i++) {
            testEncodeDecode(signedStatements, serializer, false);
        }
        long time = System.nanoTime() - start;
        LOG.info("serialize/deserialize. repeats " + repeats + " times took " + (time / 1000000) +
                "ms. each round took " + (time / repeats) + "ns");
    }

}
