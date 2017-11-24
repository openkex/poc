/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.tools;

import java.net.URLDecoder;

/**
 * tool for directories
 */
public class DirectoryTool {

    private DirectoryTool() {
    }

    /**
     * ty to "estimate" maven target directory based on input class.
     * <p>
     * this class may be placed in "target/classes" directory or in a jar file placed in "target"
     * <p>
     * Note that on windoze the result like "/C:/dev/repo/module/target/" is perfectly OK for java.util.File but fails
     * with java.nio.file classes
     *
     * @param clazz considered class
     * @return full path of target directory (with trailing slash)
     * @throws Exception in case of problems
     */
    public static String getTargetDirectory(Class clazz) throws Exception {
        // use path of jar or class to find target directory
        String jarPath = clazz.getProtectionDomain().getCodeSource().getLocation().getPath();
        String decodedJarPath = URLDecoder.decode(jarPath, "UTF-8");
        return decodedJarPath.substring(0, decodedJarPath.lastIndexOf("target/") + 7);
    }

}
