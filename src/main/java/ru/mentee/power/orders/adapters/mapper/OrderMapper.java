/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.orders.adapters.mapper;

import org.mapstruct.Mapper;
import ru.mentee.power.api.generated.dto.OrderRequest;
import ru.mentee.power.api.generated.dto.OrderResponse;
import ru.mentee.power.orders.domain.model.Order;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    Order toDomain(OrderRequest request);

    OrderResponse toResponse(Order order);
}
