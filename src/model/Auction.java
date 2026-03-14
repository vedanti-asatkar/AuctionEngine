package model;

public class Auction {

    private final String auctionId;
    private final String itemName;
    private final long startTimeMillis;
    private final long endTimeMillis;

    public Auction(String auctionId, String itemName, long startTimeMillis, long endTimeMillis) {
        if (auctionId == null || auctionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Auction ID cannot be empty.");
        }
        if (itemName == null || itemName.trim().isEmpty()) {
            throw new IllegalArgumentException("Item name cannot be empty.");
        }
        if (endTimeMillis < startTimeMillis) {
            throw new IllegalArgumentException("Auction end time must be greater than or equal to start time.");
        }

        this.auctionId = auctionId.trim();
        this.itemName = itemName.trim();
        this.startTimeMillis = startTimeMillis;
        this.endTimeMillis = endTimeMillis;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public String getItemName() {
        return itemName;
    }

    public long getStartTimeMillis() {
        return startTimeMillis;
    }

    public long getEndTimeMillis() {
        return endTimeMillis;
    }

    public boolean isActiveAt(long timestampMillis) {
        return startTimeMillis <= timestampMillis && timestampMillis <= endTimeMillis;
    }

    @Override
    public String toString() {
        return auctionId + " - " + itemName +
                " [" + startTimeMillis + " to " + endTimeMillis + "]";
    }
}
