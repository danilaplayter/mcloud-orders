/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.orders.adapters.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.mentee.power.orders.domain.model.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {}
