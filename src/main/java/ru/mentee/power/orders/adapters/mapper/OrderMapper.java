/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.orders.adapters.mapper;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.mentee.power.api.generated.dto.OrderLineRequest;
import ru.mentee.power.api.generated.dto.OrderRequest;
import ru.mentee.power.orders.domain.model.Order;
import ru.mentee.power.orders.domain.model.Order.OrderPriority;
import ru.mentee.power.orders.domain.model.OrderLine;
import ru.mentee.power.orders.ports.incoming.PlaceOrderPort;
import ru.mentee.power.orders.ports.incoming.ProcessOrderEventPort;
import ru.mentee.power.orders.ports.outgoing.OrderEventPort;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "region", source = "region")
    @Mapping(target = "amount", source = "amount", qualifiedByName = "safePrice")
    @Mapping(target = "lines", source = "lines", qualifiedByName = "toEventOrderLinesFromRequests")
    @Mapping(
            target = "orderPriority",
            source = "priority",
            qualifiedByName = "stringToOrderPriority")
    PlaceOrderPort.PlaceOrderCommand toPlaceOrderCommand(OrderRequest orderRequest);

    @Mapping(target = "quantity", source = "quantity", qualifiedByName = "safeQuantity")
    @Mapping(target = "price", source = "price", qualifiedByName = "safePrice")
    OrderEventPort.EventOrderLine toEventOrderLine(OrderLineRequest lineRequest);

    @Mapping(target = "orderId", expression = "java(java.util.UUID.randomUUID())")
    @Mapping(target = "status", constant = "QUEUED")
    @Mapping(target = "customerId", source = "customerId")
    @Mapping(target = "region", source = "region")
    @Mapping(target = "amount", source = "amount", qualifiedByName = "doubleToBigDecimal")
    @Mapping(target = "priority", source = "orderPriority")
    @Mapping(target = "dispatchedAt", expression = "java(java.time.OffsetDateTime.now())")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lines", source = "lines", qualifiedByName = "eventOrderLinesToOrderLines")
    Order toOrder(PlaceOrderPort.PlaceOrderCommand command);

    @Mapping(target = "quantity", source = "quantity", qualifiedByName = "safeQuantity")
    @Mapping(target = "price", source = "price", qualifiedByName = "doubleToBigDecimal")
    PlaceOrderPort.OrderLineCommand toOrderLineCommand(OrderLineRequest lineRequest);

    @Mapping(target = "orderId", source = "orderId")
    @Mapping(target = "customerId", source = "customerId")
    @Mapping(target = "region", source = "region")
    @Mapping(target = "amount", source = "amount", qualifiedByName = "bigDecimalToDouble")
    @Mapping(target = "priority", source = "priority")
    @Mapping(target = "lines", source = "lines", qualifiedByName = "orderLinesToEventOrderLines")
    @Mapping(target = "emittedAt", expression = "java(java.time.OffsetDateTime.now())")
    OrderEventPort.OrderEventPayload toOrderEventPayload(Order order);

    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "price", source = "price", qualifiedByName = "bigDecimalToDouble")
    OrderEventPort.EventOrderLine toEventOrderLine(OrderLine orderLine);

    @Mapping(target = "region", source = "region")
    @Mapping(target = "amount", source = "amount", qualifiedByName = "doubleToBigDecimal")
    @Mapping(target = "priority", source = "priority", qualifiedByName = "stringToOrderPriority")
    @Mapping(target = "status", constant = "PROCESSING")
    @Mapping(
            target = "dispatchedAt",
            source = "emittedAt",
            qualifiedByName = "instantToOffsetDateTime")
    @Mapping(target = "lines", source = "lines", qualifiedByName = "eventCommandLinesToOrderLines")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Order toOrderFromEvent(ProcessOrderEventPort.Command command);

    @Mapping(target = "order", ignore = true)
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "price", source = "price")
    @Mapping(target = "createdAt", ignore = true)
    OrderLine toOrderLineFromEvent(ProcessOrderEventPort.Command.OrderLine line);

    List<OrderEventPort.EventOrderLine> toEventOrderLines(List<OrderLine> orderLines);

    List<PlaceOrderPort.OrderLineCommand> toOrderLineCommands(List<OrderLineRequest> lineRequests);

    @Named("toEventOrderLinesFromRequests")
    default List<OrderEventPort.EventOrderLine> toEventOrderLinesFromRequests(
            List<OrderLineRequest> requests) {
        return Optional.ofNullable(requests)
                .map(req -> req.stream().map(this::toEventOrderLine).toList())
                .orElse(List.of());
    }

    @Named("eventOrderLinesToOrderLines")
    default List<OrderLine> eventOrderLinesToOrderLines(List<OrderEventPort.EventOrderLine> lines) {
        return Optional.ofNullable(lines)
                .map(list -> list.stream().map(this::eventOrderLineToOrderLine).toList())
                .orElse(List.of());
    }

    @Named("orderLinesToEventOrderLines")
    default List<OrderEventPort.EventOrderLine> orderLinesToEventOrderLines(List<OrderLine> lines) {
        return Optional.ofNullable(lines)
                .map(list -> list.stream().map(this::toEventOrderLine).toList())
                .orElse(List.of());
    }

    @Named("eventCommandLinesToOrderLines")
    default List<OrderLine> eventCommandLinesToOrderLines(
            List<ProcessOrderEventPort.Command.OrderLine> lines) {
        return Optional.ofNullable(lines)
                .map(list -> list.stream().map(this::toOrderLineFromEvent).toList())
                .orElse(List.of());
    }

    @Named("eventOrderLineToOrderLine")
    default OrderLine eventOrderLineToOrderLine(OrderEventPort.EventOrderLine eventLine) {
        if (eventLine == null) return null;

        return OrderLine.builder()
                .productId(eventLine.productId())
                .quantity(eventLine.quantity())
                .price(doubleToBigDecimal(eventLine.price()))
                .build();
    }

    @Named("stringToOrderPriority")
    default OrderPriority stringToOrderPriority(String priority) {
        return Optional.ofNullable(priority)
                .map(String::toUpperCase)
                .map(
                        p -> {
                            try {
                                return OrderPriority.valueOf(p);
                            } catch (IllegalArgumentException e) {
                                return OrderPriority.NORMAL;
                            }
                        })
                .orElse(OrderPriority.NORMAL);
    }

    @Named("instantToOffsetDateTime")
    default OffsetDateTime instantToOffsetDateTime(java.time.Instant instant) {
        return Optional.ofNullable(instant)
                .map(i -> i.atOffset(ZoneOffset.UTC))
                .orElse(OffsetDateTime.now());
    }

    @Named("doubleToBigDecimal")
    default BigDecimal doubleToBigDecimal(Double value) {
        return Optional.ofNullable(value).map(BigDecimal::valueOf).orElse(BigDecimal.ZERO);
    }

    @Named("bigDecimalToDouble")
    default Double bigDecimalToDouble(BigDecimal value) {
        return Optional.ofNullable(value).map(BigDecimal::doubleValue).orElse(0.0);
    }

    @Named("safeQuantity")
    default Integer safeQuantity(Integer quantity) {
        return Optional.ofNullable(quantity).orElse(0);
    }

    @Named("safePrice")
    default Double safePrice(Double price) {
        return Optional.ofNullable(price).orElse(0.0);
    }
}
