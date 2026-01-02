/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.orders.ports.outgoing;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import ru.mentee.power.orders.domain.model.Order;

public interface OrderEventPort {

    record OrderEventPayload(
            UUID orderId,
            UUID customerId,
            Order.OrderPriority priority,
            String region,
            double amount, // Используем double вместо BigDecimal
            OffsetDateTime emittedAt,
            List<EventOrderLine> lines) {}

    record EventOrderLine(UUID productId, int quantity, double price) {}

    CompletableFuture<Void> publish(OrderEventPayload payload);
}
