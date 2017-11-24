/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.server.backend.test;

import org.junit.Test;

/**
 * load test, will not run by default as class name does not end with "Test"
 * <p>
 * invoke with: mvn clean install -Dtest=SimulatedClientTestLoad -DfailIfNoTests=false | grep Timer
 */
public class SimulatedClientTestLoad {

    static {
        // may not work if log4j2 is already initialized
        System.setProperty("log4j2.level", "warn"); // see log4j2.xml
    }

    @Test
    public void multiTestLoad() throws Exception {
//        SimulatedClientTest.multiTest(3, 99999, 4, false); // 80s
//        SimulatedClientTest.multiTest(3, 9999, 0, false); // 21s
//        SimulatedClientTest.multiTest(3, 9999, 4, false); // 8s
//        SimulatedClientTest.multiTest(33, 9999, 4, false); // 55s
        SimulatedClientTest.multiTest(33, 9999, 32, false); // 44s
//        SimulatedClientTest.multiTest(33, 9999, 32, true); /// 4s
//        SimulatedClientTest.multiTest(99, 9999, 4, false); // 155s
//        SimulatedClientTest.multiTest(33, 999, 0, false); // 17s
//        SimulatedClientTest.multiTest(33, 999, 4, false); // 5s
    }
}
