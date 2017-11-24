/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.cardio.common;

import org.openkex.tools.Validate;

import java.util.Arrays;

public class PinTool {

    // use char[] NOT String. String instances cannot be "removed"
    private char[] input;
    private byte[] encoded;

    private int minLength;
    private int maxLength;

    public PinTool(int minLength, int maxLength) {
        Validate.isTrue(minLength >= 0);
        Validate.isTrue(maxLength <= 14);
        this.minLength = minLength;
        this.maxLength = maxLength;
    }

    public PinTool() {
        this.minLength = 4;
        this.maxLength = 12;
    }

    public void setInput(char[] input) {
        this.input = input;
        Validate.notNull(input);
        Validate.isTrue(input.length <= maxLength, "password too long");
        Validate.isTrue(input.length >= minLength, "password too short");
        for (char anInput : input) {
            Validate.isTrue(anInput >= '0', "character not numeric: " + anInput);
            Validate.isTrue(anInput <= '9', "character not numeric: " + anInput);
        }
        // calculate encoded
        encoded = new byte[] {-1, -1, -1, -1, -1, -1, -1, -1};  // 8x 0xff
        encoded[0] = (byte) (0x20 + input.length); // length has 4 bit
        for (int i = 0; i < input.length; i++) {
            int pos = 1 + i / 2;
            boolean shift = i % 2 == 0;
            encoded[pos] &= shift ? 0x0f : 0xf0;  // set bits to zero first
            encoded[pos] |= (byte) ((input[i] - '0') << (shift ? 4 : 0));
        }
    }

    /**
     * eliminate password in memory
     */
    public void purge() {
        if (input != null) {
            Arrays.fill(input, '0');
        }
        if (encoded != null) {
            Arrays.fill(encoded, (byte) 0);
        }
    }

    public byte[] getEncoded() {
        return encoded;
    }
}
