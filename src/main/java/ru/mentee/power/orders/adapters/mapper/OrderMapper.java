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

    @Mapping(target = "orderId", expression = "java(java.util.UUID.randomUUID())")
    @Mapping(target = "status", constant = "QUEUED")
    @Mapping(target = "customerId", source = "customerId")
    @Mapping(target = "region", source = "region")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "priority", source = "orderPriority")
    @Mapping(target = "dispatchedAt", expression = "java(java.time.OffsetDateTime.now())")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lines", ignore = true) // Игнорируем lines, они добавляются отдельно
    Order toOrder(PlaceOrderPort.PlaceOrderCommand command);

    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "price", source = "price")
    PlaceOrderPort.OrderLineCommand toOrderLineCommand(OrderLineRequest lineRequest);

    @Mapping(target = "orderId", source = "orderId")
    @Mapping(target = "customerId", source = "customerId")
    @Mapping(target = "region", source = "region")
    @Mapping(target = "amount", expression = "java(order.getAmount().doubleValue())")
    @Mapping(target = "priority", source = "priority")
    @Mapping(target = "lines", source = "lines")
    @Mapping(target = "emittedAt", expression = "java(java.time.OffsetDateTime.now())")
    OrderEventPort.OrderEventPayload toOrderEventPayload(Order order);

    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "price", expression = "java(orderLine.getPrice().doubleValue())")
    OrderEventPort.EventOrderLine toEventOrderLine(OrderLine orderLine);

    List<OrderEventPort.EventOrderLine> toEventOrderLines(List<OrderLine> orderLines);

    List<PlaceOrderPort.OrderLineCommand> toOrderLineCommands(List<OrderLineRequest> lineRequests);

    default OrderLine toOrderLine(PlaceOrderPort.OrderLineCommand command) {
        OrderLine orderLine = new OrderLine();
        orderLine.setProductId(command.productId());
        orderLine.setQuantity(command.quantity());
        orderLine.setPrice(command.price());
        return orderLine;
    }

    default List<OrderLine> toOrderLineList(List<PlaceOrderPort.OrderLineCommand> commands) {
        return commands.stream().map(this::toOrderLine).toList();
    }
}
