/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.keystore.test;

import org.junit.Assert;
import org.openkex.keystore.api.KeyStore;
import org.openkex.keystore.jks.JksKeyStore;
import org.openkex.tools.DirectoryTool;

import java.io.File;

public class Pkcs12KeyStoreTest extends KeyStoreTest {

    protected void unlock(KeyStore keystore) throws Exception {
        char[] testPin = "see_creet".toCharArray();
        Assert.assertTrue(keystore.unlock(testPin));
    }

    protected KeyStore getKeystore() throws Exception {
        JksKeyStore.Type type = JksKeyStore.Type.PKCS12;
        String fileName = "storeTest." + type.toString().toLowerCase();
        String file = DirectoryTool.getTargetDirectory(Pkcs12KeyStoreTest.class) + fileName;
        File f = new File(file);
        if (f.exists()) {
            Assert.assertTrue(f.delete());
        }
        return new JksKeyStore(type, file);
    }
}
