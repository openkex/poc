/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.keystore.smartcardhsm;

import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * tool to maintain simple key index as flat file
 */
public class KeyIndexTool {

    private static final String INDEX_SEPARATOR = ":";
    private static final Charset ENCODING = Charset.forName("ASCII");

    private KeyIndexTool() {
    }

    /**
     * split byte array representing list
     *
     * @param content the byte array
     * @return the array of Strings
     */
    public static String[] split(byte[] content) {
        if (content == null) {
            return new String[0];
        }
        // "too easy": this is broken for trailing separators like
        //return new String(content, ENCODING).split(INDEX_SEPARATOR);

        ArrayList<String> result = new ArrayList<>();
        String rest = new String(content, ENCODING);
        while (rest.length() > 0) {
            int pos = rest.indexOf(INDEX_SEPARATOR);
            if (pos == -1) {
                result.add(rest);
                break;
            }
            // split at pos
            result.add(rest.substring(0, pos));
            rest = rest.substring(pos + 1, rest.length());
        }
        return result.toArray(new String[result.size()]);
    }

    /**
     * crate byte array from array of Strings
     *
     * @param strings array
     * @return concatenated byte array
     */
    public static byte[] join(String[] strings) {
        StringBuilder sb = new StringBuilder();
        if (strings.length == 0) {
            return new byte[0];
        }
        for (int i = 0; i < strings.length; i++) {
            checkString(strings[i]);
            sb.append(strings[i]);
            sb.append(INDEX_SEPARATOR);
        }
        return sb.toString().getBytes(ENCODING);
    }

    /**
     * check if string is equal to one string in array
     *
     * @param strings input array
     * @param value string to compare
     * @return index position if matches, -1 if not found.
     */
    public static int getIndex(String[] strings, String value) {
        for (int i = 0; i < strings.length; i++) {
            if (strings[i].equals(value)) {
                return i;
            }
        }
        return -1;
    }

    private static void checkString(String value) {
        if (value.contains(INDEX_SEPARATOR)) {
            throw new RuntimeException("string contains separator: '" + value + "'");
        }
    }

    /**
     * add new value to array of Strings.
     * <p>
     * use empty index (i.e. value = "") if possible.
     *
     * @param strings original strings
     * @param value value to add
     * @return new array of Strings
     */
    public static String[] addValue(String[] strings, String value) {
        if (getIndex(strings, value) != -1) {
            throw new RuntimeException("value exists. " + value);
        }
        // search for first empty index.
        for (int i = 0; i < strings.length; i++) {
            if (strings[i].length() == 0) {
                strings[i] = value;
                return strings;
            }
        }
        // no empty space found, append at end.
        String[] newStrings = new String[strings.length + 1];
        System.arraycopy(strings, 0, newStrings, 0, strings.length);
        newStrings[strings.length] = value;
        return newStrings;
    }
}
