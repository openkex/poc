/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.tools.test;

import org.junit.Assert;
import org.junit.Test;
import org.openkex.tools.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimerTest {

    private static final Logger LOG = LoggerFactory.getLogger(TimerTest.class);

    @Test
    public void basicTest() throws Exception {
        Timer t = new Timer("testTimer", true);
        long sleepTime = 300;
        Thread.sleep(sleepTime);
        t.stop(true);
        try {
            t.stop(true); // must fail
            Assert.fail();
        }
        catch (Exception e) {
            LOG.info("expected exception: " + e);
        }
        long duration = t.getDuration() / 1000000; // nano to milli
        // check duration with some tolerance
        int tolerance = 30;  // windoze timer has 16ms resolution
        Assert.assertTrue(duration > sleepTime - tolerance);
        Assert.assertTrue(duration < sleepTime + tolerance);
    }

    @Test
    public void testTimerFast() {
        // the "fastest" timer case
        Timer t = new Timer("fast", false);
        t.stop(true);
    }

    @Test
    public void testTimerSplit() throws Exception {
        Timer t = new Timer("split test", "lap1", true);
        Thread.sleep(22);
        t.split("lap2", true);
        Thread.sleep(44);
        t.stop(true);
    }

    @Test
    public void testTimerSplitNoLog() throws Exception {
        Timer t = new Timer("split test", "lap1", false);
        Thread.sleep(22);
        t.split("lap2", false);
        Thread.sleep(44);
        t.stop(false);
    }

    @Test
    public void testTimerSplitMessage() throws Exception {
        Timer t = new Timer("split test", "lap1", true);
        Thread.sleep(22);
        t.split("lap2", "did some things", true);
        Thread.sleep(44);
        t.split("lap3", "did more things", true);
        Thread.sleep(55);
        t.stop("did final things", true);
    }

    @Test
    public void testTimerSplitMessageNoLog() throws Exception {
        Timer t = new Timer("split test", "lap1", false);
        Thread.sleep(22);
        t.split("lap2", "did some things", false);
        Thread.sleep(44);
        t.split("lap3", "did more things", false);
        Thread.sleep(55);
        t.stop("did final things", false);
    }

    @Test
    public void testTimerFraction() throws Exception {
        Timer t = new Timer("test33", false);
        Thread.sleep(33);
        t.stop(false);
        LOG.info("time millis: " + t.getTimeString(t.getDuration(), false));
        LOG.info("time fraction: " + t.getTimeString(t.getDuration(), true));
    }

}
