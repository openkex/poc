/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.cardio.smartcardhsm;

import org.openkex.cardio.common.APDUTool;
import org.openkex.cardio.common.cvc.CVCTag;
import org.openkex.cardio.common.cvc.CVCTagLengthValue;
import org.openkex.cardio.common.cvc.CVCTool;
import org.openkex.cardio.common.cvc.CVCertificate;
import org.openkex.cardio.common.cvc.CVCertificateRequest;
import org.openkex.tools.Hex;
import org.openkex.tools.Validate;
import org.openkex.tools.crypto.ECCTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.smartcardio.CardChannel;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Date;

/**
 * SmartCard-HSM is a product by the German company CardContact.
 * <p>
 * see http://www.smartcard-hsm.com/
 * <p>
 * it supports a sufficient  amount (30+) of ECC or RSA keys.
 * <p>
 * technically it is a java card applet based on the JCOP standard
 */
public class SmartCardHsm {

    /**
     * this is the Root Certificate of SmartCard HSM. CAR=CHR=DESRCACC100001
     * <p>
     * see https://github.com/CardContact/scsh-scripts/blob/master/sc-hsm/lib/smartcardhsm.js (lines 53 ff)
     */
    public static final String ROOT_CVC =
            "7F218201B47F4E82016C5F290100420E44455352434143433130303030317F4982011D060A04007F0007" +
            "02020202038120A9FB57DBA1EEA9BC3E660A909D838D726E3BF623D52620282013481D1F6E537782207D5A0975FC2C3057EEF" +
            "67530417AFFE7FB8055C126DC5C6CE94A4B44F330B5D9832026DC5C6CE94A4B44F330B5D9BBD77CBF958416295CF7E1CE6BCC" +
            "DC18FF8C07B68441048BD2AEB9CB7E57CB2C4B482FFC81B7AFB9DE27E1E3BD23C23A4453BD9ACE3262547EF835C3DAC4FD97F" +
            "8461A14611DC9C27745132DED8E545C1D54C72F0469978520A9FB57DBA1EEA9BC3E660A909D838D718C397AA3B561A6F7901E" +
            "0E82974856A78641046D025A8026CDBA245F10DF1B72E9880FFF746DAB40A43A3D5C6BEBF27707C30F6DEA72430EE3287B066" +
            "5C1EAA6EAA4FA26C46303001983F82BD1AA31E03DA0628701015F200E44455352434143433130303030317F4C10060B2B0601" +
            "040181C31F0301015301C05F25060102010100095F24060302010100085F37409DBB382B1711D2BAACB0C623D40C6267D0B52" +
            "BA455C01F56333DC9554810B9B2878DAF9EC3ADA19C7B065D780D6C9C3C2ECEDFD78DEB18AF40778ADF89E861CA";

    @Deprecated // TODO: not yet working
    public static final String BP256R1 = "brainpoolP256r1";
    public static final String SECP256K1 = "secp256k1";

    /**
     * TLV structure defining public key with OID=0.4.0.127.0.7.2.2.2.2.3 and curve is brainpoolP256r1
     */
    // public for testing
    public static final String BP256R1_STRING = "7F4982011D060A04007F0007" +
            "02020202038120A9FB57DBA1EEA9BC3E660A909D838D726E3BF623D52620282013481D1F6E537782207D5A0975FC2C3057EEF" +
            "67530417AFFE7FB8055C126DC5C6CE94A4B44F330B5D9832026DC5C6CE94A4B44F330B5D9BBD77CBF958416295CF7E1CE6BCC" +
            "DC18FF8C07B68441048BD2AEB9CB7E57CB2C4B482FFC81B7AFB9DE27E1E3BD23C23A4453BD9ACE3262547EF835C3DAC4FD97F" +
            "8461A14611DC9C27745132DED8E545C1D54C72F0469978520A9FB57DBA1EEA9BC3E660A909D838D718C397AA3B561A6F7901E" +
            "0E82974856A78641046D025A8026CDBA245F10DF1B72E9880FFF746DAB40A43A3D5C6BEBF27707C30F6DEA72430EE3287B066" +
            "5C1EAA6EAA4FA26C46303001983F82BD1AA31E03DA062870101";

