/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.orders.ports.incoming;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import ru.mentee.power.orders.domain.model.Order;

public interface ProcessOrderEventPort {

    void handle(Command command);

    record Command(
            @NotBlank UUID eventId,
            @NotNull UUID orderId,
            @NotNull UUID customerId,
            @NotNull Order.OrderPriority priority,
            @NotNull String region,
            @Positive BigDecimal amount,
            @NotNull Instant emittedAt,
            @Valid List<OrderLine> lines) {
        public record OrderLine(
                @NotNull UUID productId, @Positive int quantity, @Positive BigDecimal price) {}
    }
}
