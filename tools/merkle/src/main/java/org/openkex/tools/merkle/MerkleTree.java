/*
 * Copyright 2016 openKex. All rights reserved.
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.openkex.tools.merkle;

import org.openkex.tools.Validate;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * merkle hash tree implementation (not optimized)
 * <p>
 * https://en.wikipedia.org/wiki/Merkle_tree
 */
public class MerkleTree {

    // note: this might not be very memory efficient (but easier to code)
    private ArrayList<ArrayList<Node>> nodes = new ArrayList<>();

    // the root hash is the result of the merkle tree
    private byte[] rootHash;

    /**
     * create a merkle tree based on list of hashes
     *
     * @param hashes List of leave hash values
     * @param algorithm hash algorithm
     */
    public MerkleTree(List<byte[]> hashes, String algorithm) {
        Validate.notNull(hashes);
        Validate.isTrue(hashes.size() > 0);

        MessageDigest md;
        try {
            md = MessageDigest.getInstance(algorithm);
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        // start with level 0, list of leave nodes.
        // each level has half the node count (so level count is dual logarithm of the leave node count)
        ArrayList<Node> levelNodes = new ArrayList<>();
        nodes.add(levelNodes);
        int size = md.getDigestLength();
        for (byte[] hash : hashes) {
            // validate that hash size is correct.
            Validate.isTrue(hash.length == size, "got wrong hash size:" + hash.length + " required is:" + size);
            levelNodes.add(new Node(hash)); // leave nodes
        }

        // add next level
        while (levelNodes.size() > 1) {
            int nextSize = levelNodes.size() / 2; // number of pairs
            ArrayList<Node> nextLevelNodes = new ArrayList<>(nextSize);

            // create node for next level by pairs of two nodes
            for (int i = 0; i < nextSize; i++) {
                Node nextNode = new Node(levelNodes.get(2 * i), levelNodes.get(2 * i + 1), md);
                nextLevelNodes.add(nextNode);
            }

            // odd number: single node remains. link it to next level
            boolean odd = levelNodes.size() % 2 == 1;
            if (odd) {
                Node remaining = levelNodes.get(levelNodes.size() - 1);
                nextLevelNodes.add(new Node(remaining));
            }
            nodes.add(nextLevelNodes);

            levelNodes = nextLevelNodes; // continue with next level
        }

        Validate.isTrue(levelNodes.size() == 1);
        rootHash = levelNodes.get(0).getHash();
    }

    /**
     * @return the final root hash value
     */
    public byte[] getRootHash() {
        return rootHash;
    }

    /**
     * get leave node hash (convenience function, same hash as provided to MerkleTree constructor)
     *
     * @param index index of node
     * @return according hash value
     */
    public byte[] getLeaveHash(int index) {
        return nodes.get(0).get(index).getHash();
    }

    /**
     * get list of hashes needed to prove that leave hash is correct.
     * <p>
     * the size of the list is limited by the count of levels.
     *
     * @param index position in hash list
     * @return list af hashes for prove
     */
    public List<Prove> getProveList(int index) {
        List<Prove> proveList = new ArrayList<>();
        // get according leave node
        Node node = nodes.get(0).get(index);

        // walk "Up" merkle tree (till root node).
        while (node.getParent() != null) { // while not the root node.
            Node parent = node.getParent();
            // collect hashes of "other" paths.
            if (parent.getRightChild() == null) {
                // link node. continue.
                Validate.notNull(parent.getLeftChild());
            }
            else if (parent.getLeftChild() == node) {
                proveList.add(new Prove(true, parent.getRightChild().getHash()));
            }
            else if (parent.getRightChild() == node) {
                proveList.add(new Prove(false, parent.getLeftChild().getHash()));
            }
            else {
                throw new RuntimeException("this must not happen. please report this bug.");
            }
            // move up one level
            node = parent;
        }
        return proveList;
    }

    /**
     * node in merkle tree
     */
    private static class Node {
        private byte[] hash;
        private Node parent;
        private Node leftChild;
        private Node rightChild;

        /**
         * create new leave node (no children).
         *
         * @param hash hash of leave node
         */
        private Node(byte[] hash) {
            this.hash = hash;
            this.leftChild = null;
            this.rightChild = null;
        }

        /**
         * create new link node.
         * <p>
         * link node is "empty" link to next level. right node is null. hash is identical.
         *
         * @param child the node to link
         */
        private Node(Node child) {
            this.parent = null;
            this.hash = child.getHash();
            this.leftChild = child;
            this.rightChild = null;
            // set back references
            this.leftChild.setParent(this);
        }

        /**
         * create new "normal" node with two children. new hash is calculated.
         *
         * @param leftChild left child of node
         * @param rightChild right child of node
         * @param digest the digest to use for new hash
         */
        private Node(Node leftChild, Node rightChild, MessageDigest digest) {
            this.leftChild = leftChild;
            this.rightChild = rightChild;
            // set back references
            this.leftChild.setParent(this);
            this.rightChild.setParent(this);
            // calculate node hash
            this.hash = getNodeHash(leftChild.getHash(), rightChild.getHash(), digest);
        }

        public void setParent(Node parent) {
            this.parent = parent;
        }

        public byte[] getHash() {
            return hash;
        }

        public Node getParent() {
            return parent;
        }

        public Node getLeftChild() {
            return leftChild;
        }

        public Node getRightChild() {
            return rightChild;
        }
    }

    /**
     * a prove can be used to check that a node is valid.
     * <p>
     * it contains the "other" hash (that cannot be forged)
     * and the information if checked node was left or right child
     */
    public static class Prove {
        private boolean left;
        private byte[] hash;

        public Prove(boolean left, byte[] hash) {
            this.left = left;
            this.hash = hash;
        }

        public boolean isLeft() {
            return left;
        }

        public byte[] getHash() {
            return hash;
        }
    }

    /**
     * function to calculate node hash based on children
     *
     * @param leftHash hash of left child node
     * @param rightHash hash of right child node
     * @param digest digest to use
     * @return hash value
     */
    public static byte[] getNodeHash(byte[] leftHash, byte[] rightHash, MessageDigest digest) {
        // NOTE: this is the most trivial implementation
        digest.reset();
        digest.update(leftHash);
        digest.update(rightHash);
        return digest.digest();
    }

    /**
     * validates the prove list
     * <p>
     * the check will prove that the leave hash belongs to the root hash.
     *
     * @param proves list of proves
     * @param leaveHash the leave hash to validate
     * @param rootHash the root hash of tree
     * @param digest the digest to use
     * @return true if validation is OK
     */
    public static boolean validateProveList(List<Prove> proves, byte[] leaveHash, byte[] rootHash, MessageDigest digest) {
        byte[] hash = leaveHash; // start with leave hash.
        for (Prove prove : proves) {
            // check prove for each level, walking up the tree.
            if (prove.isLeft()) {
                hash = getNodeHash(hash, prove.getHash(), digest);
            }
            else {
                hash = getNodeHash(prove.getHash(), hash, digest);
            }
        }
        // the result must match the root hash
        return Arrays.equals(hash, rootHash);
    }

}
