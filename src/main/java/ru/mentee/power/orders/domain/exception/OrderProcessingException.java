package ru.mentee.power.orders.domain.exception;

public class OrderProcessingException extends RuntimeException {
    public OrderProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
