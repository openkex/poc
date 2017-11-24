/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.dto;

import org.openkex.tools.Hex;

import java.util.Arrays;

public class StatementAttributes {

    public enum Type {
        EMAIL,
        MSISDN,
        NICK_NAME
    }

    private Type type;
    // either value or maskedValue is null, not elegant..
    private String value;
    private byte[] maskedValue;

    // -- generated code below --

    public StatementAttributes() {
    }

    public StatementAttributes(Type type, String value, byte[] maskedValue) {
        this.type = type;
        this.value = value;
        this.maskedValue = maskedValue;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public byte[] getMaskedValue() {
        return maskedValue;
    }

    public void setMaskedValue(byte[] maskedValue) {
        this.maskedValue = maskedValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        StatementAttributes that = (StatementAttributes) o;

        if (type != that.type) {
            return false;
        }
        if (value != null ? !value.equals(that.value) : that.value != null) {
            return false;
        }
        return Arrays.equals(maskedValue, that.maskedValue);
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(maskedValue);
        return result;
    }

    @Override
    public String toString() {
        return "StatementAttributes{" +
                "type=" + type +
                ", value='" + value + '\'' +
                ", maskedValue=" + Hex.toString(maskedValue) +
                '}';
    }
}
