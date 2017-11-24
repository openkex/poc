/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.server.api;

import org.openkex.dto.SignedStatements;

import java.util.List;

/**
 * API for the kex server to server communication
 * <p>
 * Note: Server communication is authenticated and confidential.
 * The actual implementation is not scope of this interface.
 */
public interface ServerApi {

    /**
     * new statements collected for specific round.
     *
     * @param roundNr number of round
     * @return statements collected for round
     * @throws Exception if round is no longer active
     */
    List<SignedStatements> newStatements(long roundNr) throws Exception;

    /**
     * get hash of proposed aggregated list of statements
     *
     * @param roundNr number of round
     * @return hash bytes of proposed statement list
     * @throws Exception if round is no longer active
     */
    byte[] getHash(long roundNr) throws Exception;

    /**
     * get signature of final list of statements
     *
     * @param roundNr number of round
     * @return hash bytes of proposed statement list
     * @throws Exception if round is no longer active
     */
    byte[] getSignature(long roundNr) throws Exception;

    /**
     * get confirmed statements. this if for "catching up" if other node was down.
     *
     * @param roundNr number of round
     * @return all statements (TODO: add signatures)
     * @throws Exception if round was not (yet) confirmed
     */
    List<SignedStatements> getConfirmedStatements(long roundNr) throws Exception;

}
