/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.orders.adapters.mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.mentee.power.api.generated.dto.OrderLineRequest;
import ru.mentee.power.api.generated.dto.OrderRequest;
import ru.mentee.power.orders.domain.model.Order;
import ru.mentee.power.orders.domain.model.OrderLine;
import ru.mentee.power.orders.ports.incoming.PlaceOrderPort;
import ru.mentee.power.orders.ports.incoming.ProcessOrderEventPort;
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
    @Mapping(target = "lines", ignore = true)
    Order toOrder(PlaceOrderPort.PlaceOrderCommand command);

    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "price", source = "price", qualifiedByName = "mapPrice")
    PlaceOrderPort.OrderLineCommand toOrderLineCommand(OrderLineRequest lineRequest);

    @Mapping(target = "orderId", source = "orderId")
    @Mapping(target = "customerId", source = "customerId")
    @Mapping(target = "region", source = "region")
    @Mapping(target = "amount", expression = "java(order.getAmount().doubleValue())")
    @Mapping(target = "priority", source = "priority")
    @Mapping(target = "lines", source = "lines", qualifiedByName = "safeLines")
    @Mapping(target = "emittedAt", expression = "java(java.time.OffsetDateTime.now())")
    OrderEventPort.OrderEventPayload toOrderEventPayload(Order order);

    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "price", expression = "java(orderLine.getPrice().doubleValue())")
    OrderEventPort.EventOrderLine toEventOrderLine(OrderLine orderLine);

    @Mapping(target = "orderId", source = "orderId")
    @Mapping(target = "customerId", source = "customerId")
    @Mapping(target = "region", source = "region")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "priority", source = "priority")
    @Mapping(target = "status", constant = "PROCESSING")
    @Mapping(target = "dispatchedAt", source = "emittedAt")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lines", ignore = true)
    Order toOrder(ProcessOrderEventPort.Command command);

    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "price", source = "price", qualifiedByName = "bigDecimalPrice")
    OrderLine toOrderLine(ProcessOrderEventPort.Command.OrderLine line);

    @Named("bigDecimalPrice")
    default BigDecimal mapPriceToBigDecimal(Double price) {
        return Optional.ofNullable(price)
            .map(BigDecimal::valueOf)
            .orElse(BigDecimal.ZERO);
    }

    List<OrderEventPort.EventOrderLine> toEventOrderLines(List<OrderLine> orderLines);

    List<PlaceOrderPort.OrderLineCommand> toOrderLineCommands(List<OrderLineRequest> lineRequests);

    default OrderLine toOrderLine(PlaceOrderPort.OrderLineCommand command) {
        OrderLine orderLine = new OrderLine();
        orderLine.setProductId(command.productId());
        orderLine.setQuantity(command.quantity());
        orderLine.setPrice(Optional.ofNullable(command.price()).orElse(BigDecimal.ZERO));
        return orderLine;
    }

    default List<OrderLine> toOrderLineList(List<PlaceOrderPort.OrderLineCommand> commands) {
        return Optional.ofNullable(commands).orElse(List.of()).stream()
                .map(this::toOrderLine)
                .toList();
    }

    @Named("mapPrice")
    default Double mapPrice(Double price) {
        return Optional.ofNullable(price).orElse(0.0);
    }

    @Named("safeLines")
    default List<OrderEventPort.EventOrderLine> safeLines(List<OrderLine> lines) {
        return Optional.ofNullable(lines).orElse(List.of()).stream()
                .map(this::toEventOrderLine)
                .toList();
    }
}
