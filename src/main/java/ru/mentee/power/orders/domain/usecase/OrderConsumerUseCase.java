/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.orders.domain.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.mentee.power.orders.adapters.mapper.OrderMapper;
import ru.mentee.power.orders.domain.exception.OrderProcessingException;
import ru.mentee.power.orders.domain.model.Order;
import ru.mentee.power.orders.ports.incoming.ProcessOrderEventPort;
import ru.mentee.power.orders.ports.outgoing.OrderPersistencePort;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderConsumerUseCase implements ProcessOrderEventPort {

    private final OrderPersistencePort persistencePort;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public void handle(Command command) {
        if (persistencePort.existsByEventId(command.eventId())) {
            log.info("Дубликат события уже обработан: eventId={}", command.eventId());
            return;
        }

        try {
            Order order = orderMapper.toOrderFromEvent(command);
            persistencePort.save(order, command.eventId());

            log.info(
                    "Заказ успешно обработан: orderId={}, eventId={}",
                    command.orderId(),
                    command.eventId());

        } catch (Exception e) {
            log.error(
                    "Ошибка обработки заказа: orderId={}, eventId={}, error={}",
                    command.orderId(),
                    command.eventId(),
                    e.getMessage(),
                    e);
            throw new OrderProcessingException(
                    "Ошибка обработки заказа: orderId=" + command.orderId(), e);
        }
    }
}