    /**
     * TLV structure defining public key with OID=0.4.0.127.0.7.2.2.2.2.3 and curve is secp256k1
     */
    public static final String SECP256K1_STRING = "7F4981DA060A04007F00" +
            "0702020202038120FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF" +
            "FFFFFFFEFFFFFC2F822000000000000000000000000000000000000000000000" +
            "0000000000000000000083200000000000000000000000000000000000000000" +
            "00000000000000000000000784410479BE667EF9DCBBAC55A06295CE870B0702" +
            "9BFCDB2DCE28D959F2815B16F81798483ADA7726A3C4655DA4FBFC0E1108A8FD" +
            "17B448A68554199C47D08FFB10D4B88520FFFFFFFFFFFFFFFFFFFFFFFFFFFFFF" +
            "FEBAAEDCE6AF48A03BBFD25E8CD0364141870101";

    private static final Logger LOG = LoggerFactory.getLogger(SmartCardHsm.class);

    /**
     * answer to reset
     */
    private static final byte[] ATR = Hex.fromString("3BFE1800008131FE458031815448534D31738021408107FA");
    private static final byte[] ATR_CL = Hex.fromString("3B8E80018031815448534D3173802140810718");

    /**
     * application ID
     */
    private static final byte[] AID = Hex.fromString("E82B0601040181C31F0201");

    /**
     * file id of device certificate
     */
    private static final byte[] FID_C_DEV = Hex.fromString("2F02");

    /** prefix of private key file */
    private static final byte FID_PRIVATE = (byte) 0xCC;

    /** prefix of private key file */
    private static final byte FID_PUBLIC = (byte) 0xCE;

    private byte[] deviceCertificate;

    private CVCertificate device;
    private CVCertificate dica;

    private CardChannel channel;

    private String version;

    private boolean initialized;

    /**
     * constructor assumed valid citizen card connected
     *
     * @param channel card channel to use
     * @throws Exception if card communication fails
     */
    public SmartCardHsm(CardChannel channel) throws Exception {
        this.channel = channel;

        int p2 = 0x00; // return FCI   // resp 6F0782017885020102 // -> V1.2
//        int p2 = 0x04; // return FCP // resp 620782017885020102, same "content"
//        int p2 = 0x0C; // return nothing
        ResponseAPDU r = APDUTool.processAPDU(channel, new CommandAPDU(0x00, 0xA4, 0x04, p2, AID, 0), true);
        byte[] data = r.getData();
        String aidString = Hex.toString(data);
        // currently 1.2 / 2.0 only
        // Version 1.2 has length=9
        if (data.length == 9) {
            Validate.isTrue(data[7] == 1);
            Validate.isTrue(data[8] == 2);
            version = "1.2";
        }
        // Version 2.0 has length=12
        else if (data.length == 12) {
            // could read "initialized" from response ...
            Validate.isTrue(data[10] == 2);
            Validate.isTrue(data[11] == 0);
            version = "2.0";
        }
        else {
            throw new RuntimeException("unexpected response length of 'select AID': " + aidString);
        }
        int retires = verifyPIN(null);
        initialized = retires != -2;
        String status = initialized ? (retires + " retries") : "not initialized";

        // pre load certificates
        deviceCertificate = readFile(FID_C_DEV);
        // two certificates in file ("one" must know)
        ByteArrayInputStream is = new ByteArrayInputStream(getDeviceCertificate());
        device = CVCTool.parse(is, false);
        dica = CVCTool.parse(is, false);

        LOG.info("SmartCardHSM Version: '" + version + "' Status: '" + status + "' CHR='" +
                device.getCertificateHolderReference() + "' Select AID response: '" + aidString + "'");

    }

    public void close() {
        this.channel = null;
        this.deviceCertificate = null;
    }

    public boolean isInitialized() {
        return initialized;
    }

    /**
     * "certificate" for device authentication. file contains Device Issuer Certificate and Device Certificate.
     * (this is not specified)
     * <p>
     * format is based on BSI TR-03110
     * <p>
     * see: https://www.bsi.bund.de/EN/Publications/TechnicalGuidelines/TR03110/BSITR03110.html
     *
     * @return certificate content.
     * @throws Exception if card fails
     */
    public byte[] getDeviceCertificate() throws Exception {
        return deviceCertificate;
    }

    public String getCHR() {
        return device.getCertificateHolderReference();
    }

