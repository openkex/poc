/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * tool to evaluate execution time (and split times)
 */
public class Timer {

    private static final Logger LOG = LoggerFactory.getLogger(Timer.class);

    private String action;
    private long start;
    private long lastSplit;
    private String splitAction;
    private long duration;

    /**
     * create and start timer with defined action
     *
     * @param action action used for logging
     * @param log if true log start
     */
    public Timer(String action, boolean log) {
        this.duration = 0;
        this.action = action;
        this.start = System.nanoTime();
        this.lastSplit = start;
        this.splitAction = null;
        if (log) {
            LOG.info("start " + action);
        }
    }

    /**
     * start timer with defined action and split action
     *
     * @param action action used for logging
     * @param splitAction initial split action
     * @param log if true log start
     */
    public Timer(String action, String splitAction, boolean log) {
        this.action = action;
        this.splitAction = splitAction;
        this.start = System.nanoTime();
        this.lastSplit = start;
        if (log) {
            LOG.info("start " + action + "/" + splitAction);
        }
    }

    /**
     * log intermediate time (show total time and time since last split)
     *
     * @param newSplitAction next split action used for logging
     * @param message message about last action (what was done)
     * @param log if true log split
     */
    public void split(String newSplitAction, String message, boolean log) {
        long splitTime = System.nanoTime();
        long splitDelta = splitTime - lastSplit;
        long current = splitTime - start;
        if (log) {
            String messageStr = message == null ? "" : "(" + message + ")";
            LOG.info("split " + action + "/" + newSplitAction + " " +
                    splitAction + messageStr + "=" + getTimeString(splitDelta, true) +
                    " total=" + getTimeString(current, true));
        }
        lastSplit = splitTime;
        splitAction = newSplitAction;
    }

    /**
     * log intermediate time (show total time and time since last split)
     *
     * @param newSplitAction next split action used for logging
     * @param log if true log split
     */
    public void split(String newSplitAction, boolean log) {
        split(newSplitAction, null, log);
    }

    /**
     * stop timer
     *
     * @param message message about what was completed
     * @param log if true log end with duration and action
     */
    public void stop(String message, boolean log) {
        Validate.isTrue(duration == 0, "timer already stopped.");
        long end = System.nanoTime();
        String messageStr = message == null ? "" : "(" + message + ")";
        duration = end - start;
        if (log) {
            if (splitAction == null) {
                LOG.info("done  " + action + messageStr + " took=" + getTimeString(duration, true));
            }
            else {
                long splitDelta = end - lastSplit;
                LOG.info("done  " + action + "/" + splitAction + " "
                        + splitAction + messageStr + "=" + getTimeString(splitDelta, true) +
                        " total=" + getTimeString(duration, true));
            }
        }
    }

    /**
     * stop timer
     *
     * @param log if true log end with duration and action
     */
    public void stop(boolean log) {
        stop(null, log);
    }

    /**
     * @return duration in nanoseconds
     */
    public long getDuration() {
        return duration;
    }

    /**
     * get string representation of time
     *
     * @param fractions show fractions of milliseconds (microseconds)
     * @param time time value in nanoseconds
     * @return time String
     */
    public String getTimeString(long time, boolean fractions) {
        if (fractions) {
            // use formatter, could speed up with
            // show millis with 3 digits (i.e. microseconds)
            DecimalFormat df = new DecimalFormat("#.###", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
            return df.format(time / 1000000.0) + "ms";
        }
        else {
            return time / 1000000 + "ms";
        }
    }
}
