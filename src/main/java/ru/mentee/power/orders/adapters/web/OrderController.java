/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.orders.adapters.web;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mentee.power.api.generated.controller.OrdersApi;
import ru.mentee.power.api.generated.dto.OrderAcceptedResponse;
import ru.mentee.power.api.generated.dto.OrderRequest;
import ru.mentee.power.api.generated.dto.ProducerMetricsResponse;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController implements OrdersApi {

    @Override
    public ResponseEntity<ProducerMetricsResponse> getOrderProducerMetrics() {
        return OrdersApi.super.getOrderProducerMetrics();
    }

    @Override
    public ResponseEntity<OrderAcceptedResponse> submitOrder(OrderRequest orderRequest) {
        return OrdersApi.super.submitOrder(orderRequest);
    }
}
