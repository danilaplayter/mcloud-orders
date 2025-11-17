/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.orders.domain.model;

import lombok.RequiredArgsConstructor;
import ru.mentee.power.orders.adapters.mapper.OrderMapper;
import ru.mentee.power.orders.domain.model.Order.OrderStatus;
import ru.mentee.power.orders.domain.validator.OrderValidator;
import ru.mentee.power.orders.ports.incoming.PlaceOrderPort;
import ru.mentee.power.orders.ports.outgoing.OrderEventPort;

@RequiredArgsConstructor
public class PlaceOrderUseCase implements PlaceOrderPort {
    private final OrderEventPort orderEventPort;
    private final OrderMapper orderMapper;
    private final OrderValidator validator;

    @Override
    public PlaceOrderResult placeOrder(PlaceOrderCommand placeOrderCommand) {
        validator.validate(placeOrderCommand);

        Order order = orderMapper.toOrder(placeOrderCommand);

        OrderEventPort.OrderEventPayload orderEventPayload = orderMapper.toOrderEventPayload(order);

        orderEventPort.publish(orderEventPayload);

        return new PlaceOrderResult(
                order.getOrderId(), OrderStatus.QUEUED, order.getDispatchedAt());
    }
}
