/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.cardio.atcitizen.test;

import org.openkex.cardio.atcitizen.AustrianCitizenCard;
import org.openkex.cardio.atcitizen.CertificateData;
import org.openkex.cardio.common.PinTool;
import org.openkex.cardio.common.TerminalTool;
import org.openkex.tools.Hex;
import org.openkex.tools.crypto.ASN1Tool;
import org.openkex.tools.crypto.ECCTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardTerminal;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class AustrianCitizenCardTest {

    private static final Logger LOG = LoggerFactory.getLogger(AustrianCitizenCardTest.class);

    private AustrianCitizenCardTest() {
    }

    /**
     * currently as static main, does not run as unit test as it requires reader and card to be present
     */
    public static void main(String[] args) throws Exception {

        TerminalTool terminalTool = new TerminalTool();
        if (terminalTool.getStatus() != TerminalTool.Status.OK) {
            LOG.info("could not get terminal. status=" + terminalTool.getStatus());
            return;
        }
        CardTerminal terminal = terminalTool.get();
        LOG.info("using terminal: " + terminal.getName());

        if (!terminal.isCardPresent()) {
            LOG.info("no card present.");
            return;
        }

        // javadoc "protocol": the protocol to use ("T=0", "T=1", or "T=CL"), or "*" to connect using any available protocol.
        Card card = terminal.connect("*");

        byte[] atr = card.getATR().getBytes();
        LOG.info("found card with ATR=" + Hex.toString(atr));

        if (!AustrianCitizenCard.matchAtr(atr)) {
            LOG.info("Card is not a supported Austrian citizen card (Starcos G3+)");
            return;
        }

        CardChannel channel = card.getBasicChannel();
        AustrianCitizenCard atCard = new AustrianCitizenCard(channel);

        if (atCard.getQualifiedCertificate() == null) {
            LOG.info("card has no signature function");
            return;
        }

        LOG.info("qualified certificate bytes:\n" + Hex.toStringBlock(atCard.getQualifiedCertificate()));
        ASN1Tool.dump("qualified certificate", atCard.getQualifiedCertificate());

        // note: wrong (expired 2014-12-03) root certificate on card.
        X509Certificate issuerCert = getCertificate(atCard.getQualifiedRootCertificate());
        LOG.info("root DN=" + issuerCert.getIssuerDN());
        LOG.info("root valid from: " + issuerCert.getNotBefore() + " until: " + issuerCert.getNotAfter());
        try {
            issuerCert.checkValidity();
            LOG.info("root valid");
        }
        catch (Exception e) {
            LOG.info("root NOT valid");
        }

        LOG.info("basic certificate bytes:\n" + Hex.toStringBlock(atCard.getBasicCertificate()));
        ASN1Tool.dump("basic certificate", atCard.getBasicCertificate());

        LOG.info("link bytes:\n" + Hex.toStringBlock(atCard.getLink()));
        ASN1Tool.dump("link", atCard.getLink(), 8);

        X509Certificate cert = getCertificate(atCard.getQualifiedCertificate());

        // this string references issuer certificate.
        // CN=a-sign-Premium-Sig-02, OU=a-sign-Premium-Sig-02, O=A-Trust Ges. f. Sicherheitssysteme im elektr. Datenverkehr GmbH, C=AT
        LOG.info("issuer=" + cert.getIssuerDN());
        LOG.info("subject=" + cert.getSubjectDN());
        LOG.info("pubkey=" + cert.getPublicKey());  // P-256, secp256r1

        CertificateData data = new CertificateData(cert);
        LOG.info("certificate data parsed: " + data);
        try {
            cert.checkValidity();  // OK with java keystore?
            LOG.info("certificate is VALID");
        }
        catch (Exception e) {
            LOG.error("certificate is NOT VALID!");
            return;
        }

        // suggestion: set pin to 123456 while testing and then back to secret pin.
//         char[] pin = new char[] {'1', '2', '3', '4', '5', '6'};
        char[] pin = null;

        if (pin == null) {
            return;
        }

        // test signing
        byte[] fakeMessage = Hex.fromString("001122334455667788990011223344556677889900");
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(fakeMessage);
        PinTool pinTool = new PinTool(6, 12);
        pinTool.setInput(pin);

        byte[] signature = atCard.sign(digest, pinTool.getEncoded());
        pinTool.purge();

        // verify the signature with public key from certificate
        boolean ok = ECCTool.verify(fakeMessage, signature, cert.getPublicKey());
        LOG.info("signature verify: " + ok);
    }

    private static X509Certificate getCertificate(byte[] bytes) throws Exception {
        InputStream in = new ByteArrayInputStream(bytes);
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        return (X509Certificate) certFactory.generateCertificate(in);
    }
}
