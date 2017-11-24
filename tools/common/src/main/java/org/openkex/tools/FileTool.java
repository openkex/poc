/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileTool {

    private static final Logger LOG = LoggerFactory.getLogger(FileTool.class);
    private static final int BUFFER_SIZE = 8192;

    private FileTool() {
    }

    /**
     * save byte array to file
     *
     * @param array source bytes
     * @param fileName name of destination file
     * @param append if true append to file
     */
    public static void write(byte[] array, String fileName, boolean append) {

        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(fileName, append);
            fileOutputStream.write(array);
        }
        catch (IOException ex) {
            throw new RuntimeException(ex); // wrap and throw
        }
        finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            }
            catch (IOException ex) {
                LOG.error("failed to finally close FileOutputStream.", ex);
            }
        }
    }

    /**
     * read byte array from file.
     * <p>
     * Note: this method might be problematic for large files due to memory consumption
     *
     * @param file source file
     * @return byte array with file content
     */
    public static byte[] read(String file) {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytes = 0;
            while ((bytes = fileInputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytes);
            }
            return byteArrayOutputStream.toByteArray();
        }
        catch (IOException ex) {
            throw new RuntimeException(ex); // rethrow
        }
        finally {
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
            }
            catch (IOException ex) {
                LOG.error("failed to finally close FileInputStream.", ex);
            }
        }
    }
}
