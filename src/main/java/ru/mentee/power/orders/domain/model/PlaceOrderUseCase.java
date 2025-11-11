/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.orders.domain.model;

import lombok.RequiredArgsConstructor;
import ru.mentee.power.orders.ports.incoming.PlaceOrderPort;
import ru.mentee.power.orders.ports.outgoing.OrderEventPort;

@RequiredArgsConstructor
public class PlaceOrderUseCase implements PlaceOrderPort {
    private final OrderEventPort orderEventPort;

    @Override
    public Order placeOrder(Order order) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
