/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.cardio.smartcardhsm.test;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openkex.cardio.common.TerminalTool;
import org.openkex.cardio.common.cvc.CVCTagLengthValue;
import org.openkex.cardio.common.cvc.CVCTool;
import org.openkex.cardio.common.cvc.CVCertificateRequest;
import org.openkex.cardio.smartcardhsm.SmartCardHsm;
import org.openkex.tools.Entropy;
import org.openkex.tools.Hex;
import org.openkex.tools.RandomTool;
import org.openkex.tools.Timer;
import org.openkex.tools.crypto.ECCTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.smartcardio.Card;
import javax.smartcardio.CardTerminal;
import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.util.Arrays;

/**
 * test expects a Reader with correctly prepared (i.e. initialized with user pin "123456") SmartCardHSM.
 * <p>
 * Notes:
 * <p>
 * reader setup happens in @BeforeClass
 * each @Test will reconnect card.
 * automatic test is prevented by not matching *Test (with *TestHW)
 * execution is possible with "mvn test -Dtest=SmartCardHsmBasicTestHW"
 *
 */
public class SmartCardHsmBasicTestHW {

    private static final Logger LOG = LoggerFactory.getLogger(SmartCardHsmTest.class);

    private static final byte[] TEST_USER_PIN = "123456".getBytes(Charset.forName("ASCII"));
    private static final byte[] TEST_SO_PIN = Hex.fromString("1111111111111111");

    private static CardTerminal terminal;
    private Card card;
    private SmartCardHsm smartCardHsm;

    @BeforeClass
    public static void setup() throws Exception {
        TerminalTool terminalTool = new TerminalTool();

        if (terminalTool.getStatus() == TerminalTool.Status.MISSING) {
            throw new RuntimeException("terminal missing.");
        }
        // select first terminal with card present
        for (CardTerminal t : terminalTool.getTerminals()) {
            if (t.isCardPresent()) {
                terminal = t;
                break;
            }
        }
        if (terminal == null) {
            throw new RuntimeException("found no terminal with  card present.");
        }
        LOG.info("using terminal: " + terminal.getName());
    }

    @Before
    public void connect() throws Exception {
        card = terminal.connect("*");
        byte[] atr = card.getATR().getBytes();
        LOG.info("Connect: found card with ATR=" + Hex.toString(atr));

        if (!SmartCardHsm.matchAtr(atr)) {
            throw new RuntimeException("Card is not a SmartCard HSM");
        }
        smartCardHsm = new SmartCardHsm(card.getBasicChannel());
    }

    private void checkInitialized() {
        if (!smartCardHsm.isInitialized()) {
            throw new RuntimeException("card not initialized.");
        }
    }

    @After
    public void disconnect() throws Exception {
        LOG.info("disconnect");
        if (smartCardHsm != null) {
            smartCardHsm.close();
        }
        card.disconnect(true);
    }

    @Test
    public void testRandom() throws Exception {
        byte[] random = smartCardHsm.getRandom(128);
        LOG.info("random(128): " + Hex.toString(random));
    }

    @Test
    public void testRandomSpeed() throws Exception {
        Timer t = new Timer("random 1024", false);
        byte[] random = smartCardHsm.getRandom(1024); // 1.5s with SSK, 0.2s with Gemalto
//        LOG.info("random(1024):\n" + Hex.toStringBlock(random));
        t.stop(true);
        double e = Entropy.getShannonEntropy(random);
        LOG.info("random(1024) entropy=" + e); // approx 7.8 (7.77-7.83)
        // Note: high entropy is "necessary but not sufficient"
        Assert.assertTrue(e > 7.6);
    }

    @Test
    public void testRandomLimit() throws Exception {
        try {
            smartCardHsm.getRandom(1025); // too much...
            Assert.fail();
        }
        catch (Exception e) {
            LOG.info("expected: " + e);
        }
    }

    @Test
    public void testAuthContext() throws Exception {
        checkInitialized();
        byte[] fileId = Hex.fromString("CE88");
        smartCardHsm.verifyPIN(TEST_USER_PIN);
        smartCardHsm.deleteFile(fileId);
        // disconnect removes authentication
        disconnect();
        connect();
        try {
            smartCardHsm.deleteFile(fileId);
        }
        catch (Exception e) {
            LOG.info("expected: " + e);  // 0x6982
        }
    }

    @Test
    public void testPin() throws Exception {
        checkInitialized();
        int retires = smartCardHsm.verifyPIN(null);
        int maxRetries = 3;
        Assert.assertEquals(maxRetries, retires);
        byte[] correctPassword = TEST_USER_PIN;
        byte[] badPassword = "223456".getBytes("ASCII");

        int status = smartCardHsm.verifyPIN(correctPassword);
        Assert.assertEquals(-1, status);

        status = smartCardHsm.verifyPIN(badPassword);
        Assert.assertEquals(maxRetries - 1, status);

        retires = smartCardHsm.verifyPIN(null);
        Assert.assertEquals(maxRetries - 1, retires);

        // reset with correct password
        status = smartCardHsm.verifyPIN(correctPassword);
        Assert.assertEquals(-1, status);

//        retires = smartCardHsm.verifyPIN(false, null);  // not working? why? cannot get retires if "logged in".
//        Assert.assertEquals(3, retires); // actual "0"
    }

