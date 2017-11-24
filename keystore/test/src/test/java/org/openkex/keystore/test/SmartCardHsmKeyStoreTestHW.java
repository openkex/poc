/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.keystore.test;

import org.junit.Assert;
import org.openkex.keystore.api.KeyStore;
import org.openkex.keystore.smartcardhsm.SmartCardHsmKeyStore;

public class SmartCardHsmKeyStoreTestHW extends KeyStoreTest {
    protected KeyStore getKeystore() throws Exception {
        return new SmartCardHsmKeyStore();
    }

    @Override
    protected void unlock(KeyStore keystore) throws Exception {
        char[] testPin = "123456".toCharArray();
        Assert.assertTrue(keystore.unlock(testPin));
    }
}
