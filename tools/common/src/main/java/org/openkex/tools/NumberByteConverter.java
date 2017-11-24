/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.tools;

/**
 * tool to convert numbers to byte arrays (and hex-print them)
 * <p>
 * Note: All byte arrays are big endian.
 * <p>
 * Note: org.bouncycastle.util.Pack does the same but has no unit tests
 */
public class NumberByteConverter {

    private NumberByteConverter() {
    }

    /**
     * @param value integer to convert
     * @return 4 bytes from integer
     */
    public static byte[] intToBytes(int value) {
        return numberToBytes(value, 4);
    }

    /**
     * @param value long to convert
     * @return 8 bytes from long
     */
    public static byte[] longToBytes(long value) {
        return numberToBytes(value, 8);
    }

    /**
     * @param value long to convert
     * @return 6 bytes from limited long, validates that 2 upper bytes are zero
     */
    public static byte[] longToSixBytes(long value) {
        byte[] bytes = numberToBytes(value, 8);
        Validate.isTrue(bytes[0] == 0, "byte 0 is not zero. value=" + value);
        Validate.isTrue(bytes[1] == 0, "byte 1 is not zero. value=" + value);
        byte[] result = new byte[6];
        System.arraycopy(bytes, 2, result, 0, 6);
        return result;
    }

    /**
     * @param array input byte array
     * @return int from 4 bytes
     */
    public static int bytesToInt(byte[] array) {
        return (int) bytesToNumber(array, 4);
    }

    /**
     * @param array input byte array
     * @return long from 8 bytes
     */
    public static long bytesToLong(byte[] array) {
        return bytesToNumber(array, 8);
    }

    /**
     * @param array input byte array
     * @return long from 6 bytes
     */
    public static long sixBytesToLong(byte[] array) {
        return bytesToNumber(array, 6);
    }

    /**
     * @param value input integer
     * @return hex string of 4 bytes from integer
     */
    public static String intToByteString(int value) {
        // NOTE: this function exists: Integer.toHexString(value), but is does not create leading zeros.
        return Hex.toString(intToBytes(value));
    }

    /**
     * @param value input long
     * @return hex string of 8 bytes from long
     */
    public static String longToByteString(long value) {
        return Hex.toString(longToBytes(value));
    }

    /**
     * @param value input long
     * @return hex string of 6 bytes from limited long
     */
    public static String longToSixByteString(long value) {
        return Hex.toString(longToSixBytes(value));
    }

    static long bytesToNumber(byte[] bytes, int size) {
        Validate.notNull(bytes, "cannot convert null array");
        Validate.isTrue(bytes.length == size, "expected " + size + " bytes but got " + bytes.length);
        long result = 0;
        long multiplier = 1;

        for (int i = bytes.length - 1; i > 0; i--) {
            result += (bytes[i] & 0xFF) * multiplier;
            multiplier *= 256;
        }
        if (size != 6) {
            result += bytes[0] * multiplier; // don't mask sign for last byte
        }
        else {
            // no sign for "six bytes long"
            result += (bytes[0] & 0xFF) * multiplier;
        }

        return result;
    }

    private static byte[] numberToBytes(long value, int bytes) {
        byte[] result = new byte[bytes];
        for (int i = 0; i < bytes; i++) {
            int shift = (bytes - i - 1) * 8;
            result[i] = (byte) (value >> shift & 0xFF);
        }
        return result;
    }

}
