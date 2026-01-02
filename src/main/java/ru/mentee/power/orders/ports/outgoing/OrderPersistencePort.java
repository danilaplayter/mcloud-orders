/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.orders.ports.outgoing;

import ru.mentee.power.orders.domain.model.Order;

public interface OrderPersistencePort {

    boolean existsByEventId(String eventId);

    void save(Order order, String eventId);
}
