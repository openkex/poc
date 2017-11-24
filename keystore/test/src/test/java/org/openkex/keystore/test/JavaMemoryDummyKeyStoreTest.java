/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.keystore.test;

import org.openkex.keystore.api.KeyStore;
import org.openkex.keystore.memory.JavaMemoryDummyKeyStore;

public class JavaMemoryDummyKeyStoreTest extends KeyStoreTest {

    @Override
    protected KeyStore getKeystore() {
        return new JavaMemoryDummyKeyStore();
    }

    @Override
    protected boolean isDummy() {
        return true;
    }
}
