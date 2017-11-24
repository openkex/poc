/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.server.backend.test;

import org.openkex.server.backend.BlockStoreApi;
import org.openkex.server.backend.BlockStoreFileImpl;
import org.openkex.tools.DirectoryTool;
import org.openkex.tools.Validate;

import java.io.File;

public class FileBlockStoreTest extends AbstractBlockStoreTest {

    @Override
    protected BlockStoreApi getBlockStore() throws Exception {
        String path = DirectoryTool.getTargetDirectory(this.getClass()) + "FileBlockStoreTest";
        delete(new File(path));
        return new BlockStoreFileImpl(path);
    }

    @Override
    protected int getSizeLimit() {
        return 1024 * 1024 * 32; // 32MB for file based store
    }

    private void delete(File directory) {
        if (!directory.exists()) {
            return;
        }
        Validate.isTrue(directory.isDirectory());

        File[] files = directory.listFiles();
        Validate.notNull(files);
        for (File file : files) {
            if (file.isDirectory()) {
                delete(file);
            }
            else {
                Validate.isTrue(file.delete(), "failed to delete file: " + file.getPath());
            }
        }
        Validate.isTrue(directory.delete(), "failed to delete directory: " + directory.getPath());
    }
}
