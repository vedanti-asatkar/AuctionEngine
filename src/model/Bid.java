package model;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class Bid {

    private static final DateTimeFormatter DISPLAY_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    private String bidderId;
    private double amount;
    private long timestamp;

    public Bid(String bidderId, double amount) {
        this.bidderId = bidderId;
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
