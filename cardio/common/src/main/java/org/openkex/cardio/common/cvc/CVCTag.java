/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.cardio.common.cvc;

/**
 * see BSI TR-03110 (Part 3)
 * <p>
 * Tag values as defined in Appendix D
 */
public enum CVCTag {

    /** card verifiable certificate (potential root) */
    CV_CERTIFICATE(0x7F21, Type.Sequence),

    CERTIFICATE_BODY(0x7F4E, Type.Sequence),

    CERTIFICATE_PROFILE_IDENTIFIER(0x5F29, Type.Integer),

    /** aka "issuer" (acronym CAR) */
    CERTIFICATE_AUTHORITY_REFERENCE(0x42, Type.CharacterString),

    /** key of card holder */
    PUBLIC_KEY(0x7F49, Type.Sequence),

    /** aka "subject" (acronym CHR) */
    CERTIFICATE_HOLDER_REFERENCE(0x5F20, Type.CharacterString),

    CERTIFICATE_HOLDER_AUTHORIZATION_TEMPLATE(0x7F4C, Type.Sequence),
    DISCRETIONARY_DATA(0x53, Type.OctetString),

    /** aka "valid from" */
    CERTIFICATE_EFFECTIVE_DATE(0x5F25, Type.Date),
    /** aka "valid to" */
    CERTIFICATE_EXPIRATION_DATE(0x5F24, Type.Date),

    CERTIFICATE_EXTENSIONS(0x65, Type.Sequence),
    DISCRETIONARY_DATA_TEMPLATE(0x73, Type.Sequence),

    SIGNATURE(0x5F37, Type.OctetString),

    AUTHENTICATION(0x67, Type.Sequence),

    // public_key specific data
    // using OctetString for "Big Integer" and ECC curve point (deal with it later)
    OID(0x06, Type.OID),

    // ECC only for now ...

//    // RSA
//    RSA_COMPOSITE_MODULUS(0x81, Type.OctetString),
//    RSA_PUBLIC_EXPONENT(0x82, Type.OctetString),
//
//    // Diffie Hellmann
//    DH_PRIME_MODULUS(0x81, Type.OctetString),
//    DH_ORDER_SUBGROUP_GENERATOR(0x82, Type.OctetString),
//    DH_PUBLIC_VALUE(0x83, Type.OctetString),

    // ECC
    ECC_PRIME_MODULUS(0x81, Type.OctetString),
    ECC_FIRST_COEFFICIENT(0x82, Type.OctetString),
    ECC_SECOND_COEFFICIENT(0x83, Type.OctetString),
    ECC_BASE_POINT(0x84, Type.OctetString),
    ECC_ORDER_BASE_POINT(0x85, Type.OctetString),
    ECC_PUBLIC_POINT(0x86, Type.OctetString),
    ECC_COFACTOR(0x87, Type.OctetString);

    private int value;
    private Type type;

    CVCTag(int value, Type type) {
        this.value = value;
        this.type = type;
    }

    public int getValue() {
        return value;
    }

    public Type getType() {
        return type;
    }

    public enum Type {
        OID,
        Integer,
        Date,
        CharacterString,
        OctetString,
        Sequence
    }

    /**
     * @param value value of tag
     * @return tag by value, null if not found
     */
    public static CVCTag getByValue(int value) {
        for (CVCTag tag : CVCTag.values()) {
            if (tag.getValue() == value) {
                return tag;
            }
        }
        return null;
    }

//    private enum Condition {
//        /** "m" */
//        Mandatory,
//        /** "o" */
//        Optional,
//        /** "c" */
//        Conditional,
//        /** "r" */
//        R????,
//    }

}