    @Test
    public void testChangePin() throws Exception {
        checkInitialized();
        int retires = smartCardHsm.verifyPIN(null);
        int maxRetries = 3;
        Assert.assertEquals(maxRetries, retires);
        byte[] oldPassword = TEST_USER_PIN;
        byte[] newPassword = "333444".getBytes("ASCII");

        // login with old password is OK?
        int status = smartCardHsm.verifyPIN(oldPassword);
        Assert.assertEquals(-1, status);

        // change to new password
        status = smartCardHsm.changePIN(false, oldPassword, newPassword);
        Assert.assertEquals(-1, status);

        // login with new password OK?
        status = smartCardHsm.verifyPIN(newPassword);
        Assert.assertEquals(-1, status);

        // change back to old password
        status = smartCardHsm.changePIN(false, newPassword, oldPassword);
        Assert.assertEquals(-1, status);
    }

    @Test
    @Ignore("working but avoid messing around with SO pin.")
    public void testChangeSoPin() throws Exception {
        checkInitialized();
        byte[] oldPassword = TEST_SO_PIN;
        byte[] newPassword = Hex.fromString("0123456789ABCDEF");

        // change to new password
        int status = smartCardHsm.changePIN(true, oldPassword, newPassword);
        Assert.assertEquals(-1, status);

        // change back to old password
        status = smartCardHsm.changePIN(true, newPassword, oldPassword);
        Assert.assertEquals(-1, status);
    }

    @Test
    @Ignore("working but avoid initialize.")
    public void testInitialize() throws Exception {
        LOG.info("initialized: " + smartCardHsm.isInitialized());
        smartCardHsm.initialize(TEST_SO_PIN, TEST_USER_PIN);
    }

    @Test
    public void testList() throws Exception {
        byte[][] files = smartCardHsm.listObjects();
        Assert.assertTrue(hasValue(files, Hex.fromString("2F02")));
        Assert.assertTrue(hasValue(files, Hex.fromString("CC00")));

        // fresh (non initialized) card / initialized card : 2F02 CC00

        // car with one key: 2F02, CE01, C401, CC00, CC01
    }

    @Test
    public void testGenerateSample() throws Exception {
        checkInitialized();
        dumpFileInternal("CE05");
    }

    private void dumpFileInternal(String fileId) throws Exception {
        byte[] file = null;
        try {
            file = smartCardHsm.readFile(Hex.fromString(fileId));
            LOG.info("File: " + fileId + ":\n" + Hex.toStringBlock(file));
        }
        catch (Exception e) {
            LOG.info("failed to read File: " + fileId + " exception: " + e);
            return;
        }
        try {
            CVCTool.dump(new ByteArrayInputStream(file));
        }
        catch (Exception e) {
            LOG.info("no valid CVC dump.");
        }
    }

    private boolean hasValue(byte[][] array, byte[] value) {
        for (byte[] data : array) {
            if (Arrays.equals(data, value)) {
                return true;
            }
        }
        return false;
    }

    @Test
    public void testDeviceCertificate() throws Exception {
        smartCardHsm.checkDeviceCertificate();
    }

    @Test
    public void testReadPrivateKey() throws Exception {
        checkInitialized();
        try {
            smartCardHsm.verifyPIN(TEST_USER_PIN);
            smartCardHsm.readFile(Hex.fromString("CC00")); // private key
            Assert.fail();
        }
        catch (Exception e) {
            LOG.info("expected: " + e);  // 0x6982 = Security status not satisfied
        }
    }

    @Test
    public void testReadWrite() throws Exception {
        checkInitialized();
        byte[] fileId = Hex.fromString("CE88");

        readWriteInternal(fileId, new RandomTool(234).getBytes(1));
        readWriteInternal(fileId, new RandomTool(235).getBytes(127));
        readWriteInternal(fileId, new RandomTool(234).getBytes(233));
        byte[] content = new byte[133];
        readWriteInternal(fileId, content);
        Arrays.fill(content, (byte) 0x33);
        readWriteInternal(fileId, content);
    }

    @Test
    public void testReadMissing() throws Exception {
        checkInitialized();
        byte[] fileId = Hex.fromString("3400");
        smartCardHsm.verifyPIN(TEST_USER_PIN);
        smartCardHsm.deleteFile(fileId); // delete to be sure
        Assert.assertNull(smartCardHsm.readFile(fileId));
    }

