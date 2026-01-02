/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.orders.adapters.persistence.jpa;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringOrderRepository extends JpaRepository<OrderEntity, UUID> {

    boolean existsByEventId(String eventId);
}
