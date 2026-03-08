package com.purbarun.employee.dto.cache;

/**
 * Cache statistics holder class.
 * Tracks hit and miss counts for L1 and L2 cache levels.
 */
public class CacheStatistics {
    private final long l1Hits;
    private final long l1Misses;
    private final long l2Hits;
    private final long l2Misses;
    
    public CacheStatistics(long l1Hits, long l1Misses, long l2Hits, long l2Misses) {
        this.l1Hits = l1Hits;
        this.l1Misses = l1Misses;
        this.l2Hits = l2Hits;
        this.l2Misses = l2Misses;
    }
    
    public long getL1Hits() { return l1Hits; }
    public long getL1Misses() { return l1Misses; }
    public long getL2Hits() { return l2Hits; }
    public long getL2Misses() { return l2Misses; }
    
    public double getL1HitRate() {
        long total = l1Hits + l1Misses;
        return total == 0 ? 0.0 : (double) l1Hits / total;
    }
    
    public double getL2HitRate() {
        long total = l2Hits + l2Misses;
        return total == 0 ? 0.0 : (double) l2Hits / total;
    }
    
    @Override
    public String toString() {
        return String.format(
            "L1: %d hits, %d misses (%.2f%% hit rate) | L2: %d hits, %d misses (%.2f%% hit rate)",
            l1Hits, l1Misses, getL1HitRate() * 100, l2Hits, l2Misses, getL2HitRate() * 100
        );
    }
}
