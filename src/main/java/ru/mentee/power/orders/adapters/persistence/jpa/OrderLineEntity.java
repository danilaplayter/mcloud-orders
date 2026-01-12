/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.orders.adapters.persistence.jpa;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_lines")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderLineEntity {

    @Id
    @Column(name = "line_id")
    private UUID lineId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
}
