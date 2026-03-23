package structures;

import model.Bid;
import java.util.ArrayList;
import java.util.List;

public class RedBlackTree {

    private static final boolean RED = true;
    private static final boolean BLACK = false;

    private class Node {
        Bid bid;
        Node left, right, parent;
        boolean color;

        Node(Bid bid) {
            this.bid = bid;
            this.color = RED;
        }
    }

    private Node root;

    public void insert(Bid bid) {
        Node newNode = new Node(bid);
        Node parent = null;
        Node current = root;

        // Standard BST insert by bid amount (ascending)
        while (current != null) {
            parent = current;
            if (newNode.bid.getAmount() < current.bid.getAmount()) {
                current = current.left;
            } else {
                current = current.right;
            }
        }

        newNode.parent = parent;

        if (parent == null) {
            root = newNode;
        } else if (newNode.bid.getAmount() < parent.bid.getAmount()) {
            parent.left = newNode;
        } else {
            parent.right = newNode;
        }

        fixInsert(newNode);
    }

    private void leftRotate(Node x) {
        Node y = x.right;
        x.right = y.left;

        if (y.left != null) {
            y.left.parent = x;
        }

        y.parent = x.parent;

        if (x.parent == null) {
            root = y;
        } else if (x == x.parent.left) {
            x.parent.left = y;
        } else {
            x.parent.right = y;
        }

        y.left = x;
        x.parent = y;
    }

    private void rightRotate(Node y) {
        Node x = y.left;
        y.left = x.right;

        if (x.right != null) {
            x.right.parent = y;
        }

        x.parent = y.parent;

        if (y.parent == null) {
            root = x;
        } else if (y == y.parent.right) {
            y.parent.right = x;
        } else {
            y.parent.left = x;
        }

        x.right = y;
        y.parent = x;
    }

    private void fixInsert(Node node) {
        while (node != root && node.parent.color == RED) {
            Node parent = node.parent;
            Node grandparent = parent.parent;

            if (parent == grandparent.left) {
                Node uncle = grandparent.right;

                // Case 1: Uncle is RED -> recolor and move up
                if (uncle != null && uncle.color == RED) {
                    parent.color = BLACK;
                    uncle.color = BLACK;
                    grandparent.color = RED;
                    node = grandparent;
                } else {
                    // Case 2: Node is right child -> rotate left at parent
                    if (node == parent.right) {
                        node = parent;
                        leftRotate(node);
                        parent = node.parent;
                        grandparent = parent.parent;
                    }

                    // Case 3: Node is left child -> rotate right at grandparent
                    parent.color = BLACK;
                    grandparent.color = RED;
                    rightRotate(grandparent);
                }
            } else {
                Node uncle = grandparent.left;

                // Mirror Case 1
                if (uncle != null && uncle.color == RED) {
                    parent.color = BLACK;
                    uncle.color = BLACK;
                    grandparent.color = RED;
                    node = grandparent;
                } else {
                    // Mirror Case 2
                    if (node == parent.left) {
                        node = parent;
                        rightRotate(node);
                        parent = node.parent;
                        grandparent = parent.parent;
                    }

                    // Mirror Case 3
                    parent.color = BLACK;
                    grandparent.color = RED;
                    leftRotate(grandparent);
                }
            }
        }

        root.color = BLACK;
    }

    public List<Bid> inorderTraversal() {
        List<Bid> bids = new ArrayList<>();
        inorderHelper(root, bids);
        return bids;
    }

    private void inorderHelper(Node node, List<Bid> bids) {
        if (node != null) {
            inorderHelper(node.left, bids);
            bids.add(node.bid);
            inorderHelper(node.right, bids);
        }
    }
}
