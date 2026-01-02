/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.orders.adapters.kafka;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import ru.mentee.power.orders.domain.model.Order;

public record OrderEventPayload(
        String eventId,
        UUID orderId,
        UUID customerId,
        Order.OrderPriority priority,
        String region,
        BigDecimal amount,
        OffsetDateTime emittedAt,
        List<OrderLine> lines) {

    public record OrderLine(UUID productId, int quantity, BigDecimal price) {}
}
