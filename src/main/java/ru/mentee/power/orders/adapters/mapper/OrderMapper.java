/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.orders.adapters.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.mentee.power.api.generated.dto.OrderLineRequest;
import ru.mentee.power.api.generated.dto.OrderRequest;
import ru.mentee.power.orders.domain.model.Order;
import ru.mentee.power.orders.domain.model.OrderLine;
import ru.mentee.power.orders.ports.incoming.PlaceOrderPort;
import ru.mentee.power.orders.ports.outgoing.OrderEventPort;
import ru.mentee.power.orders.ports.outgoing.OrderEventPort.EventOrderLine;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "customerId", source = "customerId")
    @Mapping(target = "region", source = "region")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "lines", source = "lines")
    @Mapping(target = "orderPriority", source = "priority")
    PlaceOrderPort.PlaceOrderCommand toPlaceOrderCommand(OrderRequest orderRequest);

    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "price", source = "price")
    OrderEventPort.EventOrderLine toEventOrderLine(OrderLineRequest lineRequest);

    @Mapping(target = "orderId", expression = "java(UUID.randomUUID())")
    @Mapping(target = "status", constant = "QUEUED")
    @Mapping(target = "customerId", source = "customerId")
    @Mapping(target = "region", source = "region")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "priority", source = "orderPriority")
    @Mapping(target = "lines", source = "lines")
    @Mapping(target = "dispatchedAt", expression = "java(java.time.OffsetDateTime.now())")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Order toOrder(PlaceOrderPort.PlaceOrderCommand command);

    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "price", source = "price")
    PlaceOrderPort.OrderLineCommand toOrderLineCommand(OrderLineRequest lineRequest);

    @Mapping(target = "orderId", source = "orderId")
    @Mapping(target = "customerId", source = "customerId")
    @Mapping(target = "region", source = "region")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "priority", source = "priority")
    @Mapping(target = "lines", source = "lines")
    @Mapping(target = "emittedAt", expression = "java(java.time.OffsetDateTime.now())")
    OrderEventPort.OrderEventPayload toOrderEventPayload(Order order);

    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "price", source = "price")
    OrderEventPort.EventOrderLine toEventOrderLine(OrderLine orderLine);

    List<EventOrderLine> toEventOrderLines(List<OrderLine> orderLines);

    List<PlaceOrderPort.OrderLineCommand> toOrderLineCommands(List<OrderLineRequest> lineRequests);
}
