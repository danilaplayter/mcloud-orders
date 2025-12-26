/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.orders.adapters.metrics;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class ProducerMetricsRegistry {

    private final Map<String, AtomicLong> successCounters = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> failureCounters = new ConcurrentHashMap<>();

    public void incrementSuccess(String topic) {
        successCounters.computeIfAbsent(topic, k -> new AtomicLong()).incrementAndGet();
    }

    public void incrementFailure(String topic) {
        failureCounters.computeIfAbsent(topic, k -> new AtomicLong()).incrementAndGet();
    }
}
