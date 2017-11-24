/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.server.backend;

/**
 * stores binary blocks.
 */
public interface BlockStoreApi {

    /**
     * write block (create or update)
     *
     * @param roundNr considered round (Note: currently round is limited to 1e12)
     * @param blockData data to write
     * @return true in case of update (previous data was existing)
     * @throws Exception in case of storage problem
     */
    boolean writeBlock(long roundNr, byte[] blockData) throws Exception;

    /**
     * append to existing block
     *
     * @param roundNr considered round
     * @param blockData data to append
     * @throws Exception if block does not exist, in case of storage problem
     */
    void appendBlock(long roundNr, byte[] blockData) throws Exception;

    /**
     * read block
     *
     * @param roundNr considered round
     * @return block data, null if no data is present
     * @throws Exception in case of storage problem
     */
    byte[] readBlock(long roundNr) throws Exception;

    /**
     * get block count
     *
     * @return number of stored blocks
     * @throws Exception in case of storage problem
     */
    long getBlockCount() throws Exception;

    /**
     * get total block size
     *
     * @return sum of the sized of all stored blocks
     * @throws Exception in case of storage problem
     */
    long getStoreSize() throws Exception;

}
