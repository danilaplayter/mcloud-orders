/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.orders.ports.incoming;

import ru.mentee.power.orders.domain.model.Order;

public interface PlaceOrderPort {
    Order placeOrder(Order order);
}
