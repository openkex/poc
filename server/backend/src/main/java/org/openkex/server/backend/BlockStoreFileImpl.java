/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.server.backend;

import org.openkex.tools.FileTool;
import org.openkex.tools.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class BlockStoreFileImpl implements BlockStoreApi {

    // path separator. working also on windows (java apis fix this)
    private static final String SEP = "/";
    private static final Logger LOG = LoggerFactory.getLogger(BlockStoreFileImpl.class);
    private String basePath;

    public BlockStoreFileImpl(String basePath) throws Exception {
        LOG.info("BlockStoreFileImpl path=" + basePath);
        this.basePath = basePath;
        // check if exist, try to create if missing
        File base = new File(basePath);
        if (!base.exists()) {
            // great cascaded statics..
            LOG.info("creating dir=" + basePath);
            // NIO sucks
            // java.nio.file.InvalidPathException: Illegal char <:> at index 2: /F:/dev/git/kex_proto/server/backend/target/FileBlockStoreTest
            // Files.createDirectories(Paths.get(basePath));
            Validate.isTrue(base.mkdirs());
        }
        else if (!base.canWrite()) {
            throw new RuntimeException("cannot write to: " + basePath);
        }
    }

    public long getBlockCount() throws Exception {
        return scanFilesRecursive(new File(basePath), false);
    }

    public long getStoreSize() throws Exception {
        return scanFilesRecursive(new File(basePath), true);
    }

    @Override
    public boolean writeBlock(long roundNr, byte[] blockData) throws Exception {
        Validate.notNull(blockData);
//        LOG.debug("writeBlock roundNr=" + roundNr + " size=" + blockData.length);
        String file = getFilePath(roundNr);
        createDirectory(file);
        boolean exists = new File(file).exists();
        FileTool.write(blockData, file, false);
        return exists;
    }

    @Override
    public void appendBlock(long roundNr, byte[] blockData) throws Exception {
        Validate.notNull(blockData);

//        LOG.debug("writeBlock roundNr=" + roundNr + " size=" + blockData.length);
        String file = getFilePath(roundNr);
        File data = new File(file);
        Validate.isTrue(data.exists(), "cannot append to missing block: " + roundNr);
        FileTool.write(blockData, file, true);
    }

    @Override
    public byte[] readBlock(long roundNr) throws Exception {
        String file = getFilePath(roundNr);
        if (!new File(file).exists()) {
            return null;
        }
        return FileTool.read(file);
    }

    private String getFilePath(long roundNr) {
        // assume 12 digits are sufficient (60s round starting 1.1.1970 -> .. year 3871
        long maxRound = 1000000000000L;
        Validate.isTrue(roundNr < maxRound);
        String roundStr = String.format("%012d", roundNr);
        // split 3 x 4digits (10000 files or directories per level)

        // return basePath + File.separator + roundStr;
        return basePath + SEP + roundStr.substring(0, 4) + SEP + roundStr.substring(4, 8) + SEP + roundStr;
    }

    // will create directory if required
    private void createDirectory(String path) throws Exception {
        String directory = path.substring(0, path.lastIndexOf(SEP));
        File dirFile = new File(directory);
        if (!dirFile.exists()) {
            Validate.isTrue(dirFile.mkdirs());
        }
    }

    /**
     * scan all files recursively, calculate count or size
     *
     * @param dir base directory
     * @param size if true calculate size, if false calculate count
     * @return size or count
     * @throws Exception inf case of IO errors
     */
    private long scanFilesRecursive(File dir, boolean size) throws Exception {
        Validate.isTrue(dir.exists(), "not found: " + dir);
        Validate.isTrue(dir.isDirectory(), "not a directory: " + dir);
        File[] fileArray = dir.listFiles();
        Validate.notNull(fileArray);
        long number = 0;

        for (File f : fileArray) {
            if (f.isDirectory()) {
                // recursive...
                number += scanFilesRecursive(f, size);
            }
            else {
                if (size) {
                    number += f.length();
                }
                else {
                    number++;
                }
            }
        }
        return number;
    }

}