    @Test
    public void testReadWriteOdd() throws Exception {
        checkInitialized();
        byte[] fileId = Hex.fromString("CE88");
        smartCardHsm.verifyPIN(TEST_USER_PIN);
        smartCardHsm.deleteFile(fileId);  // extra delete for stability

        byte[] content = new RandomTool(235).getBytes(127);
        smartCardHsm.updateFile(fileId, content);
        byte[] content2 = new RandomTool(2351).getBytes(125);
        smartCardHsm.updateFile(fileId, content2);  // write less bytes

        byte[] readContent = smartCardHsm.readFile(fileId);

        Assert.assertEquals(readContent.length, content.length); // old extra bytes are "remaining"
    }

    private void readWriteInternal(byte[] fileId, byte[] content) throws Exception {
        checkInitialized();
        smartCardHsm.verifyPIN(TEST_USER_PIN);

        smartCardHsm.deleteFile(fileId);  // extra delete for stability

        smartCardHsm.updateFile(fileId, content);
        byte[] readContent = smartCardHsm.readFile(fileId);
        Assert.assertArrayEquals(content, readContent);

        Assert.assertTrue(smartCardHsm.deleteFile(fileId));  // delete should succeed
    }

    @Test
    public void testEccSpec() throws Exception {
        byte[] spec = Hex.fromString(SmartCardHsm.BP256R1_STRING);
        CVCTool.dump(new ByteArrayInputStream(spec));
        LOG.info("");
        spec = Hex.fromString(SmartCardHsm.SECP256K1_STRING);
        CVCTool.dump(new ByteArrayInputStream(spec));
    }

    @Test
    public void testGeneratePair() throws Exception {
        checkInitialized();
        byte keyId = 5;
        smartCardHsm.verifyPIN(TEST_USER_PIN);

        smartCardHsm.deleteKey(keyId);

        Timer t = new Timer("generate", false);
        smartCardHsm.generatePair(SmartCardHsm.SECP256K1, keyId, 0);
        t.stop(true);

        // sign on card
        byte[] message = new RandomTool(34342).getBytes(64); // 64 random bytes with fixed seed.
        byte[] messageHash = MessageDigest.getInstance("SHA-256").digest(message);

        t = new Timer("sign", false);
        byte[] signature = smartCardHsm.sign(messageHash, keyId);
        t.stop(true);
        LOG.info("signature=" + Hex.toString(signature));

        // test signature "without card"
        CVCTagLengthValue publicKeyTag = smartCardHsm.getPublicKey(keyId);
        LOG.info("PK=" + Hex.toString(publicKeyTag.getContent()));
        PublicKey pk = ECCTool.getPublicKeyFromCurvePoint(publicKeyTag.getContent(), SmartCardHsm.SECP256K1);

        boolean valid = ECCTool.verify(message, signature, pk);
        Assert.assertTrue(valid);
    }

    @Test
    public void testReadCSR() throws Exception {
        checkInitialized();
        byte[] csr = smartCardHsm.readFile(Hex.fromString("CE05"));
        CVCTool.dump(new ByteArrayInputStream(csr));
        CVCertificateRequest request = CVCTool.parseRequest(new ByteArrayInputStream(csr));
        LOG.info("CVC request=" + request);
    }

    @Test
    public void testKeyValid() throws Exception {
        checkInitialized();
        smartCardHsm.checkKeySignature(5);
    }

    @Test
    public void testECDH() throws Exception {
        checkInitialized();
        // two parties, each with (EC) keypair

        // KeyPair local = // use key pair number 5 from card
        int localKeyId = 5;
        KeyPair remote = ECCTool.generate(ECCTool.CURVE_SECP256K1);

        // each party (local, remote) uses own secret and other's public key

        // remote: using local public key (read from smart card)
        CVCTagLengthValue publicKeyTag = smartCardHsm.getPublicKey(localKeyId);
        PublicKey localPublic = ECCTool.getPublicKeyFromCurvePoint(publicKeyTag.getContent(), SmartCardHsm.SECP256K1);
        byte[] secretRemote = ECCTool.getECDHSecret(remote.getPrivate(), localPublic);
        LOG.info("remote secret has " + secretRemote.length + " bytes, value=" + Hex.toString(secretRemote));

        // local: using smart card (protected private key)
        smartCardHsm.verifyPIN(TEST_USER_PIN);
        Timer t = new Timer("ECDH", false);
        byte[] secretLocal = smartCardHsm.eccDecode(localKeyId, ECCTool.encodeCurvePoint(remote.getPublic(), SmartCardHsm.SECP256K1, false));
        t.stop(true);

        // need to "truncate" to get same value as from java.
        byte[] secretLocalTruncated = Arrays.copyOfRange(secretLocal, 1, 33);

        LOG.info("local secret has " + secretLocal.length + " bytes, value=" + Hex.toString(secretLocal));

        // result is "same" secret for both parties.
        Assert.assertArrayEquals(secretLocalTruncated, secretRemote);
    }

}