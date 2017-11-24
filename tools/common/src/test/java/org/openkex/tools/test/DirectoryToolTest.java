/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.tools.test;

import org.junit.Assert;
import org.junit.Test;
import org.openkex.tools.DirectoryTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectoryToolTest {

    private static final Logger LOG = LoggerFactory.getLogger(DirectoryToolTest.class);

    @Test
    public void testTarget() throws Exception {
        String target = DirectoryTool.getTargetDirectory(this.getClass());
        LOG.info("found current target path: " + target);
        Assert.assertTrue(target.endsWith("/target/"));
    }
}
