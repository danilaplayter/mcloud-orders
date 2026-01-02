/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.orders.adapters.persistence.jpa;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "crm_order_lines", schema = "crm")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderLineEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "line_id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "price", nullable = false, precision = 19, scale = 4)
    private BigDecimal price;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
