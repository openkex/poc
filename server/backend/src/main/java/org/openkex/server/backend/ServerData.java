/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.server.backend;

import org.openkex.dto.SignatureAlgorithm;
import org.openkex.tools.Hex;

import java.util.Arrays;

/**
 * information about a kex server
 */
public class ServerData {

    private String serverId;
    private String url;
    private String operator; // will need  contact information (name, address, etc) later
    private SignatureAlgorithm algorithm;
    private byte[] key;

    public ServerData(String serverId, String url, String operator, SignatureAlgorithm algorithm, byte[] key) {
        this.serverId = serverId;
        this.url = url;
        this.operator = operator;
        this.algorithm = algorithm;
        this.key = key;
    }

    public String getServerId() {
        return serverId;
    }

    public String getUrl() {
        return url;
    }

    public String getOperator() {
        return operator;
    }

    public SignatureAlgorithm getAlgorithm() {
        return algorithm;
    }

    public byte[] getKey() {
        return key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ServerData that = (ServerData) o;

        if (serverId != null ? !serverId.equals(that.serverId) : that.serverId != null) {
            return false;
        }
        if (url != null ? !url.equals(that.url) : that.url != null) {
            return false;
        }
        if (operator != null ? !operator.equals(that.operator) : that.operator != null) {
            return false;
        }
        if (algorithm != that.algorithm) {
            return false;
        }
        return Arrays.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        int result = serverId != null ? serverId.hashCode() : 0;
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (operator != null ? operator.hashCode() : 0);
        result = 31 * result + (algorithm != null ? algorithm.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(key);
        return result;
    }

    @Override
    public String toString() {
        return "ServerData{" +
                "serverId='" + serverId + '\'' +
                ", url='" + url + '\'' +
                ", operator='" + operator + '\'' +
                ", algorithm=" + algorithm +
                ", key=" + Hex.toString(key) +
                '}';
    }
}
