/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.server.backend;

import org.openkex.tools.ByteArrayTool;
import org.openkex.tools.Validate;

import java.util.HashMap;

public class BlockStoreMemoryImpl implements BlockStoreApi {

    private HashMap<Long, byte[]> blocks;

    public BlockStoreMemoryImpl() {
        blocks = new HashMap<>();
    }

    public long getBlockCount() {
        return blocks.size();
    }

    public long getStoreSize() {
        long size = 0;
        for (byte[] block : blocks.values()) {
            size += block.length;
        }
        return size;
    }

    @Override
    public boolean writeBlock(long roundNr, byte[] blockData) throws Exception {
        Validate.notNull(blockData);
        return blocks.put(roundNr, blockData) != null;
    }

    @Override
    public void appendBlock(long roundNr, byte[] blockData) throws Exception {
        Validate.notNull(blockData);
        byte[] oldData = blocks.get(roundNr);
        Validate.notNull(oldData, "no block found with number=" + roundNr);
        blocks.put(roundNr, ByteArrayTool.add(oldData, blockData));
    }

    @Override
    public byte[] readBlock(long roundNr) throws Exception {
        return blocks.get(roundNr);
    }
}
