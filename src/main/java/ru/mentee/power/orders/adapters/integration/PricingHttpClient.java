package ru.mentee.power.orders.adapters.integration;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.mentee.power.orders.domain.model.Order;
import ru.mentee.power.orders.domain.model.Order.OrderPriority;
import ru.mentee.power.orders.domain.model.Order.OrderStatus;
import ru.mentee.power.orders.domain.model.OrderLine;

@Slf4j
@Component
public class PricingHttpClient {

    public Order fetchOrder(UUID orderId, String region) {
        log.info("Fetching order {} for region {}", orderId, region);

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Delay simulation interrupted");
        }

        Order order = createMockOrder(orderId, region);

        log.info("Order {} successfully fetched", orderId);
        return order;
    }

    private Order createMockOrder(UUID orderId, String region) {
        Order order = new Order();
        order.setOrderId(orderId);
        order.setCustomerId(UUID.randomUUID());
        order.setPriority(OrderPriority.NORMAL);
        order.setRegion(region);
        order.setAmount(BigDecimal.valueOf(999.99));
        order.setStatus(OrderStatus.PROCESSING);
        order.setCreatedAt(OffsetDateTime.now().minusHours(1));
        order.setUpdatedAt(OffsetDateTime.now());
        order.setLines(createMockOrderLines());

        for (OrderLine line : order.getLines()) {
            line.setOrder(order);
        }

        return order;
    }

    private List<OrderLine> createMockOrderLines() {
        return List.of(
                OrderLine.builder()
                        .productId(UUID.randomUUID())
                        .quantity(2)
                        .price(BigDecimal.valueOf(499.99))
                        .build());
    }
}
