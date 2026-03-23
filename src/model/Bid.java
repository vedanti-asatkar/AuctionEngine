package model;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class Bid {

    private static final DateTimeFormatter DISPLAY_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    private final String bidderId;
    private final double amount;
    private final long timestamp;

    public Bid(String bidderId, double amount) {
        if (bidderId == null || bidderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Bidder ID cannot be empty.");
        }
        if (!Double.isFinite(amount) || amount <= 0) {
            throw new IllegalArgumentException("Bid amount must be a positive finite number.");
        }

        this.bidderId = bidderId.trim();
        this.amount = amount;
        this.timestamp = System.currentTimeMillis();
    }

    public String getBidderId() {
        return bidderId;
    }

    public double getAmount() {
        return amount;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        String readableTime = DISPLAY_TIME_FORMATTER.format(Instant.ofEpochMilli(timestamp));
        return "Bidder: " + bidderId +
               ", Amount: " + amount +
               ", Time: " + readableTime +
               " (" + timestamp + " ms)";
    }
}
