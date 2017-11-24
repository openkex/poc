/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.cardio.common.test;

import org.junit.Test;
import org.openkex.cardio.common.TerminalTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.smartcardio.CardTerminal;

public class TerminalToolTest {

    private static final Logger LOG = LoggerFactory.getLogger(TerminalToolTest.class);

    @Test
    public void testBasic() throws Exception {
        TerminalTool terminalTool = new TerminalTool();
        LOG.info("status: " + terminalTool.getStatus());
        if (terminalTool.getStatus() == TerminalTool.Status.MISSING) {
            return;
        }
        for (CardTerminal terminal : terminalTool.getTerminals()) {
            LOG.info("terminal name=" + terminal.getName() + " card present=" + terminal.isCardPresent());
        }
    }
}
