/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.orders.ports.incoming;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import ru.mentee.power.orders.domain.model.Order.OrderPriority;
import ru.mentee.power.orders.domain.model.Order.OrderStatus;
import ru.mentee.power.orders.ports.outgoing.OrderEventPort.EventOrderLine;

public interface PlaceOrderPort {

    PlaceOrderResult placeOrder(PlaceOrderCommand placeOrderCommand);

    record PlaceOrderCommand(
            UUID customerId,
            String region,
            Double amount,
            List<EventOrderLine> lines,
            OrderPriority orderPriority) {}

    record OrderLineCommand(UUID productId, Integer quantity, BigDecimal price) {}

    record PlaceOrderResult(UUID orderId, OrderStatus orderStatus, OffsetDateTime dispatchedAt) {}
}