    /**
     * check if device certificate is valid.
     *
     * @throws Exception if certificate is invalid
     */
    public void checkDeviceCertificate() throws Exception {
        CVCertificate root = CVCTool.parse(new ByteArrayInputStream(Hex.fromString(ROOT_CVC)), false);
        LOG.info("checkDeviceCertificate:");
        LOG.info("SC HSM Root   : " + root);
        LOG.info("Device Issuer : " + dica);
        LOG.info("Device        : " + device);

        // 1) CAR/CHR chain
        Validate.isTrue(root.getCertificateAuthorityReference().equals(root.getCertificateHolderReference()));
        Validate.isTrue(dica.getCertificateAuthorityReference().equals(root.getCertificateHolderReference()));
        Validate.isTrue(device.getCertificateAuthorityReference().equals(dica.getCertificateHolderReference()));

        // 2) validity dates
        Date now = new Date();
        root.checkDate(now);
        dica.checkDate(now);
        device.checkDate(now);

        // 3) signatures

        // root: self-signed
        PublicKey pkRoot = ECCTool.getPublicKeyFromCurvePoint(root.getPublicKey(), ECCTool.CURVE_BRAINPOOLP256R1);
        boolean rootValid = ECCTool.verify(root.getBody(), ECCTool.encodeRawSignature(root.getSignature()), pkRoot);
        Validate.isTrue(rootValid);

        // dica: signed with root
        boolean dicaValid = ECCTool.verify(dica.getBody(), ECCTool.encodeRawSignature(dica.getSignature()), pkRoot);
        Validate.isTrue(dicaValid);

        // device: signed with dica
        PublicKey pkDica = ECCTool.getPublicKeyFromCurvePoint(dica.getPublicKey(), ECCTool.CURVE_BRAINPOOLP256R1);
        boolean deviceValid = ECCTool.verify(device.getBody(), ECCTool.encodeRawSignature(device.getSignature()), pkDica);
        Validate.isTrue(deviceValid);

        // finally: no error found, device certificate is valid with pre defined ROOT_CVC ....
        LOG.info("checkDeviceCertificate: verify OK");
    }

    /**
     * check if key is valid (verify according Certificate Request against device certificate)
     *
     * @param keyId id of key
     * @throws Exception if key signature is invalid
     */
    public void checkKeySignature(int keyId) throws Exception {
        LOG.info("checkKeySignature: keyId=" + keyId);
        byte[] csr = readFile(new byte[] {FID_PUBLIC, (byte) keyId});

        CVCertificateRequest request = CVCTool.parseRequest(new ByteArrayInputStream(csr));
        LOG.info("CR=" + request);

        CVCertificate inner = request.getCertificate();
        // "inner" signature: self signed...
        PublicKey pkKey = ECCTool.getPublicKeyFromCurvePoint(inner.getPublicKey(), ECCTool.CURVE_SECP256K1);
        boolean keySelfValid = ECCTool.verify(inner.getBody(), ECCTool.encodeRawSignature(inner.getSignature()), pkKey);
        Validate.isTrue(keySelfValid);

        // validate chain
        Validate.isTrue(request.getCertificateAuthorityReference().equals(device.getCertificateHolderReference()));

        // "outer" signature: signed by device key

        // signed data: encoded certificate + encoded CAR
        ByteArrayOutputStream signed = new ByteArrayOutputStream();
        signed.write(inner.getData());
        signed.write(request.getCarData());
        // CVCTool.dump(new ByteArrayInputStream(signed.toByteArray()));

        PublicKey pkDevice = ECCTool.getPublicKeyFromCurvePoint(device.getPublicKey(), ECCTool.CURVE_BRAINPOOLP256R1);
        boolean keyValid = ECCTool.verify(signed.toByteArray(), ECCTool.encodeRawSignature(request.getSignature()), pkDevice);
        Validate.isTrue(keyValid, "key + " + keyId + " is not valid.");

        LOG.info("checkKeySignature keyId=" + keyId + " was OK");
        // todo: maybe export "full chain" as prove
    }

    /**
     * @param atr the ATR to check
     * @return true if card is SmartCard-HSM
     */
    public static boolean matchAtr(byte[] atr) {
        return Arrays.equals(atr, ATR) || Arrays.equals(atr, ATR_CL);
    }

    /**
     * get "true" random bytes (uses "TRNG" on card)
     *
     * @param bytes number of bytes (max 1024)
     * @return random bytes
     * @throws Exception if card fails
     */
    public byte[] getRandom(int bytes) throws Exception {
        LOG.info("getRandom: bytes=" + bytes);
        ResponseAPDU r = APDUTool.processAPDU(channel, new CommandAPDU(0x00, 0x84, 0x00, 0x00, bytes), true);
        return r.getData();
    }

