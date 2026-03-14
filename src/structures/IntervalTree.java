package structures;

import model.Auction;

import java.util.ArrayList;
import java.util.List;

public class IntervalTree {

    private static final class Node {
        private final Auction auction;
        private long maxEnd;
        private Node left;
        private Node right;

        private Node(Auction auction) {
            this.auction = auction;
            this.maxEnd = auction.getEndTimeMillis();
        }
    }

    private Node root;

    public void insert(Auction auction) {
        if (auction == null) {
            return;
        }
        root = insert(root, auction);
    }

    public List<Auction> searchActive(long currentTime) {
        List<Auction> activeAuctions = new ArrayList<>();
        searchActive(root, currentTime, activeAuctions);
        return activeAuctions;
    }

    private Node insert(Node node, Auction auction) {
        if (node == null) {
            return new Node(auction);
        }

        if (auction.getStartTimeMillis() < node.auction.getStartTimeMillis()) {
            node.left = insert(node.left, auction);
        } else {
            node.right = insert(node.right, auction);
        }

        long leftMax = node.left == null ? Long.MIN_VALUE : node.left.maxEnd;
        long rightMax = node.right == null ? Long.MIN_VALUE : node.right.maxEnd;
        node.maxEnd = Math.max(node.auction.getEndTimeMillis(), Math.max(leftMax, rightMax));
        return node;
    }

    private void searchActive(Node node, long currentTime, List<Auction> result) {
        if (node == null) {
            return;
        }

        if (node.left != null && node.left.maxEnd >= currentTime) {
            searchActive(node.left, currentTime, result);
        }

        if (node.auction.isActiveAt(currentTime)) {
            result.add(node.auction);
        }

        // Right subtree can only contain active intervals if some start time is <= current time.
        if (node.right != null && node.auction.getStartTimeMillis() <= currentTime) {
            searchActive(node.right, currentTime, result);
        }
    }
}
