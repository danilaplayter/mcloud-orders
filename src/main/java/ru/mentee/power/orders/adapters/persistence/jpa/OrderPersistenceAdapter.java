package ru.mentee.power.orders.adapters.persistence.jpa;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.mentee.power.orders.domain.model.Order;
import ru.mentee.power.orders.ports.outgoing.OrderPersistencePort;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPersistenceAdapter implements OrderPersistencePort {

    private final SpringOrderRepository repository;
    private final OrderEntityMapper orderEntityMapper;

    @Override
    public boolean existsByEventId(UUID eventId) {
        return repository.existsByEventId(eventId);
    }

    @Override
    @Transactional
    public void save(Order order, UUID eventId) {
        try {
            log.info("Сохранение заказа в БД: orderId={}, eventId={}", order.getOrderId(), eventId);

            if (existsByEventId(eventId)) {
                log.debug("Заказ уже существует в БД по eventId: {}", eventId);
                return;
            }

            OrderEntity orderEntity =
                    orderEntityMapper.toOrderEntityWithEventId(order, eventId, Instant.now());

            OrderEntity savedOrder = repository.save(orderEntity);
            log.debug("Заказ сохранен: {}", savedOrder.getOrderId());

            saveOrderLines(order, savedOrder);

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

    private void saveOrderLines(Order order, OrderEntity savedOrder) {
        Optional.ofNullable(order.getLines())
                .filter(lines -> !lines.isEmpty())
                .ifPresent(
                        lines -> {
                            lines.forEach(
                                    line -> {
                                        OrderLineEntity lineEntity =
                                                orderEntityMapper.toOrderLineEntity(line);
                                        lineEntity.setOrder(savedOrder);
                                        savedOrder.getLines().add(lineEntity);
                                    });

                            repository.save(savedOrder); // Сохраняем каскадно
                            log.debug("Сохранено {} позиций заказа", lines.size());
                        });
    }
}