    /**
     * enter User PIN to unlock or check remaining tries
     *
     * @param password PIN for unlock, null to "check"
     * @return -1 if login was OK. remaining tries if unlock failed or "check"
     * @throws Exception if card fails
     */
    public int verifyPIN(byte[] password) throws Exception {
        return verifyPIN(false, password);
    }

    /**
     * enter PIN to unlock or check remaining tries
     * <p>
     * Note: SO pin works only for SmartCard HSM 2.0
     *
     * @param securityOfficer if true SO PIN, if false User PIN
     * @param pin PIN for unlock, null to check
     * @return -1 if login was OK. -2 if card is not initialized. remaining tries if unlock failed or "check"
     * @throws Exception if card fails
     */
    public int verifyPIN(boolean securityOfficer, byte[] pin) throws Exception {
        LOG.info("verifyPIN: SO=" + securityOfficer + " pin length=" + (pin == null ? "0" : pin.length));
        if (securityOfficer && version.equals("1.2")) { // could do this more "precise"...
            throw new RuntimeException("Version 1.2 does not support verify of SO PIN");
        }
        int p2 = securityOfficer ? 0x88 : 0x81;
        ResponseAPDU r = null;
        if (pin == null) {
            r = APDUTool.processAPDU(channel, new CommandAPDU(0x00, 0x20, 0x00, p2), false);
        }
        else {
            r = APDUTool.processAPDU(channel, new CommandAPDU(0x00, 0x20, 0x00, p2, pin), false);
        }
        int code = r.getSW();
        if ((code & 0x63C0) == 0x63C0) {
            return code & 0x000F;
        }
        else if (code == 0x9000) {
            return -1; // password OK
        }
        else if (code == 0x6984) {
            return -2; // not initialized (is this a good test?)
        }
        throw new RuntimeException("unexpected response code=0x" + Integer.toHexString(code));
    }

    /**
     * change PIN
     *
     * @param securityOfficer if true SO PIN, if false User PIN
     * @param oldPin          old pin value
     * @param newPin          new pin value
     * @return -1 if change was OK. remaining tries if change failed.
     * @throws Exception if card fails
     */
    public int changePIN(boolean securityOfficer, byte[] oldPin, byte[] newPin) throws Exception {
        LOG.info("changePIN: SO=" + securityOfficer);
        int p2 = securityOfficer ? 0x88 : 0x81;
        ResponseAPDU r = null;
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        os.write(oldPin);
        os.write(newPin);
        byte[] data = os.toByteArray();
        r = APDUTool.processAPDU(channel, new CommandAPDU(0x00, 0x24, 0x00, p2, data), false);
        int code = r.getSW();

        if ((code & 0x63C0) == 0x63C0) {
            return code & 0x000F;  // remaining count
        }
        else if (code == 0x9000) {
            return -1; // change OK
        }
        throw new RuntimeException("unexpected response code=0x" + Integer.toHexString(code));
    }

    /**
     * initialize or reset (i.e. re-initialize) card
     *
     * @param pin byte array with encoded pin
     * @param soPin desired (initialize) or current (reset) security officer PIN
     * @throws Exception if card fails
     */
    public void initialize(byte[] soPin, byte[] pin) throws Exception {
        LOG.info("initialize");
        Validate.isTrue(soPin.length == 8);
        Validate.isTrue(pin.length > 4); // ??
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        os.write(Hex.fromString("80020001"));
        os.write(0x81);
        os.write(pin.length);
        os.write(pin);
        os.write(0x82);
        os.write(soPin.length);
        os.write(soPin);
        os.write(0x91);
        os.write(1);
        os.write(3); // Note: fails with 5.
        APDUTool.processAPDU(channel, new CommandAPDU(0x80, 0x50, 0x00, 0x00, os.toByteArray()), true);
    }

