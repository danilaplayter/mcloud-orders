/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.orders.adapters.metrics;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;
import ru.mentee.power.orders.domain.model.Order.OrderPriority;

@Component
public class ConsumerMetricsRegistry {

    private final Map<OrderPriority, AtomicLong> receivedByPriority =
            new EnumMap<>(OrderPriority.class);
    private final Map<OrderPriority, AtomicLong> processedByPriority =
            new EnumMap<>(OrderPriority.class);
    private final Map<OrderPriority, AtomicLong> failedByPriority =
            new EnumMap<>(OrderPriority.class);
    private final Map<String, AtomicLong> receivedByRegion = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> processedByRegion = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> failedByRegion = new ConcurrentHashMap<>();

    public ConsumerMetricsRegistry() {
        // Инициализируем счетчики для всех приоритетов
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

    // Геттеры для метрик
    public Map<OrderPriority, Long> getReceivedByPriority() {
        Map<OrderPriority, Long> result = new EnumMap<>(OrderPriority.class);
        receivedByPriority.forEach((k, v) -> result.put(k, v.get()));
        return result;
    }

    public Map<OrderPriority, Long> getProcessedByPriority() {
        Map<OrderPriority, Long> result = new EnumMap<>(OrderPriority.class);
        processedByPriority.forEach((k, v) -> result.put(k, v.get()));
        return result;
    }

    public Map<OrderPriority, Long> getFailedByPriority() {
        Map<OrderPriority, Long> result = new EnumMap<>(OrderPriority.class);
        failedByPriority.forEach((k, v) -> result.put(k, v.get()));
        return result;
    }

    public Map<String, Long> getReceivedByRegion() {
        Map<String, Long> result = new ConcurrentHashMap<>();
        receivedByRegion.forEach((k, v) -> result.put(k, v.get()));
        return result;
    }

    public Map<String, Long> getProcessedByRegion() {
        Map<String, Long> result = new ConcurrentHashMap<>();
        processedByRegion.forEach((k, v) -> result.put(k, v.get()));
        return result;
    }

    public Map<String, Long> getFailedByRegion() {
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
}
