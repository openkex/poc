/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.cardio.atcitizen;

import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;

import java.security.cert.X509Certificate;

public class CertificateData {

    private String commonName;
    private String givenName;
    private String surname;
    private String title;
    private String serial;
    private String countryCode;

    public CertificateData(X509Certificate cert) throws Exception {
        // http://stackoverflow.com/questions/2914521/how-to-extract-cn-from-x509certificate-in-java
        // https://tools.ietf.org/html/rfc2253#section-3 obsoleted by 4510, 4514
        // https://tools.ietf.org/html/rfc3039 (in BCStyle.java) obsoleted by 3739
        // https://tools.ietf.org/html/rfc2256 (in BCStyle.java) obsoleted by 4517, 4519, 4523, 4512, 4510
        // https://tools.ietf.org/html/rfc3166 (in BCStyle.java) wow, not obsoleted.
        // Note: JcaX509CertificateHolder requires maven artifact "bcpkix-jdk15on"
        // this code is UGLY!
        X500Name x500name = new JcaX509CertificateHolder(cert).getSubject();
        RDN rdn = x500name.getRDNs(BCStyle.CN)[0];
        commonName = IETFUtils.valueToString(rdn.getFirst().getValue());
        rdn = x500name.getRDNs(BCStyle.GIVENNAME)[0];
        givenName = IETFUtils.valueToString(rdn.getFirst().getValue());
        rdn = x500name.getRDNs(BCStyle.SURNAME)[0];
        surname = IETFUtils.valueToString(rdn.getFirst().getValue());
        rdn = x500name.getRDNs(BCStyle.T)[0];
        title = IETFUtils.valueToString(rdn.getFirst().getValue());
        rdn = x500name.getRDNs(BCStyle.SERIALNUMBER)[0];
        serial = IETFUtils.valueToString(rdn.getFirst().getValue());
        // note about "country". semantics are unclear.
        // there would be alternatives like: RFC 3739 CountryOfCitizenship CountryOfResidence
        rdn = x500name.getRDNs(BCStyle.C)[0];
        countryCode = IETFUtils.valueToString(rdn.getFirst().getValue());
    }

    public String getCommonName() {
        return commonName;
    }

    public String getGivenName() {
        return givenName;
    }

    public String getSurname() {
        return surname;
    }

    public String getTitle() {
        return title;
    }

    public String getSerial() {
        return serial;
    }

    public String getCountryCode() {
        return countryCode;
    }

    @Override
    public String toString() {
        return "CertificateData{" +
                "commonName='" + commonName + '\'' +
                ", givenName='" + givenName + '\'' +
                ", surname='" + surname + '\'' +
                ", title='" + title + '\'' +
                ", serial='" + serial + '\'' +
                ", countryCode='" + countryCode + '\'' +
                '}';
    }
}
