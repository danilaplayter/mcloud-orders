/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.orders.adapters.web;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mentee.power.orders.adapters.metrics.ConsumerMetricsRegistry;

@RestController
@RequestMapping("/api/v1/orders/consumers")
@RequiredArgsConstructor
public class ConsumerMetricsController {

    private final ConsumerMetricsRegistry metricsRegistry;

    // тут тоже в маппер вынести
    @GetMapping("/metrics")
    public ResponseEntity<ConsumerMetricsResponse> getMetrics() {
        ConsumerMetricsResponse response =
                new ConsumerMetricsResponse(
                        metricsRegistry.getReceivedByPriority(),
                        metricsRegistry.getProcessedByPriority(),
                        metricsRegistry.getFailedByPriority(),
                        metricsRegistry.getReceivedByRegion(),
                        metricsRegistry.getProcessedByRegion(),
                        metricsRegistry.getFailedByRegion(),
                        metricsRegistry.getTotalReceived(),
                        metricsRegistry.getTotalProcessed(),
                        metricsRegistry.getTotalFailed());

        return ResponseEntity.ok(response);
    }

    public record ConsumerMetricsResponse(
            Map<ru.mentee.power.orders.domain.model.Order.OrderPriority, Long> receivedByPriority,
            Map<ru.mentee.power.orders.domain.model.Order.OrderPriority, Long> processedByPriority,
            Map<ru.mentee.power.orders.domain.model.Order.OrderPriority, Long> failedByPriority,
            Map<String, Long> receivedByRegion,
            Map<String, Long> processedByRegion,
            Map<String, Long> failedByRegion,
            long totalReceived,
            long totalProcessed,
            long totalFailed) {}
}
