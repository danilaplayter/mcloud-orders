/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.orders.ports.outgoing;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import ru.mentee.power.orders.domain.model.Order.OrderPriority;
import ru.mentee.power.orders.domain.model.OrderLine;

public interface OrderEventPort {

    record OrderEventPayload(
            UUID orderId,
            UUID customerId,
            String region,
            Double amount,
            OrderPriority priority,
            List<OrderLine> lines,
            OffsetDateTime emittedAt) {}

    record EventOrderLine(UUID productId, Integer quantity, Double price) {}

    CompletableFuture<Void> publish(OrderEventPayload payload);
}
