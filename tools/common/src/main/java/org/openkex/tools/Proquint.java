/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.tools;

import java.util.Arrays;
import java.util.Locale;

/**
 * class to encode and decode proquint byte representation
 * <p>
 * see: https://arxiv.org/html/0901.4016
 */
public class Proquint {

    /** representing two bits (0-3) */
    private static final char[] VOWELS = {'a', 'i', 'o', 'u'};

    /** representing four bits (0-15) */
    private static final char[] CONSONANTS = {'b', 'd', 'f', 'g', 'h', 'j', 'k', 'l', 'm', 'n', 'p', 'r', 's', 't', 'v', 'z'};

    private static final char SEPARATOR = '-';

    // for error log
    private static final String VOWEL_NAME = "vowels";
    // for error log
    private static final String CONSONANT_NAME = "consonants";

    private Proquint() {
    }

    /**
     * decode proquint String
     * @param proquint String like "bofip-sipog-fufaj"
     * @return decoded byte array
     */
    public static byte[] decode(String proquint) {
        Validate.notNull(proquint);
        Validate.isTrue((proquint.length() + 1) % 6 == 0, "wrong size: " + proquint.length());
        int blocks = (proquint.length() + 1) / 6;
        byte[] result = new byte[blocks * 2];
        for (int b = 0; b < blocks; b++) {
            int pos = b * 6;
            String proBlock = proquint.substring(pos, pos + 5).toLowerCase(Locale.ENGLISH);
            long word = 0;
            word |= arrayPos(CONSONANT_NAME, CONSONANTS, proBlock.charAt(0)) << 12;
            word |= arrayPos(VOWEL_NAME, VOWELS, proBlock.charAt(1)) << 10;
            word |= arrayPos(CONSONANT_NAME, CONSONANTS, proBlock.charAt(2)) << 6;
            word |= arrayPos(VOWEL_NAME, VOWELS, proBlock.charAt(3)) << 4;
            word |= arrayPos(CONSONANT_NAME, CONSONANTS, proBlock.charAt(4));
            result[2 * b + 1] = (byte) (word & 0xff);
            result[2 * b] = (byte) (word >> 8 & 0xff);
            if (b < blocks - 1) { // not the last block?
                char check = proquint.charAt(pos + 5);
                Validate.isTrue(check == SEPARATOR, "wrong separator: " + check);
            }
        }
        return result;
    }

    /**
     * encode to proquint
     * @param bytes bytes to encode, need to be 16 bit aligned (i.e. even length)
     * @return encoded String like "bofip-sipog-fufaj"
     */
    public static String encode(byte[] bytes) {
        Validate.notNull(bytes);
        Validate.isTrue(bytes.length % 2 == 0, "need even number of bytes (16bit aligned). got: " + bytes.length);
        StringBuilder sb = new StringBuilder(bytes.length * 3);

        // each block has two bytes
        // ccccvvcc ccvvcccc
        // 76543210 76543210
        for (int i = 0; i < bytes.length; i += 2) {
            byte[] wordBytes = Arrays.copyOfRange(bytes, i, i + 2);
            long word = NumberByteConverter.bytesToNumber(wordBytes, 2);
            sb.append(CONSONANTS[(int) ((word & 0xf000) >> 12)]);
            sb.append(VOWELS[(int) ((word & 0xc00) >> 10)]);
            sb.append(CONSONANTS[(int) ((word & 0x3c0) >> 6)]);
            sb.append(VOWELS[(int) ((word & 0x30) >> 4)]);
            sb.append(CONSONANTS[(int) (word & 0xf)]);
            if (i < bytes.length - 2) { // not the last block?
                sb.append(SEPARATOR);
            }
        }
        return sb.toString();
    }

    private static int arrayPos(String type, char[] chars, char character) {
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == character) {
                return i;
            }
        }
        throw new RuntimeException("character not found in " + type + ": " + character);
    }
}
