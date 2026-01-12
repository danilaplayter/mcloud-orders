/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.orders.adapters.metrics;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import lombok.Getter;
import org.springframework.stereotype.Component;
import ru.mentee.power.orders.domain.model.Order.OrderPriority;

@Component
@Getter
public class ConsumerMetricsRegistry {

    private final Map<OrderPriority, AtomicLong> receivedByPriority =
            new EnumMap<>(OrderPriority.class);
    private final Map<OrderPriority, AtomicLong> processedByPriority =
            new EnumMap<>(OrderPriority.class);
    private final Map<OrderPriority, AtomicLong> failedByPriority =
            new EnumMap<>(OrderPriority.class);

    private final ConcurrentHashMap<String, AtomicLong> receivedByRegion =
            new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> processedByRegion =
            new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> failedByRegion = new ConcurrentHashMap<>();

    private final AtomicLong retryAttempts = new AtomicLong(0);
    private final AtomicLong externalFailures = new AtomicLong(0);
    private final AtomicLong dlqCount = new AtomicLong(0);

    private final AtomicLong totalProcessingTimeMs = new AtomicLong(0);
    private final AtomicLong processingCount = new AtomicLong(0);

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

    public void recordProcessingTime(long processingTimeMs) {
        totalProcessingTimeMs.addAndGet(processingTimeMs);
        processingCount.incrementAndGet();
    }

    public double getAverageProcessingTimeMs() {
        long count = processingCount.get();
        if (count == 0) {
            return 0.0;
        }
        return (double) totalProcessingTimeMs.get() / count;
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

    public long getRetryAttemptsCount() {
        return retryAttempts.get();
    }

    public long getExternalFailuresCount() {
        return externalFailures.get();
    }

    public long getDlqCount() {
        return dlqCount.get();
    }

    public Map<OrderPriority, Long> getReceivedByPrioritySnapshot() {
        Map<OrderPriority, Long> snapshot = new EnumMap<>(OrderPriority.class);
        receivedByPriority.forEach((priority, counter) -> snapshot.put(priority, counter.get()));
        return snapshot;
    }

    public Map<OrderPriority, Long> getProcessedByPrioritySnapshot() {
        Map<OrderPriority, Long> snapshot = new EnumMap<>(OrderPriority.class);
        processedByPriority.forEach((priority, counter) -> snapshot.put(priority, counter.get()));
        return snapshot;
    }

    public Map<OrderPriority, Long> getFailedByPrioritySnapshot() {
        Map<OrderPriority, Long> snapshot = new EnumMap<>(OrderPriority.class);
        failedByPriority.forEach((priority, counter) -> snapshot.put(priority, counter.get()));
        return snapshot;
    }

    public Map<String, Long> getReceivedByRegionSnapshot() {
        Map<String, Long> snapshot = new ConcurrentHashMap<>();
        receivedByRegion.forEach((region, counter) -> snapshot.put(region, counter.get()));
        return snapshot;
    }

    public Map<String, Long> getProcessedByRegionSnapshot() {
        Map<String, Long> snapshot = new ConcurrentHashMap<>();
        processedByRegion.forEach((region, counter) -> snapshot.put(region, counter.get()));
        return snapshot;
    }

    public Map<String, Long> getFailedByRegionSnapshot() {
        Map<String, Long> snapshot = new ConcurrentHashMap<>();
        failedByRegion.forEach((region, counter) -> snapshot.put(region, counter.get()));
        return snapshot;
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

        metrics.put("averageProcessingTimeMs", getAverageProcessingTimeMs());
        metrics.put("totalProcessingTimeMs", totalProcessingTimeMs.get());
        metrics.put("processingCount", processingCount.get());

        metrics.put("successRate", calculateSuccessRate());
        metrics.put("failureRate", calculateFailureRate());

        return metrics;
    }

    public double calculateSuccessRate() {
        long total = getTotalReceived();
        if (total == 0) {
            return 0.0;
        }
        long success = getTotalProcessed();
        return (double) success / total * 100;
    }

    public double calculateFailureRate() {
        long total = getTotalReceived();
        if (total == 0) {
            return 0.0;
        }
        long failures = getTotalFailed();
        return (double) failures / total * 100;
    }

    public double calculateRetryRate() {
        long processed = getTotalProcessed();
        if (processed == 0) {
            return 0.0;
        }
        return (double) retryAttempts.get() / processed * 100;
    }

    public void resetAll() {
        receivedByPriority.values().forEach(counter -> counter.set(0));
        processedByPriority.values().forEach(counter -> counter.set(0));
        failedByPriority.values().forEach(counter -> counter.set(0));

        receivedByRegion.clear();
        processedByRegion.clear();
        failedByRegion.clear();

        retryAttempts.set(0);
        externalFailures.set(0);
        dlqCount.set(0);
        totalProcessingTimeMs.set(0);
        processingCount.set(0);
    }

    public void resetCounters() {
        retryAttempts.set(0);
        externalFailures.set(0);
        dlqCount.set(0);
    }

    public boolean isHealthy() {
        long totalReceived = getTotalReceived();
        long totalProcessed = getTotalProcessed();

        if (totalReceived > 100 && totalProcessed < totalReceived * 0.5) {
            return false;
        }

        if (externalFailures.get() > 100) {
            return false;
        }

        return true;
    }

    public String getHealthStatus() {
        if (isHealthy()) {
            return "HEALTHY";
        } else {
            return "UNHEALTHY - проверьте логи и метрики";
        }
    }

    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new ConcurrentHashMap<>();

        stats.put("successRatePercent", String.format("%.2f%%", calculateSuccessRate()));
        stats.put("failureRatePercent", String.format("%.2f%%", calculateFailureRate()));
        stats.put("retryRatePercent", String.format("%.2f%%", calculateRetryRate()));
        stats.put(
                "averageProcessingTimeMs", String.format("%.2f ms", getAverageProcessingTimeMs()));
        stats.put("healthStatus", getHealthStatus());
        stats.put("throughput", calculateThroughput());

        return stats;
    }

    private String calculateThroughput() {
        long total = getTotalProcessed();
        long time = totalProcessingTimeMs.get();

        if (time == 0 || total == 0) {
            return "0 orders/sec";
        }

        double throughput = (double) total / (time / 1000.0);
        return String.format("%.2f orders/sec", throughput);
    }
}
