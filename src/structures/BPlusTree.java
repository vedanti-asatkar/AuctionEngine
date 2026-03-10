package structures;

import model.Bid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BPlusTree {

    private abstract static class Node {
        protected final List<Double> keys = new ArrayList<>();

        protected abstract boolean isLeaf();
    }

    private static final class InternalNode extends Node {
        private final List<Node> children = new ArrayList<>();

        @Override
        protected boolean isLeaf() {
            return false;
        }
    }

    private static final class LeafNode extends Node {
        private final List<List<Bid>> values = new ArrayList<>();
        private LeafNode next;

        @Override
        protected boolean isLeaf() {
            return true;
        }
    }

    private static final class SplitResult {
        private final double promotedKey;
        private final Node rightNode;

        private SplitResult(double promotedKey, Node rightNode) {
            this.promotedKey = promotedKey;
            this.rightNode = rightNode;
        }
    }

    private final int order;
    private Node root;

    public BPlusTree(int order) {
        if (order < 3) {
            throw new IllegalArgumentException("B+ Tree order must be at least 3.");
        }
        this.order = order;
        this.root = new LeafNode();
    }

    public void insert(double key, Bid bid) {
        if (bid == null) {
            return;
        }

        SplitResult split = insertRecursive(root, key, bid);
        if (split == null) {
            return;
        }

        InternalNode newRoot = new InternalNode();
        newRoot.keys.add(split.promotedKey);
        newRoot.children.add(root);
        newRoot.children.add(split.rightNode);
        root = newRoot;
    }

    public List<Bid> search(double key) {
        LeafNode leaf = findLeafNode(key);
        int index = findKeyIndex(leaf.keys, key);
        if (index < leaf.keys.size() && Double.compare(leaf.keys.get(index), key) == 0) {
            return new ArrayList<>(leaf.values.get(index));
        }
        return Collections.emptyList();
    }

    public List<Bid> rangeSearch(double minAmount, double maxAmount) {
        if (Double.isNaN(minAmount) || Double.isNaN(maxAmount) || minAmount > maxAmount) {
            return Collections.emptyList();
        }

        List<Bid> result = new ArrayList<>();
        LeafNode current = findLeafNode(minAmount);
        int index = findKeyIndex(current.keys, minAmount);

        while (current != null) {
            while (index < current.keys.size()) {
                double key = current.keys.get(index);
                if (key > maxAmount) {
                    return result;
                }
                if (key >= minAmount) {
                    result.addAll(current.values.get(index));
                }
                index++;
            }
            current = current.next;
            index = 0;
        }

        return result;
    }

    private SplitResult insertRecursive(Node node, double key, Bid bid) {
        if (node.isLeaf()) {
            return insertIntoLeaf((LeafNode) node, key, bid);
        }

        InternalNode internal = (InternalNode) node;
        int childIndex = findChildIndex(internal.keys, key);
        SplitResult childSplit = insertRecursive(internal.children.get(childIndex), key, bid);
        if (childSplit == null) {
            return null;
        }

        internal.keys.add(childIndex, childSplit.promotedKey);
        internal.children.add(childIndex + 1, childSplit.rightNode);

        if (internal.keys.size() < order) {
            return null;
        }
        return splitInternalNode(internal);
    }

    private SplitResult insertIntoLeaf(LeafNode leaf, double key, Bid bid) {
        int index = findKeyIndex(leaf.keys, key);
        if (index < leaf.keys.size() && Double.compare(leaf.keys.get(index), key) == 0) {
            leaf.values.get(index).add(bid);
        } else {
            leaf.keys.add(index, key);
            List<Bid> bucket = new ArrayList<>();
            bucket.add(bid);
            leaf.values.add(index, bucket);
        }

        if (leaf.keys.size() < order) {
            return null;
        }
        return splitLeafNode(leaf);
    }

    private SplitResult splitLeafNode(LeafNode leaf) {
        int splitIndex = leaf.keys.size() / 2;

        LeafNode right = new LeafNode();
        right.keys.addAll(leaf.keys.subList(splitIndex, leaf.keys.size()));
        right.values.addAll(leaf.values.subList(splitIndex, leaf.values.size()));

        leaf.keys.subList(splitIndex, leaf.keys.size()).clear();
        leaf.values.subList(splitIndex, leaf.values.size()).clear();

        right.next = leaf.next;
        leaf.next = right;

        return new SplitResult(right.keys.get(0), right);
    }

    private SplitResult splitInternalNode(InternalNode node) {
        int midIndex = node.keys.size() / 2;
        double promotedKey = node.keys.get(midIndex);

        InternalNode right = new InternalNode();
        right.keys.addAll(node.keys.subList(midIndex + 1, node.keys.size()));
        right.children.addAll(node.children.subList(midIndex + 1, node.children.size()));

        node.keys.subList(midIndex, node.keys.size()).clear();
        node.children.subList(midIndex + 1, node.children.size()).clear();

        return new SplitResult(promotedKey, right);
    }

    private LeafNode findLeafNode(double key) {
        Node current = root;
        while (!current.isLeaf()) {
            InternalNode internal = (InternalNode) current;
            int childIndex = findChildIndex(internal.keys, key);
            current = internal.children.get(childIndex);
        }
        return (LeafNode) current;
    }

    private int findChildIndex(List<Double> keys, double key) {
        int index = 0;
        while (index < keys.size() && key >= keys.get(index)) {
            index++;
        }
        return index;
    }

    private int findKeyIndex(List<Double> keys, double key) {
        int low = 0;
        int high = keys.size();
        while (low < high) {
            int mid = (low + high) >>> 1;
            if (keys.get(mid) < key) {
                low = mid + 1;
            } else {
                high = mid;
            }
        }
        return low;
    }
}
