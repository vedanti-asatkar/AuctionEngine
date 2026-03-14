package service;

import model.Auction;
import model.Bid;
import structures.BPlusTree;
import structures.IntervalTree;
import structures.MaxHeap;
import structures.PersistentBidTree;
import structures.RedBlackTree;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AuctionEngine {

    private static final int DEFAULT_RAPID_WINDOW_SECONDS = 20;
    private static final int DEFAULT_RAPID_THRESHOLD = 3;
    private static final double PRICE_SPIKE_MULTIPLIER = 1.7;
    private static final int MAX_RECENT_ALERTS = 50;
    private static final int DEFAULT_BPLUS_ORDER = 4;

    private static final class AuctionState {
        private final MaxHeap maxHeap = new MaxHeap();
        private final RedBlackTree redBlackTree = new RedBlackTree();
        private final PersistentBidTree persistentBidTree = new PersistentBidTree();
        private final BPlusTree bPlusTree = new BPlusTree(DEFAULT_BPLUS_ORDER);
        private final FraudDetector fraudDetector = new FraudDetector();
        private final List<String> recentAlerts = new ArrayList<>();
        private int totalBids;
        private boolean manuallyClosed;
    }

    private final Map<String, Auction> auctions;
    private final Map<String, AuctionState> auctionStates;
    private final IntervalTree auctionSchedule;

    public AuctionEngine() {
        auctions = new LinkedHashMap<>();
        auctionStates = new LinkedHashMap<>();
        auctionSchedule = new IntervalTree();
    }

    public void createAuction(String auctionId, String itemName, long start, long end) {
        if (auctions.containsKey(auctionId)) {
            throw new IllegalArgumentException("Auction ID already exists: " + auctionId);
        }

        Auction auction = new Auction(auctionId, itemName, start, end);
        auctions.put(auctionId, auction);
        auctionStates.put(auctionId, new AuctionState());
        auctionSchedule.insert(auction);
    }

    public List<Auction> getActiveAuctions() {
        long now = System.currentTimeMillis();
        List<Auction> activeFromSchedule = auctionSchedule.searchActive(now);
        List<Auction> visibleActiveAuctions = new ArrayList<>();

        for (Auction auction : activeFromSchedule) {
            AuctionState state = auctionStates.get(auction.getAuctionId());
            if (state != null && !state.manuallyClosed) {
                visibleActiveAuctions.add(auction);
            }
        }

        return visibleActiveAuctions;
    }

    public boolean isAuctionActive(String auctionId) {
        Auction auction = auctions.get(auctionId);
        AuctionState state = auctionStates.get(auctionId);
        return auction != null
                && state != null
                && !state.manuallyClosed
                && auction.isActiveAt(System.currentTimeMillis());
    }

    public Auction getAuction(String auctionId) {
        return auctions.get(auctionId);
    }

    public List<Auction> getAllAuctions() {
        return new ArrayList<>(auctions.values());
    }

    public void placeBid(String auctionId, String bidderId, double amount) {
        Auction auction = requireAuction(auctionId);
        AuctionState state = requireAuctionState(auctionId);

        if (!isAuctionActive(auctionId)) {
            System.out.println("Auction is closed. No more bids are accepted.");
            return;
        }

        if (amount <= 0) {
            System.out.println("Bid amount must be greater than 0.");
            return;
        }

        Bid highest = state.maxHeap.getMax();
        if (highest != null && amount <= highest.getAmount()) {
            System.out.println("Bid must be higher than current highest bid.");
            return;
        }

        boolean priceSpike = isPriceSpike(auctionId, amount);

        Bid bid = new Bid(bidderId, amount);
        state.maxHeap.insert(bid);
        state.redBlackTree.insert(bid);
        state.persistentBidTree.addVersion(bid);
        state.bPlusTree.insert(amount, bid);

        state.fraudDetector.recordBid(bidderId, bid.getTimestamp());
        state.totalBids++;

        boolean rapidBidding = isRapidBidding(auctionId, bidderId, DEFAULT_RAPID_WINDOW_SECONDS, DEFAULT_RAPID_THRESHOLD);

        System.out.println("Bid placed successfully.");

        if (rapidBidding || priceSpike) {
            System.out.println("WARNING: Unusual bidding activity detected.");
            if (rapidBidding) {
                String message = "Auction " + auction.getAuctionId() + ": Rapid bidding by bidder: " + bidderId + " at amount " + amount;
                System.out.println("WARNING: Unusual bidding activity detected (" + message + ").");
                addAlert(state, message);
            }
            if (priceSpike) {
                String message = "Auction " + auction.getAuctionId() + ": Price spike at amount " + amount;
                System.out.println("WARNING: Unusual bidding activity detected (" + message + ").");
                addAlert(state, message);
            }
        }
    }

    public Bid getHighestBid(String auctionId) {
        return requireAuctionState(auctionId).maxHeap.getMax();
    }

    public boolean isRapidBidding(String auctionId, String bidderId, int windowSeconds, int threshold) {
        return requireAuctionState(auctionId).fraudDetector.isRapidBidding(bidderId, windowSeconds, threshold);
    }

    public boolean isPriceSpike(String auctionId, double newBidAmount) {
        Bid currentHighest = getHighestBid(auctionId);
        if (currentHighest == null) {
            return false;
        }
        return newBidAmount > (currentHighest.getAmount() * PRICE_SPIKE_MULTIPLIER);
    }

    public int getTotalBids(String auctionId) {
        return requireAuctionState(auctionId).totalBids;
    }

    public boolean isAuctionOpen(String auctionId) {
        return isAuctionActive(auctionId);
    }

    public void closeAuction(String auctionId) {
        requireAuctionState(auctionId).manuallyClosed = true;
    }

    public void printSortedBids(String auctionId) {
        requireAuctionState(auctionId).redBlackTree.inorderTraversal();
    }

    public int getHistoryVersionCount(String auctionId) {
        return requireAuctionState(auctionId).persistentBidTree.getVersionCount();
    }

    public Bid getHighestBidAtVersion(String auctionId, int version) {
        return requireAuctionState(auctionId).persistentBidTree.getHighestInVersion(version);
    }

    public List<Bid> getBidsAtVersion(String auctionId, int version) {
        return requireAuctionState(auctionId).persistentBidTree.getBidsInVersion(version);
    }

    public boolean isValidHistoryVersion(String auctionId, int version) {
        return requireAuctionState(auctionId).persistentBidTree.isValidVersion(version);
    }

    public List<Bid> getBidsByAmount(String auctionId, double amount) {
        return requireAuctionState(auctionId).bPlusTree.search(amount);
    }

    public List<Bid> getBidsInAmountRange(String auctionId, double minAmount, double maxAmount) {
        return requireAuctionState(auctionId).bPlusTree.rangeSearch(minAmount, maxAmount);
    }

    public List<String> getRecentAlerts(String auctionId) {
        return Collections.unmodifiableList(requireAuctionState(auctionId).recentAlerts);
    }

    private AuctionState requireAuctionState(String auctionId) {
        AuctionState state = auctionStates.get(auctionId);
        if (state == null) {
            throw new IllegalArgumentException("Unknown auction ID: " + auctionId);
        }
        return state;
    }

    private Auction requireAuction(String auctionId) {
        Auction auction = auctions.get(auctionId);
        if (auction == null) {
            throw new IllegalArgumentException("Unknown auction ID: " + auctionId);
        }
        return auction;
    }

    private void addAlert(AuctionState state, String message) {
        if (state.recentAlerts.size() == MAX_RECENT_ALERTS) {
            state.recentAlerts.remove(0);
        }
        state.recentAlerts.add(message);
    }
}
