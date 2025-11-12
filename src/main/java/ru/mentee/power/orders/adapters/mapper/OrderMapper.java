/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.orders.adapters.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.mentee.power.orders.domain.model.Order;
import ru.mentee.power.orders.domain.model.OrderLine;
import ru.mentee.power.orders.ports.incoming.PlaceOrderPort.OrderLineCommand;
import ru.mentee.power.orders.ports.incoming.PlaceOrderPort.PlaceOrderCommand;
import ru.mentee.power.orders.ports.outgoing.OrderEventPort.EventOrderLine;
import ru.mentee.power.orders.ports.outgoing.OrderEventPort.OrderEventPayload;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    @Mapping(target = "orderId", ignore = true)
    @Mapping(target = "status", constant = "QUEUED")
    @Mapping(target = "dispatchedAt", expression = "java(java.time.OffsetDateTime.now())")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lines", source = "lines")
    Order toOrder(PlaceOrderCommand command);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    OrderLine toOrderLine(OrderLineCommand lineCommand);

    OrderEventPayload toOrderEventPayload(Order order);

    EventOrderLine toEventOrderLine(OrderLine orderLine);
}
