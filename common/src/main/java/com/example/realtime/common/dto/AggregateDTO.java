package com.example.realtime.common.dto;

import java.time.Instant;

/**
 * DTO for aggregated metrics to be shared between services.
 */
public class AggregateDTO {
    private String metricId;
    private Instant windowStart;
    private Instant windowEnd;
    private long count;
    private double sum;
    private double max;

    public AggregateDTO() {}

    // Getters and setters
    public String getMetricId() { return metricId; }
    public void setMetricId(String metricId) { this.metricId = metricId; }
    public Instant getWindowStart() { return windowStart; }
    public void setWindowStart(Instant windowStart) { this.windowStart = windowStart; }
    public Instant getWindowEnd() { return windowEnd; }
    public void setWindowEnd(Instant windowEnd) { this.windowEnd = windowEnd; }
    public long getCount() { return count; }
    public void setCount(long count) { this.count = count; }
    public double getSum() { return sum; }
    public void setSum(double sum) { this.sum = sum; }
    public double getMax() { return max; }
    public void setMax(double max) { this.max = max; }
}
