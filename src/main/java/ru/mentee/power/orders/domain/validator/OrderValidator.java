/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.orders.domain.validator;

import org.springframework.stereotype.Component;
import ru.mentee.power.orders.domain.exception.OrderValidationException;
import ru.mentee.power.orders.ports.incoming.PlaceOrderPort.PlaceOrderCommand;
import ru.mentee.power.orders.ports.outgoing.OrderEventPort.EventOrderLine;

@Component
public class OrderValidator {
    public void validate(PlaceOrderCommand command) {
        if (command.customerId() == null) {
            throw new OrderValidationException("Customer ID is required");
        }
        if (command.region() == null || command.region().trim().isEmpty()) {
            throw new OrderValidationException("Region is required");
        }
        if (command.amount() == null || command.amount() <= 0) {
            throw new OrderValidationException("Amount must be positive");
        }
        if (command.lines() == null || command.lines().isEmpty()) {
            throw new OrderValidationException("Order must have at least one line");
        }

        command.lines().forEach(this::validateLine);
    }

    private void validateLine(EventOrderLine line) {
        if (line.productId() == null) {
            throw new OrderValidationException("Product ID is required in order line");
        }
        if (line.quantity() <= 0) {
            throw new OrderValidationException("Line quantity must be positive");
        }
        if (line.price() < 0) {
            throw new OrderValidationException("Line price cannot be negative");
        }
    }
}
