package ru.mentee.power.orders.adapters.persistence.jpa;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.mentee.power.orders.domain.model.Order;
import ru.mentee.power.orders.domain.model.OrderLine;

@Mapper(componentModel = "spring")
public interface OrderEntityMapper {

    @Mapping(target = "orderId", source = "orderId")
    @Mapping(target = "customerId", source = "customerId")
    @Mapping(target = "priority", source = "priority")
    @Mapping(target = "region", source = "region")
    @Mapping(target = "amount", source = "amount")
    @Mapping(
            target = "status",
            source = "status",
            defaultExpression =
                    "java(ru.mentee.power.orders.domain.model.Order.OrderStatus.PROCESSING)")
    @Mapping(target = "eventId", ignore = true)
    @Mapping(
            target = "emittedAt",
            source = "dispatchedAt",
            qualifiedByName = "offsetDateTimeToInstant")
    @Mapping(target = "dispatchedAt", source = "dispatchedAt")
    @Mapping(target = "processedAt", ignore = true)
    @Mapping(target = "lines", ignore = true)
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    OrderEntity toOrderEntity(Order order);

    @Mapping(target = "order", ignore = true)
    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "price", source = "price", qualifiedByName = "safePrice")
    @Mapping(target = "createdAt", source = "createdAt")
    OrderLineEntity toOrderLineEntity(OrderLine orderLine);

    @Named("offsetDateTimeToInstant")
    default Instant offsetDateTimeToInstant(OffsetDateTime offsetDateTime) {
        return offsetDateTime != null ? offsetDateTime.toInstant() : null;
    }

    default Instant map(OffsetDateTime offsetDateTime) {
        return offsetDateTime != null ? offsetDateTime.toInstant() : null;
    }

    @Named("safePrice")
    default BigDecimal safePrice(BigDecimal price) {
        return price != null ? price : BigDecimal.ZERO;
    }

    @Named("safePriceDouble")
    default Double safePriceDouble(Double price) {
        return price != null ? price : 0.0;
    }

    default OrderEntity toOrderEntityWithEventId(Order order, String eventId, Instant processedAt) {
        OrderEntity orderEntity = toOrderEntity(order);
        orderEntity.setEventId(eventId);
        orderEntity.setProcessedAt(processedAt);

        if (orderEntity.getCustomerId() == null) {
            throw new IllegalArgumentException("Customer ID не может быть null");
        }

        return orderEntity;
    }
}
