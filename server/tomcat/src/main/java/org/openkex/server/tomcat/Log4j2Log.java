/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.server.tomcat;

import org.apache.juli.logging.Log;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * why is logging always broken?
 * <p>
 * activate with META-INF/services/org.apache.juli.logging.Log
 * <p>
 * NOTE: this class is inspired by org.apache.meecrowave.logging.tomcat.Log4j2Log
 */
public class Log4j2Log implements Log {
    private final Logger delegate;

    // for SPI?
    public Log4j2Log() {
        delegate = null;
    }

    public Log4j2Log(final String name) {
        delegate = LogManager.getLogger(name);
    }

    @Override
    public boolean isDebugEnabled() {
        return delegate.isDebugEnabled();
    }

    @Override
    public boolean isErrorEnabled() {
        return delegate.isErrorEnabled();
    }

    @Override
    public boolean isFatalEnabled() {
        return delegate.isFatalEnabled();
    }

    @Override
    public boolean isInfoEnabled() {
        return delegate.isInfoEnabled();
    }

    @Override
    public boolean isTraceEnabled() {
        return delegate.isTraceEnabled();
    }

    @Override
    public boolean isWarnEnabled() {
        return delegate.isWarnEnabled();
    }

    @Override
    public void trace(final Object message) {
        delegate.trace(message);
    }

    @Override
    public void trace(final Object message, final Throwable t) {
        delegate.trace(message, t);
    }

    @Override
    public void debug(final Object message) {
        delegate.debug(message);
    }

    @Override
    public void debug(final Object message, final Throwable t) {
        delegate.debug(message, t);
    }

    @Override
    public void info(final Object message) {
        delegate.info(message);
    }

    @Override
    public void info(final Object message, final Throwable t) {
        delegate.info(message, t);
    }

    @Override
    public void warn(final Object message) {
        delegate.warn(message);
    }

    @Override
    public void warn(final Object message, final Throwable t) {
        delegate.warn(message, t);
    }

    @Override
    public void error(final Object message) {
        delegate.error(message);
    }

    @Override
    public void error(final Object message, final Throwable t) {
        delegate.error(message, t);
    }

    @Override
    public void fatal(final Object message) {
        delegate.fatal(message);
    }

    @Override
    public void fatal(final Object message, final Throwable t) {
        delegate.fatal(message, t);
    }
}

