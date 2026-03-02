package service;

import model.Bid;
import structures.MaxHeap;
import structures.PersistentBidTree;
import structures.RedBlackTree;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AuctionEngine {

    private static final int DEFAULT_RAPID_WINDOW_SECONDS = 20;
    private static final int DEFAULT_RAPID_THRESHOLD = 3;
    private static final double PRICE_SPIKE_MULTIPLIER = 1.7;
    private static final int MAX_RECENT_ALERTS = 50;

    private MaxHeap maxHeap;
    private RedBlackTree redBlackTree;
    private PersistentBidTree persistentBidTree;
    private FraudDetector fraudDetector;
    private final List<String> recentAlerts;
    private int totalBids;
    private boolean auctionOpen;

    public AuctionEngine() {
        maxHeap = new MaxHeap();
        redBlackTree = new RedBlackTree();
        persistentBidTree = new PersistentBidTree();
        fraudDetector = new FraudDetector();
        recentAlerts = new ArrayList<>();
        totalBids = 0;
        auctionOpen = true;
    }

    public void placeBid(String bidderId, double amount) {
        if (!auctionOpen) {
            System.out.println("Auction is closed. No more bids are accepted.");
            return;
        }

        if (amount <= 0) {
            System.out.println("Bid amount must be greater than 0.");
            return;
        }

        Bid highest = maxHeap.getMax();
        if (highest != null && amount <= highest.getAmount()) {
            System.out.println("Bid must be higher than current highest bid.");
            return;
        }

        boolean priceSpike = isPriceSpike(amount);

        Bid bid = new Bid(bidderId, amount);
        maxHeap.insert(bid);
        redBlackTree.insert(bid);
        persistentBidTree.addVersion(bid);

        fraudDetector.recordBid(bidderId, bid.getTimestamp());
        totalBids++;

        boolean rapidBidding = isRapidBidding(bidderId, DEFAULT_RAPID_WINDOW_SECONDS, DEFAULT_RAPID_THRESHOLD);

        System.out.println("Bid placed successfully.");

        if (rapidBidding || priceSpike) {
            System.out.println("WARNING: Unusual bidding activity detected.");
            if (rapidBidding) {
                String message = "Rapid bidding by bidder: " + bidderId + " at amount " + amount;
                System.out.println("WARNING: Unusual bidding activity detected (" + message + ").");
                addAlert(message);
            }
            if (priceSpike) {
                String message = "Price spike at amount " + amount;
                System.out.println("WARNING: Unusual bidding activity detected (" + message + ").");
                addAlert(message);
            }
        }
    }

    public Bid getHighestBid() {
        return maxHeap.getMax();
    }

    public boolean isRapidBidding(String bidderId, int windowSeconds, int threshold) {
        return fraudDetector.isRapidBidding(bidderId, windowSeconds, threshold);
    }

    public boolean isPriceSpike(double newBidAmount) {
        Bid currentHighest = getHighestBid();
        if (currentHighest == null) {
            return false;
        }
        return newBidAmount > (currentHighest.getAmount() * PRICE_SPIKE_MULTIPLIER);
    }

    public int getTotalBids() {
        return totalBids;
    }

    public boolean isAuctionOpen() {
        return auctionOpen;
    }

    public void closeAuction() {
        auctionOpen = false;
    }

    public void printSortedBids() {
        redBlackTree.inorderTraversal();
    }

    public int getHistoryVersionCount() {
        return persistentBidTree.getVersionCount();
    }

    public Bid getHighestBidAtVersion(int version) {
        return persistentBidTree.getHighestInVersion(version);
    }

    public List<Bid> getBidsAtVersion(int version) {
        return persistentBidTree.getBidsInVersion(version);
    }

    public boolean isValidHistoryVersion(int version) {
        return persistentBidTree.isValidVersion(version);
    }

    public List<String> getRecentAlerts() {
        return Collections.unmodifiableList(recentAlerts);
    }

    private void addAlert(String message) {
        if (recentAlerts.size() == MAX_RECENT_ALERTS) {
            recentAlerts.remove(0);
        }
        recentAlerts.add(message);
    }
}
