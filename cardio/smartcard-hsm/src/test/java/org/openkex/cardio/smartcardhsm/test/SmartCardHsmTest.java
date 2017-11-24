/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.cardio.smartcardhsm.test;

import org.openkex.cardio.common.TerminalTool;
import org.openkex.cardio.common.cvc.CVCTool;
import org.openkex.cardio.common.cvc.CVCertificate;
import org.openkex.cardio.smartcardhsm.SmartCardHsm;
import org.openkex.tools.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.smartcardio.Card;
import javax.smartcardio.CardTerminal;
import java.io.ByteArrayInputStream;

public class SmartCardHsmTest {

    private static final Logger LOG = LoggerFactory.getLogger(SmartCardHsmTest.class);

    private SmartCardHsmTest() {
    }

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

        Card card = terminal.connect("*");

        byte[] atr = card.getATR().getBytes();
        LOG.info("found card with ATR=" + Hex.toString(atr));

        if (!SmartCardHsm.matchAtr(atr)) {
            LOG.warn("Card is not a SmartCard HSM");
            return;
        }

        SmartCardHsm hsm = new SmartCardHsm(card.getBasicChannel());

//        LOG.info("DeviceCertificate: \n" + Hex.toStringBlock(hsm.getDeviceCertificate()));
//        FileTool.write(hsm.getDeviceCertificate(), "hsm.cer");

//        CVCTool.dump(new ByteArrayInputStream(hsm.getDeviceCertificate()));

        ByteArrayInputStream is = new ByteArrayInputStream(hsm.getDeviceCertificate());

        CVCertificate cvc1 = CVCTool.parse(is, false);
        CVCertificate cvc2 = CVCTool.parse(is, false);
        LOG.info("cvc1=" + cvc1);
        LOG.info("cvc2=" + cvc2);
    }
}
