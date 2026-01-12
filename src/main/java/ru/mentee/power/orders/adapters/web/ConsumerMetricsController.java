/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.orders.adapters.web;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.mentee.power.orders.adapters.metrics.ConsumerMetricsRegistry;

@RestController
@RequestMapping("/api/v1/orders/consumers")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ConsumerMetricsController {

    private final ConsumerMetricsRegistry metricsRegistry;

    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getAllMetrics() {
        return ResponseEntity.ok(metricsRegistry.getAllMetrics());
    }

    @GetMapping("/metrics/summary")
    public ResponseEntity<Map<String, Object>> getMetricsSummary() {
        Map<String, Object> summary =
                Map.of(
                        "totalReceived", metricsRegistry.getTotalReceived(),
                        "totalProcessed", metricsRegistry.getTotalProcessed(),
                        "totalFailed", metricsRegistry.getTotalFailed(),
                        "retryAttempts", metricsRegistry.getRetryAttemptsCount(),
                        "externalFailures", metricsRegistry.getExternalFailuresCount(),
                        "dlqCount", metricsRegistry.getDlqCount(),
                        "averageProcessingTimeMs", metricsRegistry.getAverageProcessingTimeMs());
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/metrics/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        return ResponseEntity.ok(metricsRegistry.getStatistics());
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealth() {
        Map<String, Object> health =
                Map.of(
                        "status", metricsRegistry.isHealthy() ? "UP" : "DOWN",
                        "details", metricsRegistry.getHealthStatus(),
                        "timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(health);
    }

    @PostMapping("/metrics/reset")
    public ResponseEntity<Map<String, String>> resetMetrics() {
        metricsRegistry.resetAll();
        return ResponseEntity.ok(
                Map.of(
                        "status", "SUCCESS",
                        "message", "Все метрики сброшены"));
    }

    @GetMapping("/metrics/priority")
    public ResponseEntity<Map<String, Object>> getPriorityMetrics() {
        Map<String, Object> priorityMetrics =
                Map.of(
                        "received", metricsRegistry.getReceivedByPrioritySnapshot(),
                        "processed", metricsRegistry.getProcessedByPrioritySnapshot(),
                        "failed", metricsRegistry.getFailedByPrioritySnapshot());
        return ResponseEntity.ok(priorityMetrics);
    }

    @GetMapping("/metrics/region")
    public ResponseEntity<Map<String, Object>> getRegionMetrics() {
        Map<String, Object> regionMetrics =
                Map.of(
                        "received", metricsRegistry.getReceivedByRegionSnapshot(),
                        "processed", metricsRegistry.getProcessedByRegionSnapshot(),
                        "failed", metricsRegistry.getFailedByRegionSnapshot());
        return ResponseEntity.ok(regionMetrics);
    }
}
