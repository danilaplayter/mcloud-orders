/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.orders.domain.exception;

public class OrderValidationException extends RuntimeException {
    public OrderValidationException(String message) {
        super(message);
    }
}
