/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.server.undertow;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class LogFilter implements Filter {

    private static final Logger LOG = LogManager.getLogger(LogFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String remoteAddress = null;

        long start = 0;
        String url = "";
        String method = "";
        Throwable throwable = null;
        String agent = null;
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        // see "Java Servlet Specification" Version 3.1: chapter 3.9 "SSL Attributes"
        // Note: the certificate chain is assumed to be valid.
        X509Certificate[] certs = (X509Certificate[]) req.getAttribute("javax.servlet.request.X509Certificate");
        if (certs != null) {
            for (X509Certificate certificate : certs) {
                LOG.info("client cert DN: " + certificate.getSubjectDN());
            }
        }

        try {
            url = req.getRequestURI();
            method = req.getMethod();
            String qs = req.getQueryString();
            agent = req.getHeader("User-Agent");
            if (qs != null) {
                url += "?" + qs;
            }
            String forwarded = req.getHeader("X-Forwarded-For");
            if (forwarded != null) {
                remoteAddress = forwarded;
            }
            else {
                remoteAddress = request.getRemoteAddr();
            }

            StringBuilder msg = new StringBuilder();
            msg.append("request start ").append(method).append(" UA=").append(agent).
                    append(" url=").append(url).append(" remote=").append(remoteAddress);
            LOG.info(msg);
            start = System.nanoTime();

            chain.doFilter(request, response);
        }
        catch (Throwable e) {  // be sure to get all errors
            throwable = e;
            throw new ServletException(e);
        }
        finally {
            long time = System.nanoTime() - start;
            // try to get OID again at request end
            int statusCode = res.getStatus();
            StringBuilder msg = new StringBuilder();
            msg.append("request done ").
                    append(" code=").append(statusCode).
                    append(" time=").append(formatNanos(time)).append("ms");
            LOG.info(msg);
        }
    }

    @Override
    public void destroy() {

    }

    private String formatNanos(long nanos) {
        // show millis with 3 digits (i.e. us)
        return new DecimalFormat("#.###", DecimalFormatSymbols.getInstance(Locale.ENGLISH)).format(nanos / 1000000.0);
    }
}
