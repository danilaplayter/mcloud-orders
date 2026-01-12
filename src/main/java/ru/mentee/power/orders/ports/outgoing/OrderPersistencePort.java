/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.orders.ports.outgoing;

import java.util.UUID;
import ru.mentee.power.orders.domain.model.Order;

public interface OrderPersistencePort {

    boolean existsByEventId(UUID eventId);

    void save(Order order, UUID eventId);
}
