/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.cardio.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.smartcardio.CardTerminal;
import javax.smartcardio.TerminalFactory;
import java.util.ArrayList;
import java.util.List;

public class TerminalTool {

    private static final Logger LOG = LoggerFactory.getLogger(TerminalTool.class);

    public enum Status {
        /** found no terminal */
        MISSING,
        /** found multiple terminals */
        MULTIPLE,
        /** found one terminals */
        OK
    }

    private List<CardTerminal> terminals = null;
    private Status status;

    public TerminalTool() {
        try {
            // no proper way to find out that card reader (aka terminal) is not plugged in (or does not exist)?
            // terminals.isEmpty() not always working ..
            // this throws: sun.security.smartcardio.PCSCException: SCARD_E_NO_READERS_AVAILABLE
            TerminalFactory factory = TerminalFactory.getDefault();
            terminals = factory.terminals().list();
        }
        catch (Exception e) {
            // could "parse" exception message in stack for unspecified text but this is "unclean".
            Throwable logEx = e.getCause() == null ? e : e.getCause();
            LOG.info("found no terminals (exception: " + logEx + ")");
            status = Status.MISSING;
            return;
        }

        if (terminals.isEmpty()) {
            LOG.info("found no terminal."); // second possible "no terminal" case.
            status = Status.MISSING;
            return;
        }

        if (terminals.size() > 1) {
            status = Status.MULTIPLE;
            return;
        }

        status = Status.OK;
    }

    /**
     * @return terminal status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * @return terminal. assume one terminal attached.
     */
    public CardTerminal get() {
        if (this.status == Status.OK) {
            return this.terminals.get(0);
        }
        throw new RuntimeException("cannot return terminal. status=" + status);
    }

    /**
     * @param index terminal index
     * @return selected terminal
     */
    public CardTerminal get(int index) {
        return this.terminals.get(index);
    }

    /**
     * @return list of terminal names
     */
    public List<String> getNames() {
        ArrayList<String> ret = new ArrayList<String>();
        for (int i = 0; i < terminals.size(); i++) {
            ret.add(terminals.get(i).getName());
        }
        return ret;
    }

    /**
     * @return available terminals
     */
    public List<CardTerminal> getTerminals() {
        return terminals;
    }
}
