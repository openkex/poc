/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.tools;

import java.nio.charset.Charset;
import java.util.Locale;

public class RandomCheck {

    private RandomCheck() {
    }

    /**
     * try to check if kexId bytes are random generated
     * <p>
     * Note: this is an heuristic check. is should avoid manual and trivial input.
     * it will have obvious false positives. try to keep these below 0.1 percent.
     *
     * @param bytes the bytes to check (6 bytes for kexId)
     * @return true if estimated to be random
     */
    public static boolean estimateRandom(byte[] bytes) {

        Validate.notNull(bytes);
        Validate.isTrue(bytes.length == 6, "wrong length: " + bytes.length);

        if (bytes[0] == 0 && bytes[1] == 0) {
            return false; // well. call this domain rule...
        }

        // this is not very significant for 6 bytes...
        double entropy = Entropy.getShannonEntropy(bytes);
        if (entropy < 2.2) {
            return false;
        }

        // check for sequence and patterns
        byte[] diff = new byte[] {
            (byte) (bytes[1] - bytes[0]),
            (byte) (bytes[2] - bytes[1]),
            (byte) (bytes[3] - bytes[2]),
            (byte) (bytes[4] - bytes[3]),
            (byte) (bytes[5] - bytes[4]),
        };
        double diffEntropy = Entropy.getShannonEntropy(diff);
        if (diffEntropy < 1.9) {
            return false;
        }

        Charset ascii = Charset.forName("ASCII");

        String letters = getRange('a', 26);
        String numbers = getRange('0', 10);
        // interpret as ascii string
        String bytesAsString = new String(bytes, ascii);

        // check for limited range
        int alphanumericSeq = countRangeSequence(bytesAsString, numbers + letters + letters.toUpperCase(Locale.ENGLISH));
        if (alphanumericSeq > 3) {
            return false;
        }

        int alphanumericTotal = countRange(bytesAsString, numbers + letters + letters.toUpperCase(Locale.ENGLISH));
        if (alphanumericTotal > 4) {
            return false;
        }

        // check nibble patterns
        String hexString = Hex.toString(bytes);
        int printingHexSeq = countRangeSequence(hexString, "ABCDEF");
        if (printingHexSeq > 6) {
            return false;
        }

        int printingHexTotal = countRange(hexString, "ABCDEF");
        if (printingHexTotal > 9) {
            return false;
        }

        // nibble entropy
        double nibbleEntropy = Entropy.getShannonEntropy(hexString.getBytes(ascii));
        if (nibbleEntropy < 2.2) {
            return false;
        }

        return true;
    }

    private static String getRange(char start, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            char c = (char) (start + i);
            sb.append(c);
        }
        return sb.toString();
    }

    public static int countRange(String input, String range) {
        int count = 0;
        for (int i = 0; i < input.length(); i++) {
            boolean match = range.contains("" + input.charAt(i));
            if (match) {
                count++;
            }
        }
        return count;
    }

    // public for testing...
    public static int countRangeSequence(String input, String range) {
        int maxSeq = 0;
        int seq = 0;
        boolean sequence = false;
        for (int i = 0; i < input.length(); i++) {
            boolean match = range.contains("" + input.charAt(i));
            if (sequence) {
                if (match) {
                    // sequence continues
                    seq++;
                }
                else {
                    // sequence ends. update max.
                    if (seq > maxSeq) {
                        maxSeq = seq;
                    }
                    sequence = false;
                }
            }
            else if (match) {
                // sequence starts.
                sequence = true;
                seq = 1;
            }
        }
        if (sequence) {  // sequence ends with input, do final check
            if (seq > maxSeq) {
                maxSeq = seq;
            }
        }

        return maxSeq;
    }

}
