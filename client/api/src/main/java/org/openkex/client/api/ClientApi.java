/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.client.api;

import org.openkex.dto.SignedStatements;

import java.util.List;

/**
 * API for the kex client
 */
public interface ClientApi {

    /**
     * session start
     *
     * @param kexId the kex id
     * @return initial server challenge
     * @throws Exception if communication failed
     */
    byte[] getSessionChallenge(long kexId) throws Exception;

    /**
     * confirm session
     *
     * @param signature signature bytes
     * @param kexId the kex id
     * @return authentication token
     * @throws Exception if session validation failed
     */
    byte[] startKexSession(long kexId, byte[] signature) throws Exception;

    /**
     * submit new statement to server
     *
     * @param statement the new statement
     * @param authToken token representing client session
     * @return round number when consensus shall happen
     * @throws Exception if immediate check of statement failed
     */
    long submitStatement(SignedStatements statement, byte[] authToken) throws Exception;

    /**
     * get list of statements for kex user
     *
     * @param kexId kexId of user
     * @return all statements for user, null if kexId is unknown (i.e. no statement exist)
     * @throws Exception in case of problems
     */
    List<SignedStatements> getStatements(long kexId) throws Exception;

}
