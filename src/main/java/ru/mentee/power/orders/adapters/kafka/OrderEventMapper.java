/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.orders.adapters.kafka;

import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.stereotype.Component;
import ru.mentee.power.orders.ports.incoming.ProcessOrderEventPort;

@Component
public class OrderEventMapper {

    public ProcessOrderEventPort.Command toCommand(
            OrderEventPayload payload, Integer partition, Long offset) {

        return new ProcessOrderEventPort.Command(
                payload.eventId(),
                payload.orderId(),
                payload.customerId(),
                payload.priority(),
                payload.region(),
                payload.amount(),
                payload.emittedAt() != null ? payload.emittedAt().toInstant() : Instant.now(),
                payload.lines().stream()
                        .map(
                                line ->
                                        new ProcessOrderEventPort.Command.OrderLine(
                                                line.productId(), line.quantity(), line.price()))
                        .toList());
    }

    public OrderEventPayload fromProducerPayload(
            ru.mentee.power.orders.ports.outgoing.OrderEventPort.OrderEventPayload payload,
            String eventId) {
        return new OrderEventPayload(
                eventId,
                payload.orderId(),
                payload.customerId(),
                payload.priority(),
                payload.region(),
                BigDecimal.valueOf(payload.amount()),
                payload.emittedAt(),
                payload.lines().stream()
                        .map(
                                line ->
                                        new OrderEventPayload.OrderLine(
                                                line.productId(),
                                                line.quantity(),
                                                BigDecimal.valueOf(line.price())))
                        .toList());
    }
}