    /**
     * get list of available file IDs
     *
     * @return array of file IDs. each as array with 2 bytes
     * @throws Exception if card fails
     */
    public byte[][] listObjects() throws Exception {
        LOG.info("listObjects");
        // ResponseAPDU r = APDUTool.processAPDU(channel, new CommandAPDU(0x80, 0x58, 0x00, 0x00), true);
        // extended APDU, need 3x 00 byte as "LE", cannot use "normal" constructor.
        ResponseAPDU r = APDUTool.processAPDU(channel, new CommandAPDU(new byte[]{(byte) 0x80, 0x58, 0x00, 0x00, 0x00, 0x00, 0x00}), true);

        byte[] data = r.getData();
        Validate.isTrue(data.length % 2 == 0);
        byte[][] ids = new byte[data.length / 2][2];
        StringBuilder log = new StringBuilder();

        for (int i = 0; i < ids.length; i++) {
            ids[i][0] = data[i * 2];
            ids[i][1] = data[i * 2 + 1];
            if (log.length() > 0) {
                log.append(", ");
            }
            log.append(Hex.toString(ids[i]));
        }
        LOG.info("listObjects got IDs: " + log);
        return ids;
    }

    /**
     * read content of file
     * <p>
     * Note: SmartCard-HSM has its own (alternative) file access APDUs
     *
     * @param fileId id of file
     * @return content of file, null if file not found
     * @throws Exception if card fails
     */
    public byte[] readFile(byte[] fileId) throws Exception {

        int byteNr = 0; // file offset in bytes

        // for byte array concatenation
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        do {
            byte[] command = new byte[]{0x54, 0x02, (byte) (byteNr / 256), (byte) (byteNr % 256)};
            // TODO: check FID MSB/LSB mixed up?
            ResponseAPDU resp = APDUTool.processAPDU(channel, new CommandAPDU(0x00, 0xB1, fileId[0], fileId[1], command, 0xFF), false);
            int status = resp.getSW();
            if (status == 0x6A82) { // file not found
                LOG.info("readFile: id=" + Hex.toString(fileId) + " was not found.");
                return null;
            }
            if (status != 0x9000 && status != 0x6282) {
                throw new RuntimeException("invalid status=0x" + Integer.toHexString(status));
            }
            byte[] data = resp.getData();
//            if (data.length == 0) {  // required if LE=0, using LE=0xff now.
//                break;
//            }
            baos.write(data);
            if (resp.getSW() == 0x6282) { // end of file reached
                break;
            }
            byteNr += data.length;
        }
        while (true);
        byte[] result = baos.toByteArray(); // full result
        LOG.info("readFile: id=" + Hex.toString(fileId) + " length=" + result.length);
        return result;
    }

    /**
     * update or create file
     * <p>
     * Note: Update will replace existing bytes but will not "truncate" original content
     *
     * @param fileId id of file
     * @param data   data to write
     * @throws Exception if card fails
     */
    public void updateFile(byte[] fileId, byte[] data) throws Exception {
        LOG.info("updateFile: id=" + Hex.toString(fileId) + " length=" + data.length);

        int byteNr = 0; // file offset in bytes
        int blockSize = 0x7F; // small to keep one byte "length" encoding

        do {
            int remain = data.length - byteNr;
            int count = remain < blockSize ? remain : blockSize;
            ByteArrayOutputStream command = new ByteArrayOutputStream();
            command.write(new byte[]{0x54, 0x02, (byte) (byteNr / 256), (byte) (byteNr % 256), 0x53});
            command.write(count);
            command.write(data, byteNr, count);

            ResponseAPDU resp = APDUTool.processAPDU(channel, new CommandAPDU(0x00, 0xD7, fileId[0], fileId[1], command.toByteArray()), false);
            int status = resp.getSW();
            if (status != 0x9000) {
                throw new RuntimeException("invalid status=0x" + Integer.toHexString(status));
            }
            byteNr += count;
        }
        while (byteNr < data.length);
    }

    /**
     * delete file content
     *
     * @param fileId id of file to delete
     * @return true if successful, false if not found
     * @throws Exception if card fails
     */
    public boolean deleteFile(byte[] fileId) throws Exception {
        LOG.info("deleteFile: id=" + Hex.toString(fileId));
        ResponseAPDU resp = APDUTool.processAPDU(channel, new CommandAPDU(0x00, 0xE4, 0x02, 0x0, fileId), false);
        int status = resp.getSW();
        if (status == 0x9000) {
            return true;
        }
        else if (status == 0x6A82) {
            return false;  // file not found
        }
        else if (status == 0x6982) {
            throw new RuntimeException("Security condition not satisfied (missing or invalid PIN)");
        }
        else {
            // e.g.  0x6982 not allowed in case of missing "validatePin"
            throw new RuntimeException("invalid status=0x" + Integer.toHexString(status));
        }
    }

