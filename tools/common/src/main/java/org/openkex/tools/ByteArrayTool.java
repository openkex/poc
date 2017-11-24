/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.tools;

public class ByteArrayTool {

    private static final int BITS_PER_BYTE = 8;

    private ByteArrayTool() {
    }

    /**
     * compare two byte arrays
     *
     * @param array1 first array
     * @param array2 second array
     * @return values -1, 0, 1 according to java.util.Comparator
     */
    public static int compareByteArray(byte[] array1, byte[] array2) {
        if (array1.length == array2.length) {
            // compare byte by byte. assume MSB first.
            for (int i = 0; i < array1.length; i++) {
                if (array1[i] == array2[i]) {
                    continue;
                }
                if ((array1[i] & 0xFF) > (array2[i] & 0xFF)) {
                    return 1;
                }
                else {
                    return -1;
                }
            }
            // all bytes equal.
            return 0;
        }
        else { // longer byte array is "bigger".
            return Integer.compare(array1.length, array2.length);
        }
    }

    /**
     * calculate number of different bits between two equal sized byte arrays
     *
     * @param a first array
     * @param b second array
     * @return number of different bits
     */
    public static int bitsDifferent(byte[] a, byte[] b) {
        Validate.notNull(a);
        Validate.notNull(b);
        Validate.isTrue(a.length == b.length, "arrays size is different.");
        int bits = 0;
        for (int i = 0; i < a.length; i++) {
            // XOR, then mask away sign extension of negative bytes
            int diff = a[i] ^ b[i];
            bits += Integer.bitCount(diff & 0xFF);
        }
        return bits;
    }

    /**
     * toggle one bit in a byte array
     *
     * @param a the array to change
     * @param pos the bit position (position 0 is bit 0 of a[0])
     */
    public static void toggleBit(byte[] a, int pos) {
        Validate.notNull(a);
        Validate.isTrue(pos >= 0, "negative position is invalid.");
        Validate.isTrue(pos < a.length * BITS_PER_BYTE, "position is out of range.");
        int bytePos = pos / BITS_PER_BYTE;
        int bitPos = pos % BITS_PER_BYTE;
        int bitValue = 1 << bitPos;  // shift left
        a[bytePos] ^= bitValue;  // toggle with XOR
    }

    /**
     * concatenate two byte arrays
     *
     * @param a first array
     * @param b second array
     * @return first "plus" second array
     */
    public static byte[] add(byte[] a, byte[] b) {
        Validate.notNull(a);
        Validate.notNull(b);
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
}
