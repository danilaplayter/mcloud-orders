/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.orders.adapters.persistence.jpa;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.mentee.power.orders.domain.model.Order;
import ru.mentee.power.orders.domain.model.Order.OrderStatus;
import ru.mentee.power.orders.domain.model.OrderLine;
import ru.mentee.power.orders.ports.outgoing.OrderPersistencePort;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPersistenceAdapter implements OrderPersistencePort {

    private final SpringOrderRepository repository;

    @Override
    public boolean existsByEventId(String eventId) {
        return repository.existsByEventId(eventId);
    }

    @Override
    @Transactional
    public void save(Order order, String eventId) {
        try {
            log.info("Сохранение заказа в БД: orderId={}, eventId={}", order.getOrderId(), eventId);

            // Проверяем, не существует ли уже такой заказ
            if (existsByEventId(eventId)) {
                log.debug("Заказ уже существует в БД по eventId: {}", eventId);
                return;
            }

            // Создаем сущность заказа
            OrderEntity orderEntity =
                    OrderEntity.builder()
                            .orderId(order.getOrderId())
                            .customerId(order.getCustomerId())
                            .priority(order.getPriority())
                            .region(order.getRegion())
                            .amount(order.getAmount())
                            .status(
                                    order.getStatus() != null
                                            ? order.getStatus()
                                            : OrderStatus.PROCESSING)
                            .eventId(eventId)
                            .emittedAt(
                                    order.getDispatchedAt() != null
                                            ? order.getDispatchedAt().toInstant()
                                            : null)
                            .dispatchedAt(order.getDispatchedAt())
                            .processedAt(Instant.now())
                            .build();

            // Сохраняем заказ
            OrderEntity savedOrder = repository.save(orderEntity);
            log.debug("Заказ сохранен: {}", savedOrder.getOrderId());

            // Сохраняем позиции заказа
            if (order.getLines() != null && !order.getLines().isEmpty()) {
                for (OrderLine line : order.getLines()) {
                    OrderLineEntity lineEntity =
                            OrderLineEntity.builder()
                                    .order(savedOrder)
                                    .productId(line.getProductId())
                                    .quantity(line.getQuantity())
                                    .price(line.getPrice())
                                    .build();

                    // Сохраняем через каскад или явно
                    savedOrder.getLines().add(lineEntity);
                }
                repository.save(savedOrder); // Сохраняем каскадно
                log.debug("Сохранено {} позиций заказа", order.getLines().size());
            }

            log.info(
                    "Заказ успешно сохранен в БД: orderId={}, eventId={}",
                    order.getOrderId(),
                    eventId);

        } catch (Exception e) {
            log.error(
                    "Ошибка сохранения заказа в БД: orderId={}, eventId={}, error={}",
                    order.getOrderId(),
                    eventId,
                    e.getMessage(),
                    e);
            throw new RuntimeException("Ошибка сохранения заказа: " + e.getMessage(), e);
        }
    }
}
