/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.orders.adapters.web;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mentee.power.api.generated.controller.DefaultApi;
import ru.mentee.power.api.generated.dto.OrderRequest;
import ru.mentee.power.api.generated.dto.OrderResponse;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController implements DefaultApi {
    @Override
    public ResponseEntity<OrderResponse> createOrder(OrderRequest orderRequest) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public ResponseEntity<OrderResponse> getOrder(UUID orderId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
