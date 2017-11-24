/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.dto;

/**
 * this class references specifications for cryptographic algorithms
 */
public enum Specification {

    SEC("Certicom Research, Standards for Efficient Cryptography (SEC)",
            "http://www.secg.org/sec1-v2.pdf http://www.secg.org/sec2-v2.pdf"),

    EDDSA("RFC 8032, Edwards-Curve Digital Signature Algorithm (EdDSA)",
            "https://tools.ietf.org/html/rfc8032"),
    /** no specification, testing only */
    DUMMY("DUMMY", "DUMMY");

    private String title;
    private String url;

    Specification(String title, String url) {
        this.title = title;
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }
}
