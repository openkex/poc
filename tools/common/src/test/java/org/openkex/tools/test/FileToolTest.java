/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.tools.test;

import org.junit.Assert;
import org.junit.Test;
import org.openkex.tools.ByteArrayTool;
import org.openkex.tools.DirectoryTool;
import org.openkex.tools.FileTool;
import org.openkex.tools.RandomTool;

public class FileToolTest {

    private static final long SEED = 4711;

    @Test
    public void testBasic() throws Exception {

        String fileName = DirectoryTool.getTargetDirectory(this.getClass()) + "FileToolTest";

        RandomTool randomTool = new RandomTool(SEED);
        byte[] data = randomTool.getBytes(43661);

        FileTool.write(data, fileName, false);

        byte[] dataRead = FileTool.read(fileName);

        Assert.assertArrayEquals(data, dataRead);

        // note assertArrayEquals is slow for large arrays. faster variant for large arrays.
        // Assert.assertTrue(Arrays.equals(data, dataRead));

    }

    @Test
    public void testAppend() throws Exception {

        String fileName = DirectoryTool.getTargetDirectory(this.getClass()) + "FileToolTest2";
        RandomTool randomTool = new RandomTool(SEED);
        byte[] data1 = randomTool.getBytes(43665);
        byte[] data2 = randomTool.getBytes(4761);

        FileTool.write(data1, fileName, false);  // create
        FileTool.write(data2, fileName, true);  // append

        byte[] dataRead = FileTool.read(fileName);
        Assert.assertArrayEquals(ByteArrayTool.add(data1, data2), dataRead);

        FileTool.write(data2, fileName, false); // overwrite
        dataRead = FileTool.read(fileName);
        Assert.assertArrayEquals(data2, dataRead);

    }
}
