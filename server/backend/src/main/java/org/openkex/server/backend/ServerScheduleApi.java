/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.server.backend;

/**
 * API to control server phases
 */
public interface ServerScheduleApi {

    enum Phase {
        /** no phase */
        INVALID(-1),
        /** initial phase only collects data */
        INITIAL(0),
        /** start new round, publish statements collected in last round */
        PUBLISH_STATEMENTS(1),
        /** fetch collected statements from other servers,
           validate all statement and calculate block hash */
        VALIDATE(2),
        /** publish proposed new block hash and fetch other block hashes */
        PUBLISH_HASH(3),
        /** fetch hashes, check for consensus */
        CHECK_CONSENSUS(4),
        /** publish signature in case of consensus */
        PUBLISH_SIGNATURE(5),
        /** fetch signatures */
        FETCH_SIGNATURE(6);

        private int sequence;

        Phase(int sequence) {
            this.sequence = sequence;
        }

        public int getSequence() {
            return sequence;
        }
    }

    void processRound(long roundNr, Phase phase) throws Exception;
}