    /**
     * delete key
     *
     * @param keyId id of key
     * @return true if successful, false if not found
     * @throws Exception if card fails
     */
    public boolean deleteKey(int keyId) throws Exception {
        LOG.info("deleteKey: keyId=" + keyId);
        // NOTE: delete private key deletes pair.
        boolean privOK = deleteFile(new byte[] {FID_PRIVATE, (byte) keyId});
        if (privOK) {
            boolean pubOK = deleteFile(new byte[] {FID_PUBLIC, (byte) keyId});
            if (!pubOK) {
                LOG.warn("failed to delete public key. id=" + keyId);
            }
        }
        return privOK;
    }

    /**
     * sign a hash with specific key
     *
     * @param keyId id of key
     * @param hash hash value
     * @return signature value
     * @throws Exception if card fails
     */
    public byte[] sign(byte[] hash, int keyId) throws Exception {
        LOG.info("sign: keyId=" + keyId);
        int algorithm = 0x70;  // ECDSA with "external hash"
        ResponseAPDU resp = APDUTool.processAPDU(channel, new CommandAPDU(0x80, 0x68, keyId, algorithm, hash), true);
        return resp.getData();
    }

    /**
     * generate key pair
     *
     * @param algorithm key algorithm
     * @param keyId id of key
     * @param idRef id of key for CSR, 0 for device key
     * @throws Exception if card fails
     */
    public void generatePair(String algorithm, int keyId, int idRef) throws Exception {
        LOG.info("generatePair: algorithm=" + algorithm + " keyId=" + keyId + " idRef=" + idRef);
        // this is a TLV ASN1 thing (format also used by CVC)
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        os.write(Hex.fromString("5F290100")); // CPI: TLV

        // certificate authority reference
        os.write(Hex.fromString("42")); // CAR: T
        String car = "UTCA00001";  // more or less "don't care"
        os.write(car.length()); // CAR: L
        os.write(car.getBytes("ISO-8859-1")); // CAR: V

        if (algorithm.equalsIgnoreCase(BP256R1)) {
            os.write(Hex.fromString(BP256R1_STRING));
        }
        else if (algorithm.equalsIgnoreCase(SECP256K1)) {
            os.write(Hex.fromString(SECP256K1_STRING));
        }
        // TODO: more curves
        else {
            throw new RuntimeException("found no PK parameters for: " + algorithm);
        }
        // certificate holder reference
        os.write(Hex.fromString("5F20")); // CHR: T
        String chr = "KID_8F354DA3F642D34A"; // more or less "don't care" test: 48bit kexId
        os.write(chr.length()); // CHR: L
        os.write(chr.getBytes("ISO-8859-1")); // CHR: V

        // outer certificate authority reference (required if idRef != 0)
//        os.write(Hex.fromString("45")); // CHR: T
//        String ocar = "DECM010444800000";
//        os.write(ocar.length()); // CHR: L
//        os.write(ocar.getBytes("ISO-8859-1")); // CHR: V

//        CVCTool.dump(new ByteArrayInputStream(os.toByteArray()));

        APDUTool.processAPDU(channel, new CommandAPDU(0x00, 0x46, keyId, idRef, os.toByteArray()), true);
    }

    /**
     * get public key for key pair (curve point)
     *
     * @param keyId id of key pair
     * @return encoded ECC curve point of public key (0x04 ..), null if key not found
     * @throws Exception if card fails
     */
    public CVCTagLengthValue getPublicKey(int keyId) throws Exception {
        LOG.info("getPublicKey: keyId=" + keyId);
        byte[] csr = readFile(new byte[] {FID_PUBLIC, (byte) keyId});
        if (csr == null) {
            return null;
        }
        return CVCTool.getFirst(new ByteArrayInputStream(csr), CVCTag.ECC_PUBLIC_POINT.getValue());
    }

    /**
     * ECC decipher (ECDH primitive operation)
     *
     * @param keyId id of key pair
     * @param publicPoint ECC public point
     * @return  decoded ECC point
     * @throws Exception if card fails
     */
    public byte[] eccDecode(int keyId, byte[] publicPoint) throws Exception {
        LOG.info("eccDecode: keyId=" + keyId);
        int algorithm = 0x80; // ECC decipher
        // note: need to set correct response size ("LE"=65)
        ResponseAPDU resp = APDUTool.processAPDU(channel, new CommandAPDU(0x80, 0x62, keyId, algorithm, publicPoint, 65), true);
        return resp.getData();
    }
}
