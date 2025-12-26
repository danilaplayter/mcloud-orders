/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.orders.adapters.web;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mentee.power.api.generated.controller.OrdersApi;
import ru.mentee.power.api.generated.dto.OrderAcceptedResponse;
import ru.mentee.power.api.generated.dto.OrderRequest;
import ru.mentee.power.api.generated.dto.ProducerMetricsResponse;
import ru.mentee.power.api.generated.dto.ProducerMetricsResponseTopicsValue;
import ru.mentee.power.api.generated.dto.ProducerMetricsResponseTotals;
import ru.mentee.power.orders.adapters.mapper.OrderMapper;
import ru.mentee.power.orders.adapters.metrics.ProducerMetricsRegistry;
import ru.mentee.power.orders.domain.exception.OrderValidationException;
import ru.mentee.power.orders.ports.incoming.PlaceOrderPort;

@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController implements OrdersApi {

    private final PlaceOrderPort placeOrderPort;
    private final OrderMapper orderMapper;
    private final ProducerMetricsRegistry metricsRegistry;

    @Override
    public ResponseEntity<OrderAcceptedResponse> submitOrder(OrderRequest orderRequest) {

        try {
            PlaceOrderPort.PlaceOrderCommand command =
                    orderMapper.toPlaceOrderCommand(orderRequest);
            PlaceOrderPort.PlaceOrderResult result = placeOrderPort.placeOrder(command);
            OrderAcceptedResponse response = new OrderAcceptedResponse();
            response.setOrderId(result.orderId());
            response.setStatus(
                    OrderAcceptedResponse.StatusEnum.valueOf(result.orderStatus().name()));
            response.setDispatchedAt(result.dispatchedAt());

            return ResponseEntity.accepted().body(response);
        } catch (OrderValidationException e) {
            log.warn("Order validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Internal server error while processing order", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Override
    public ResponseEntity<ProducerMetricsResponse> getOrderProducerMetrics() {
        try {
            Set<String> allTopics = new HashSet<>();
            allTopics.addAll(metricsRegistry.getSuccessCounters().keySet());
            allTopics.addAll(metricsRegistry.getFailureCounters().keySet());

            Map<String, ProducerMetricsResponseTopicsValue> topicsMap = new HashMap<>();
            long totalSuccess = 0;
            long totalFailure = 0;

            for (String topic : allTopics) {
                AtomicLong successCounter = metricsRegistry.getSuccessCounters().get(topic);
                AtomicLong failureCounter = metricsRegistry.getFailureCounters().get(topic);

                long success = (successCounter != null) ? successCounter.get() : 0;
                long failure = (failureCounter != null) ? failureCounter.get() : 0;

                totalSuccess += success;
                totalFailure += failure;

                ProducerMetricsResponseTopicsValue topicsValue =
                        new ProducerMetricsResponseTopicsValue();
                topicsValue.setSuccess((int) success);
                topicsValue.setFailure((int) failure);

                topicsMap.put(topic, topicsValue);
            }

            ProducerMetricsResponseTotals totals =
                    new ProducerMetricsResponseTotals((int) totalSuccess, (int) totalFailure);

            ProducerMetricsResponse response = new ProducerMetricsResponse(totals, topicsMap);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error while getting producer metrics", e);

            ProducerMetricsResponseTotals totals = new ProducerMetricsResponseTotals(0, 0);
            ProducerMetricsResponse response = new ProducerMetricsResponse(totals, new HashMap<>());

            return ResponseEntity.ok(response);
        }
    }
}
