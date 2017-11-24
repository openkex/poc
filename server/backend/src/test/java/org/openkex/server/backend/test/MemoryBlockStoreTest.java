/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.server.backend.test;

import org.openkex.server.backend.BlockStoreApi;
import org.openkex.server.backend.BlockStoreMemoryImpl;

public class MemoryBlockStoreTest extends AbstractBlockStoreTest {

    @Override
    protected BlockStoreApi getBlockStore() {
        return new BlockStoreMemoryImpl();
    }

    @Override
    protected int getSizeLimit() {
        return 1024 * 1024 * 32; // 32MB for memory based store
    }
}
