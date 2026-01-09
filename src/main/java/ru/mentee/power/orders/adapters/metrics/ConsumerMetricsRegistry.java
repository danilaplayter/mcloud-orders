/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.orders.adapters.metrics;

import lombok.Getter;
import org.springframework.stereotype.Component;
import ru.mentee.power.orders.domain.model.Order.OrderPriority;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Getter
public class ConsumerMetricsRegistry {

    private final Map<OrderPriority, AtomicLong> receivedByPriority = new EnumMap<>(OrderPriority.class);
    private final Map<OrderPriority, AtomicLong> processedByPriority = new EnumMap<>(OrderPriority.class);
    private final Map<OrderPriority, AtomicLong> failedByPriority = new EnumMap<>(OrderPriority.class);
    private final Map<String, AtomicLong> receivedByRegion = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> processedByRegion = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> failedByRegion = new ConcurrentHashMap<>();

    //лучше через Long или AtomicLong?
    private final AtomicLong retryAttempts = new AtomicLong(0);
    private final AtomicLong externalFailures = new AtomicLong(0);
    private final AtomicLong dlqCount = new AtomicLong(0);

    public ConsumerMetricsRegistry() {
        for (OrderPriority priority : OrderPriority.values()) {
            receivedByPriority.put(priority, new AtomicLong(0));
            processedByPriority.put(priority, new AtomicLong(0));
            failedByPriority.put(priority, new AtomicLong(0));
        }
    }

    public void incrementReceived(OrderPriority priority, String region) {
        receivedByPriority.get(priority).incrementAndGet();
        receivedByRegion.computeIfAbsent(region, k -> new AtomicLong(0)).incrementAndGet();
    }

    public void incrementProcessed(OrderPriority priority, String region) {
        processedByPriority.get(priority).incrementAndGet();
        processedByRegion.computeIfAbsent(region, k -> new AtomicLong(0)).incrementAndGet();
    }

    public void incrementFailed(OrderPriority priority, String region) {
        failedByPriority.get(priority).incrementAndGet();
        failedByRegion.computeIfAbsent(region, k -> new AtomicLong(0)).incrementAndGet();
    }

    public void incrementRetryAttempts() {
        retryAttempts.incrementAndGet();
    }

    public void incrementRetryAttempts(long count) {
        retryAttempts.addAndGet(count);
    }

    public void incrementExternalFailures() {
        externalFailures.incrementAndGet();
    }

    public void incrementExternalFailures(long count) {
        externalFailures.addAndGet(count);
    }

    public void incrementDlqCount() {
        dlqCount.incrementAndGet();
    }

    public void incrementDlqCount(long count) {
        dlqCount.addAndGet(count);
    }

    public Map<OrderPriority, Long> getReceivedByPrioritySnapshot() {
        Map<OrderPriority, Long> result = new EnumMap<>(OrderPriority.class);
        receivedByPriority.forEach((k, v) -> result.put(k, v.get()));
        return result;
    }

    public Map<OrderPriority, Long> getProcessedByPrioritySnapshot() {
        Map<OrderPriority, Long> result = new EnumMap<>(OrderPriority.class);
        processedByPriority.forEach((k, v) -> result.put(k, v.get()));
        return result;
    }

    public Map<OrderPriority, Long> getFailedByPrioritySnapshot() {
        Map<OrderPriority, Long> result = new EnumMap<>(OrderPriority.class);
        failedByPriority.forEach((k, v) -> result.put(k, v.get()));
        return result;
    }

    public Map<String, Long> getReceivedByRegionSnapshot() {
        Map<String, Long> result = new ConcurrentHashMap<>();
        receivedByRegion.forEach((k, v) -> result.put(k, v.get()));
        return result;
    }

    public Map<String, Long> getProcessedByRegionSnapshot() {
        Map<String, Long> result = new ConcurrentHashMap<>();
        processedByRegion.forEach((k, v) -> result.put(k, v.get()));
        return result;
    }

    public Map<String, Long> getFailedByRegionSnapshot() {
        Map<String, Long> result = new ConcurrentHashMap<>();
        failedByRegion.forEach((k, v) -> result.put(k, v.get()));
        return result;
    }

    public long getTotalReceived() {
        return receivedByPriority.values().stream().mapToLong(AtomicLong::get).sum();
    }

    public long getTotalProcessed() {
        return processedByPriority.values().stream().mapToLong(AtomicLong::get).sum();
    }

    public long getTotalFailed() {
        return failedByPriority.values().stream().mapToLong(AtomicLong::get).sum();
    }

    public Map<String, Object> getAllMetrics() {
        Map<String, Object> metrics = new ConcurrentHashMap<>();

        metrics.put("receivedByPriority", getReceivedByPrioritySnapshot());
        metrics.put("processedByPriority", getProcessedByPrioritySnapshot());
        metrics.put("failedByPriority", getFailedByPrioritySnapshot());
        metrics.put("receivedByRegion", getReceivedByRegionSnapshot());
        metrics.put("processedByRegion", getProcessedByRegionSnapshot());
        metrics.put("failedByRegion", getFailedByRegionSnapshot());
        metrics.put("totalReceived", getTotalReceived());
        metrics.put("totalProcessed", getTotalProcessed());
        metrics.put("totalFailed", getTotalFailed());

        metrics.put("retryAttempts", retryAttempts.get());
        metrics.put("externalFailures", externalFailures.get());
        metrics.put("dlqCount", dlqCount.get());

        return metrics;
    }
}