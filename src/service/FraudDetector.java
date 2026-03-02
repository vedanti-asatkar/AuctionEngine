package service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FraudDetector {

    private final Map<String, List<Long>> bidderTimestamps;

    public FraudDetector() {
        this.bidderTimestamps = new HashMap<>();
    }

    public void recordBid(String bidderId, long timestampMillis) {
        bidderTimestamps.computeIfAbsent(bidderId, id -> new ArrayList<>()).add(timestampMillis);
    }

    public boolean isRapidBidding(String bidderId, int windowSeconds, int threshold) {
        List<Long> timestamps = bidderTimestamps.get(bidderId);
        if (timestamps == null || timestamps.isEmpty()) {
            return false;
        }

        long nowMillis = System.currentTimeMillis();
        long cutoff = nowMillis - (windowSeconds * 1000L);
        int firstValidIndex = 0;

        while (firstValidIndex < timestamps.size() && timestamps.get(firstValidIndex) < cutoff) {
            firstValidIndex++;
        }

        if (firstValidIndex > 0) {
            timestamps.subList(0, firstValidIndex).clear();
        }

        return timestamps.size() >= threshold;
    }
}
