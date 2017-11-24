/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.cardio.atcitizen;

import org.openkex.cardio.common.APDUTool;
import org.openkex.tools.Hex;
import org.openkex.tools.crypto.ECCTool;

import javax.smartcardio.CardChannel;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

/**
 * The Austrian Citizen Card (Bürgerkarte) enables Austrian citizen to use "qualified signatures".
 * <p>
 * The citizen certificate is signed by a-trust,
 * it holds Name, Title and a Cardholder Identification Number (CIN) store as "serial number" in subjects common name.
 * <p>
 * technically it uses ECC (P-256 = secp256r1) for signing.
 * <p>
 * see https://www.buergerkarte.at
 * see https://www.a-trust.at
 */
public class AustrianCitizenCard {

    private static final byte[] AID_QS = Hex.fromString("D040000017001201");
    private static final byte[] FID_QS = Hex.fromString("C000");
    private static final byte[] FID_QSR = Hex.fromString("C608"); // issuer certificate
    private static final byte[] AID_BS = Hex.fromString("D040000017001301");
    private static final byte[] FID_BS = Hex.fromString("2F01");
    private static final byte[] AID_LINK = Hex.fromString("D040000017001801");
    private static final byte[] FID_LINK = Hex.fromString("C002");

    private CardChannel channel;
    private byte[] qualifiedCertificate;
    private byte[] qualifiedRootCertificate;
    private byte[] basicCertificate;
    private byte[] link;

    /**
     * constructor assumed valid citizen card connected
     *
     * @param channel card channel to use
     * @throws Exception if card communication fails
     */
    public AustrianCitizenCard(CardChannel channel) throws Exception {

        this.channel = channel;
        // select master file
        APDUTool.processAPDU(channel, new CommandAPDU(0x00, 0xa4, 0x00, 0x0c, 0x00), true);

        ResponseAPDU r = APDUTool.processAPDU(channel, new CommandAPDU(0x00, 0xa4, 0x04, 0x00, AID_QS, 0xff), false);
        if (APDUTool.isOK(r)) {
            qualifiedCertificate = APDUTool.readBinary(channel, FID_QS);
            qualifiedRootCertificate = APDUTool.readBinary(channel, FID_QSR);
        }

        r = APDUTool.processAPDU(channel, new CommandAPDU(0x00, 0xa4, 0x04, 0x00, AID_BS, 0xff), false);
        if (APDUTool.isOK(r)) {
            basicCertificate = APDUTool.readBinary(channel, FID_BS);
        }

        r = APDUTool.processAPDU(channel, new CommandAPDU(0x00, 0xa4, 0x04, 0x00, AID_LINK, 0xff), false);
        if (APDUTool.isOK(r)) {
            link = APDUTool.readBinary(channel, FID_LINK);
        }
    }

    public byte[] getQualifiedCertificate() {
        return qualifiedCertificate;
    }

    /**
     * outdated certificate on card
     *
     * @return byte array for root certificate
     */
    @Deprecated
    public byte[] getQualifiedRootCertificate() {
        return qualifiedRootCertificate;
    }

    public byte[] getBasicCertificate() {
        return basicCertificate;
    }

    public byte[] getLink() {
        return link;
    }

    // TODO fix password parameter, exception is no proper way to deal with wrong pin
    // currently only qualified..
    public byte[] sign(byte[] sha256, byte[] password) throws Exception {
        APDUTool.processAPDU(channel, new CommandAPDU(0x00, 0xa4, 0x04, 0x00, AID_QS, 0xff), true);
        ResponseAPDU r = APDUTool.processAPDU(channel, new CommandAPDU(0x00, 0x20, 0x00, 0x81, password, 0x00), true);
        if (r.getSW() != 0x9000) {
            throw new RuntimeException("wrong pin");
        }
        r = APDUTool.processAPDU(channel, new CommandAPDU(0x00, 0x2a, 0x9e, 0x9a, sha256, 0xff), false);
        byte[] rawSig = r.getData();

        // need to convert signature to java specific ASN1 integer sequence (why?):
        return ECCTool.encodeRawSignature(rawSig);
    }

    /**
     * @param atr the ATR to check
     * @return true if card is supported (only STARCOS G3, G4)
     */
    public static boolean matchAtr(byte[] atr) {
        String atrString = Hex.toString(atr);
        // G3: ATR starts with: 3b:dd:96:ff:81:b1:fe:45:1f:03
        if (atrString.startsWith("3BDD96FF81B1FE451F03")) {
            return true;
        }
        // G4: ATR starts with: 3b:df:18:00:81:31:fe:58
        if (atrString.startsWith("3BDF18008131FE58")) {
            return true;
        }
        return false;
    }
}
