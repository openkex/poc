/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.cardio.common;

import org.openkex.tools.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.smartcardio.CardChannel;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import java.io.ByteArrayOutputStream;

public class APDUTool {

    private static final Logger LOG = LoggerFactory.getLogger(APDUTool.class);

    private APDUTool() {
    }

    public static ResponseAPDU processAPDU(CardChannel channel, CommandAPDU apdu, boolean assumeOK) throws Exception {
        LOG.debug("request (" + apdu.getBytes().length + " bytes):  " + Hex.toString(apdu.getBytes()));
        ResponseAPDU r = channel.transmit(apdu);
        LOG.debug("response (" + r.getBytes().length + " bytes): " + Hex.toString(r.getBytes()));
        if (!isOK(r)) {
            String message = "invalid response status: 0x" + Integer.toHexString(r.getSW());
            LOG.warn(message);
            if (assumeOK) {
                throw new RuntimeException(message);
            }
        }
        return r;
    }

    public static boolean isOK(ResponseAPDU r) {
        return r.getSW() == 0x9000 || r.getSW1() == 0x61 || r.getSW1() == 0x62 || r.getSW1() == 0x63;
    }

    public static byte[] readBinary(CardChannel channel, byte[] fileId) throws Exception {
        ResponseAPDU r = processAPDU(channel, new CommandAPDU(0x00, 0xa4, 0x02, 0x04, fileId, 0xff), false);
        if (isOK(r)) {
            int byteNr = 0;
            // pragmatic array concatenation
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            do {
                int p1 = byteNr / 256;
                int p2 = byteNr % 256;
                ResponseAPDU resp = processAPDU(channel, new CommandAPDU(0x00, 0xb0, p1, p2, 0xFF), true);
                byte[] data = resp.getData();
                baos.write(data);
                if (resp.getSW() != 0x9000) {
                    break;
                }
                byteNr += data.length;
            }
            while (true);
            return baos.toByteArray(); // full result
        }
        return null;
    }
}
