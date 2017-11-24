/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.dto;

import org.openkex.tools.NumberByteConverter;

/**
 * input for client authentication token
 */
public class ClientTokenInfo {

    private long kexId;
    /**
     * time_t of session expiry
     */
    private long expiryTime;

    public ClientTokenInfo() {
    }

    public ClientTokenInfo(long kexId, long expiryTime) {
        this.kexId = kexId;
        this.expiryTime = expiryTime;
    }

    public long getKexId() {
        return kexId;
    }

    public long getExpiryTime() {
        return expiryTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ClientTokenInfo that = (ClientTokenInfo) o;

        if (kexId != that.kexId) {
            return false;
        }
        return expiryTime == that.expiryTime;
    }

    @Override
    public int hashCode() {
        int result = (int) (kexId ^ (kexId >>> 32));
        result = 31 * result + (int) (expiryTime ^ (expiryTime >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "ClientTokenInfo{" +
                "kexId=" + NumberByteConverter.longToSixByteString(kexId) +
                ", expiryTime=" + expiryTime +
                '}';
    }
}
