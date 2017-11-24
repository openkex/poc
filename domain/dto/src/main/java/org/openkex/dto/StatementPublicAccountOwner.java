/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.dto;

/**
 * statement to claim ownership of a public account.
 * see #846
 */
public class StatementPublicAccountOwner {

    public enum Type {
        TWITTER,
        FACEBOOK,
        GITHUB
    }

    private Type type;

    // type specific account identifier
    private String accountId;

    // -- generated code below --

    public StatementPublicAccountOwner() {
    }

    public StatementPublicAccountOwner(Type type, String accountId) {
        this.type = type;
        this.accountId = accountId;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        StatementPublicAccountOwner that = (StatementPublicAccountOwner) o;

        if (type != that.type) {
            return false;
        }
        return accountId != null ? accountId.equals(that.accountId) : that.accountId == null;
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (accountId != null ? accountId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "StatementPublicAccountOwner{" +
                "type=" + type +
                ", accountId='" + accountId + '\'' +
                '}';
    }
}
