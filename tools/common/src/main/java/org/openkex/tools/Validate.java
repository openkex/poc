/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.tools;

public class Validate {

    /**
     * static methods only, no instances
     */
    private Validate() {
    }

    public static void isTrue(boolean condition, Object... messages) {
        if (!condition) {
            throw new RuntimeException("Validate.isTrue failed: " + getMessage(messages));
        }
    }

    public static void isTrue(boolean condition) {
        if (!condition) {
            throw new RuntimeException("Validate.isTrue failed.");
        }
    }

    public static void notNull(Object object, Object... message) {
        if (object == null) {
            throw new RuntimeException("Validate.notNull failed: " + getMessage(message));
        }
    }

    public static void notNull(Object object) {
        if (object == null) {
            throw new RuntimeException("Validate.notNull failed.");
        }
    }

    private static String getMessage(Object... messages) {
        StringBuilder sb = new StringBuilder();
        for (Object message : messages) {
            sb.append(message);
        }
        return sb.toString();
    }

}
