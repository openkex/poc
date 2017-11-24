/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.tools.merkle.test;

import org.junit.Assert;
import org.junit.Test;
import org.openkex.tools.ByteArrayTool;
import org.openkex.tools.Hex;
import org.openkex.tools.RandomTool;
import org.openkex.tools.Timer;
import org.openkex.tools.merkle.MerkleTreeSimple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

public class MerkleTreeSimpleTest {

    private static final Logger LOG = LoggerFactory.getLogger(MerkleTreeSimpleTest.class);

    private static final long SEED = 47111213;
    private static final String HASH_ALGORITHM = "SHA-256";
    private static final int HASH_SIZE = 32;

    @Test
    public void testSmall() {
        MerkleTreeSimple tree = getTestTree(HASH_ALGORITHM, HASH_SIZE, SEED, 16, false, false);
        LOG.info("got tree hash=" + Hex.toString(tree.getRootHash()));
    }

    @Test
    public void testProve() throws Exception {
        int size = 4000;
        int depth = 12;
        MerkleTreeSimple tree = getTestTree(HASH_ALGORITHM, HASH_SIZE, SEED, size, false, false);
        MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
        byte[] rootHash = tree.getRootHash();
        for (int i = 0; i < size; i++) {
            List<byte[]> prove = tree.getProveList(i);
            Assert.assertTrue(prove.size() <= depth);
            Assert.assertTrue(MerkleTreeSimple.validateProveList(prove, tree.getLeaveHash(i), rootHash, digest));
        }
    }

    private MerkleTreeSimple getTestTree(String algorithm, int hashSize, long seed, long size, boolean sort, boolean log) {
        Timer t = new Timer("merkle tree with size " + size, "random", log);
        RandomTool randomTool = new RandomTool(seed);
        ArrayList<byte[]> hashes = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            if (i % 50000 == 0 && i > 150000) {
                LOG.info("generating at: " + i);
            }
            hashes.add(randomTool.getBytes(hashSize));
        }

        if (sort) {
            t.split("sort", log);
            hashes.sort(ByteArrayTool::compareByteArray);
        }
        t.split("merkle", log);
        MerkleTreeSimple m = new MerkleTreeSimple(hashes, algorithm);
        t.stop(log);
        return m;
    }

    @Test
    public void testSpeed() {

        try {
//            int startSize = 10000; use this with -Xmx64m for memory limit test
            int startSize = 1000;
            int size = startSize;
            int steps = 3; // 3 steps with 10 times the size

            for (int step = 0; step < steps; step++) {
                getTestTree(HASH_ALGORITHM, HASH_SIZE, SEED, size, false, true);
                size *= 10;
            }

            size = startSize;
            for (int step = 0; step < steps; step++) {
                getTestTree(HASH_ALGORITHM, HASH_SIZE, SEED, size, true, true);
                size *= 10;
            }
        }
        catch (Throwable e) {  // this will log java.lang.OutOfMemoryError
            LOG.error("got exception", e);
        }
    }

    @Test
    public void testBadAlgorithm() {
        ArrayList<byte[]> hashes = new ArrayList<>();
        try {
            new MerkleTreeSimple(hashes, "sha-nix");
            Assert.fail();
        }
        catch (Exception e) {
            LOG.info("expected: " + e);
        }
    }

    @Test
    public void testBadSize() {
        ArrayList<byte[]> hashes = new ArrayList<>();
        hashes.add(new byte[33]);  // too large
        try {
            new MerkleTreeSimple(hashes, HASH_ALGORITHM);
            Assert.fail();
        }
        catch (Exception e) {
            LOG.info("expected: " + e);
        }
    }

    @Test
    public void testBitChange() {
        // check that if a single bit is changed the merkle root will change.

        RandomTool randomTool = new RandomTool(4712);
        ArrayList<byte[]> hashes = new ArrayList<>();
        int size = 1000;

        for (int i = 0; i < size; i++) {
            hashes.add(randomTool.getBytes(HASH_SIZE));
        }

        MerkleTreeSimple tree = new MerkleTreeSimple(hashes, HASH_ALGORITHM);
        byte[] root1 = tree.getRootHash();

        // change bit 122 in hash 416
        ByteArrayTool.toggleBit(hashes.get(418), 122);

        tree = new MerkleTreeSimple(hashes, HASH_ALGORITHM);
        byte[] root2 = tree.getRootHash();

        // statistically 50% of bits will changed, checking for approx 40%
        Assert.assertTrue(ByteArrayTool.bitsDifferent(root1, root2) > 100);
    }

}
