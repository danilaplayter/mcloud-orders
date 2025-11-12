/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.orders.ports.outgoing;

import ru.mentee.power.orders.domain.model.Order;

public interface OrderEventPort {
    void publishOrderEvent(Order order);
}
