package structures;

import model.Bid;
import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

public class PersistentBidTree {

    private static final class Node {
        private final Bid bid;
        private final int priority;
        private final Node left;
        private final Node right;

        private Node(Bid bid, int priority, Node left, Node right) {
            this.bid = bid;
            this.priority = priority;
            this.left = left;
            this.right = right;
        }
    }

    private final List<Node> versionRoots;
    private final SplittableRandom random;

    public PersistentBidTree() {
        this.versionRoots = new ArrayList<>();
        this.versionRoots.add(null); // Version 0: empty snapshot
        this.random = new SplittableRandom();
    }

    public void addVersion(Bid bid) {
        Node previousRoot = versionRoots.get(versionRoots.size() - 1);
        Node nextRoot = insert(previousRoot, bid);
        versionRoots.add(nextRoot);
    }

    public int getVersionCount() {
        return versionRoots.size() - 1;
    }

    public boolean isValidVersion(int version) {
        return version >= 0 && version < versionRoots.size();
    }

    public List<Bid> getBidsInVersion(int version) {
        if (!isValidVersion(version)) {
            return new ArrayList<>();
        }

        List<Bid> bids = new ArrayList<>();
        inorder(versionRoots.get(version), bids);
        return bids;
    }

    public Bid getHighestInVersion(int version) {
        if (!isValidVersion(version)) {
            return null;
        }

        Node current = versionRoots.get(version);
        while (current != null && current.right != null) {
            current = current.right;
        }
        return current == null ? null : current.bid;
    }

    private Node insert(Node root, Bid bid) {
        if (root == null) {
            return new Node(bid, random.nextInt(), null, null);
        }

        if (compareBids(bid, root.bid) < 0) {
            Node newLeft = insert(root.left, bid);
            Node rebuilt = new Node(root.bid, root.priority, newLeft, root.right);
            if (newLeft.priority > rebuilt.priority) {
                return rotateRight(rebuilt);
            }
            return rebuilt;
        }

        Node newRight = insert(root.right, bid);
        Node rebuilt = new Node(root.bid, root.priority, root.left, newRight);
        if (newRight.priority > rebuilt.priority) {
            return rotateLeft(rebuilt);
        }
        return rebuilt;
    }

    private Node rotateLeft(Node node) {
        Node rightChild = node.right;
        Node movedSubtree = rightChild.left;
        Node newLeft = new Node(node.bid, node.priority, node.left, movedSubtree);
        return new Node(rightChild.bid, rightChild.priority, newLeft, rightChild.right);
    }

    private Node rotateRight(Node node) {
        Node leftChild = node.left;
        Node movedSubtree = leftChild.right;
        Node newRight = new Node(node.bid, node.priority, movedSubtree, node.right);
        return new Node(leftChild.bid, leftChild.priority, leftChild.left, newRight);
    }

    private void inorder(Node node, List<Bid> bids) {
        if (node == null) {
            return;
        }
        inorder(node.left, bids);
        bids.add(node.bid);
        inorder(node.right, bids);
    }

    private int compareBids(Bid a, Bid b) {
        int amountCmp = Double.compare(a.getAmount(), b.getAmount());
        if (amountCmp != 0) {
            return amountCmp;
        }

        int timeCmp = Long.compare(a.getTimestamp(), b.getTimestamp());
        if (timeCmp != 0) {
            return timeCmp;
        }

        return a.getBidderId().compareTo(b.getBidderId());
    }
}
