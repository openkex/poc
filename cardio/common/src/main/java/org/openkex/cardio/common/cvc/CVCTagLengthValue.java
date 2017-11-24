/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.cardio.common.cvc;

import org.openkex.tools.Hex;
import org.openkex.tools.Validate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * "Tag Length Value (TLV)" Holder Object
 * <p>
 * see BSI TR-03110 (Part 3, Appendix C and D)
 */
public class CVCTagLengthValue {
    private CVCTag tag;
    private int length;
    private byte[] content;
    // copy of header bytes
    private ByteArrayOutputStream header = new ByteArrayOutputStream();

    /**
     * parse TAG from input stream
     * @param is input stream
     * @param expectedTag if not null Tag value will be "enforced" (throwing Exception in case of mismatch)
     * @throws IOException if read fails from input stream
     */
    public CVCTagLengthValue(InputStream is, CVCTag expectedTag) throws IOException {
        Validate.isTrue(is.available() > 0, "stream is empty");
        int tagValue = decodeTag(is);
        tag = CVCTag.getByValue(tagValue);
        Validate.notNull(tag, "unknown tag: " + Integer.toHexString(tagValue));
        if (expectedTag != null) {
            Validate.isTrue(tag == expectedTag, "wrong tag. got=" + tag + " expected=" + expectedTag);
        }
        length = decodeLength(is);
        content = new byte[length];
        int read = is.read(content);
        Validate.isTrue(read == length, "missing bytes from stream. need=" + length + " got=" + read);
    }

    public CVCTag getTag() {
        return tag;
    }

    public int getLength() {
        return length;
    }

    public byte[] getContent() {
        return content;
    }

    /**
     * get full TLV content (header + content)
     *
     * @return byte array with all data
     * @throws IOException if stream conversion fails
     */
    public byte[] getHeaderAndContent() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(header.toByteArray());
        baos.write(content);
        return baos.toByteArray();
    }

    /**
     * read header byte and keep copy
     *
     * @param inputStream stream to replicate
     * @return byte read from stream
     * @throws IOException if stream operation fails
     */
    private int readHeader(InputStream inputStream) throws IOException {
        int readByte = inputStream.read();
        if (readByte != -1) {
            header.write(readByte);
        }
        return readByte;
    }

    /**
     * Reads ASN.1/DER tag value from the input stream
     *
     * @param inputStream to read fromm
     * @return tag value
     * @throws IOException if stream operation fails
     */
    private int decodeTag(InputStream inputStream) throws IOException {
        int b1 = readHeader(inputStream);
        if ((b1 & 0x1F) == 0x1F) {
            int b2 = readHeader(inputStream);
            return (b1 << 8) + b2;
        }
        else {
            return b1;
        }
    }

    /**
     * read ASN.1/DER encoded length
     * <p>
     * code assumes limit of 4 bytes (approx 2 gigabyte, "enough" for smart cards.)
     *
     * @param inputStream stream with ASN.1 data
     * @return decoded integer
     * @throws IOException if stream operation fails
     */
    private int decodeLength(InputStream inputStream) throws IOException {
        int b1 = readHeader(inputStream);
        if (b1 < 0x80) {
            // short form
            return b1;
        }
        // long form
        int bytes = b1 & 0x7F; // remaining 7 bits are byte count

        Validate.isTrue(bytes <= 4, "byte limit of 4 exceeded. bytes=" + bytes);

        int decoded = 0;

        for (int i = 0; i < bytes; i++) {
            int b2 = readHeader(inputStream);
            decoded += b2 << (8 * (bytes - i - 1));  // MSB first
        }
        return decoded;
    }

    // get String representation of OID bytes
    private String getOIDString(byte[] content) {
        StringBuilder oidString = new StringBuilder(content[0] / 40 + ".");
        oidString.append(content[0] % 40);
        int value = 0;
        boolean ext = false;
        for (int i = 1; i < content.length; i++) {
            if ((content[i] & 0x80) != 0) {
                ext = true;
                value = value * 128;
                value += content[i] & 0x7F;
            }
            else {
                if (ext) {
                    value = value * 128;
                }
                value += content[i];
                oidString.append(".").append(value);
                value = 0;
                ext = false;
            }
        }
        return oidString.toString();
    }

    public String getContentString() {
        switch (tag.getType()) {
            case CharacterString:
                return new String(content, Charset.forName("ISO-8859-1"));
            case Date:
                // convert internal date format YYMMDD (e.g. "110720") to YYYY-MM-DD (e.g. "2011-07-20")
                byte[] bcdContent = new byte[length];
                for (int i = 0; i < length; i++) {
                    bcdContent[i] = (byte) (content[i] + 0x30);  // BCD to ASCII
                }
                String bcdString = new String(bcdContent, Charset.forName("ASCII"));
                return "20" + bcdString.substring(0, 2) + "-" + bcdString.substring(2, 4) + "-" + bcdString.substring(4, 6);
            case OID:
                return getOIDString(content);
            case Integer:
            case OctetString:
                return Hex.toString(content);
            default:
                throw new RuntimeException("unsupported tag type:" + tag.getType());
        }
    }

    @Override
    public String toString() {
        // do not dump content for sequence
        String content = tag.getType() == CVCTag.Type.Sequence ? "" : " content=" + getContentString();
        return tag + "(0x" + Integer.toHexString(tag.getValue()) + ") type=" + tag.getType() +
                " length=" + length + content;
    }
}
