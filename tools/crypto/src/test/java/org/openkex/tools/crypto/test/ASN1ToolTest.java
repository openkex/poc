/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.tools.crypto.test;

import org.junit.Test;
import org.openkex.tools.Hex;
import org.openkex.tools.RandomTool;
import org.openkex.tools.crypto.ASN1Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ASN1ToolTest {

    private static final Logger LOG = LoggerFactory.getLogger(ASN1ToolTest.class);

    @Test
    public void testDump() {

        String test = "3046022100ad177938af520abd49567c8e72e9e733ed932d2a2ddf833fdf99939d5b83fa" +
                      "5c022100a522866a8014e2641cb1d671a13120f9d751aa206c00ff431fee8d94a030482a";

        ASN1Tool.dump("testing", Hex.fromString(test));

        ASN1Tool.dump("testing", Hex.fromString("11223344" + test), 4);

        ASN1Tool.dump("testing", Hex.fromString(test + test));
    }

    @Test
    public void testDumpFail() {
        ASN1Tool.dump("fail", new RandomTool(222).getBytes(22));
    }

}
