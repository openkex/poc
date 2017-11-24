/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.keystore.test;

import org.openkex.keystore.api.KeyStore;
import org.openkex.keystore.memory.JavaMemoryKeyStore;

public class JavaMemoryKeyStoreTest extends KeyStoreTest {

    @Override
    protected KeyStore getKeystore() {
        return new JavaMemoryKeyStore();
    }
}
