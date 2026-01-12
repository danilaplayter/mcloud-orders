package ru.mentee.power.orders.adapters.kafka;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.net.ssl.SSLException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import ru.mentee.power.orders.domain.model.DeadLetterMessage;
import ru.mentee.power.orders.ports.incoming.ProcessOrderEventPort.Command;
import ru.mentee.power.orders.ports.outgoing.DeadLetterPublisher;

@Component
@Slf4j
@RequiredArgsConstructor
public class KafkaDeadLetterPublisher implements DeadLetterPublisher {

    private static final String DLQ_TOPIC = "orders.priority.dlq";
    private static final String ORIGINAL_TOPIC_HEADER = "original-topic";
    private static final String ERROR_CODE_HEADER = "error-code";
    private static final String RETRIES_PERFORMED_HEADER = "retries-performed";
    private static final String EXCEPTION_MESSAGE_HEADER = "exception-message";
    private static final String EXCEPTION_CLASS_HEADER = "exception-class";
    private static final String TIMESTAMP_HEADER = "timestamp";
    private static final String ORDER_ID_HEADER = "order-id";
    private static final String REGION_HEADER = "region";

    private final KafkaTemplate<String, DeadLetterMessage> dlqKafkaTemplate;
    private final RetryRegistry retryRegistry;

    @Override
    public void publish(Command command, Throwable throwable) {
        try {
            DeadLetterMessage message = createDeadLetterMessage(command, throwable);
            Message<DeadLetterMessage> kafkaMessage =
                    createKafkaMessage(command, message, throwable);

            SendResult<String, DeadLetterMessage> sendResult =
                    dlqKafkaTemplate.send(kafkaMessage).get(10, TimeUnit.SECONDS);

            log.info(
                    "DLQ message sent successfully for order {} to topic {}, partition {}, offset {}",
                    command.orderId(),
                    sendResult.getRecordMetadata().topic(),
                    sendResult.getRecordMetadata().partition(),
                    sendResult.getRecordMetadata().offset());

            log.warn(
                    "Order {} sent to DLQ after retries exhausted. Error: {}",
                    command.orderId(),
                    throwable.getMessage());

        } catch (TimeoutException e) {
            log.error(
                    "Timeout while sending DLQ message for order {}: {}",
                    command.orderId(),
                    e.getMessage());
            saveToLocalFallback(command, throwable, "TIMEOUT");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error(
                    "Thread interrupted while sending DLQ message for order {}",
                    command.orderId(),
                    e);
            saveToLocalFallback(command, throwable, "INTERRUPTED");
        } catch (ExecutionException e) {
            log.error(
                    "Execution failed while sending DLQ message for order {}: {}",
                    command.orderId(),
                    e.getCause().getMessage(),
                    e.getCause());
            saveToLocalFallback(command, throwable, "EXECUTION_ERROR");
        } catch (Exception e) {
            log.error("Critical error while publishing to DLQ for order {}", command.orderId(), e);
            saveToLocalFallback(command, throwable, "UNKNOWN_ERROR");
        }
    }

    private DeadLetterMessage createDeadLetterMessage(Command command, Throwable throwable) {
        return DeadLetterMessage.builder()
                .orderId(command.orderId())
                .region(command.region())
                .eventId(command.eventId())
                .failureTimestamp(Instant.now())
                .errorDetails(extractErrorDetails(throwable))
                .stackTrace(getStackTraceAsString(throwable))
                .build();
    }

    private Message<DeadLetterMessage> createKafkaMessage(
            Command command, DeadLetterMessage message, Throwable throwable) {

        int retriesPerformed = getRetriesPerformed("order-processing");

        return MessageBuilder.withPayload(message)
                .setHeader(KafkaHeaders.TOPIC, DLQ_TOPIC)
                .setHeader(KafkaHeaders.KEY, command.orderId())
                .setHeader(ERROR_CODE_HEADER, extractErrorCode(throwable))
                .setHeader(RETRIES_PERFORMED_HEADER, retriesPerformed)
                .setHeader(EXCEPTION_CLASS_HEADER, throwable.getClass().getName())
                .setHeader(EXCEPTION_MESSAGE_HEADER, truncate(throwable.getMessage(), 500))
                .setHeader(TIMESTAMP_HEADER, Instant.now().toString())
                .setHeader(ORDER_ID_HEADER, command.orderId())
                .setHeader(REGION_HEADER, command.region())
                .setHeader("x-dlq-reason", "RETRIES_EXHAUSTED")
                .setHeader("x-processing-attempts", retriesPerformed + 1)
                .build();
    }

    private int getRetriesPerformed(String retryName) {
        try {
            Retry retry = retryRegistry.retry(retryName);
            Retry.Metrics metrics = retry.getMetrics();
            return (int) metrics.getNumberOfFailedCallsWithRetryAttempt();
        } catch (Exception e) {
            log.warn("Could not get retry metrics for {}", retryName, e);
            return 3;
        }
    }

    private String extractErrorCode(Throwable throwable) {
        if (throwable instanceof ResourceAccessException) {
            return "NETWORK_ERROR";
        } else if (throwable instanceof ConnectException) {
            return "CONNECTION_REFUSED";
        } else if (throwable instanceof SocketTimeoutException) {
            return "TIMEOUT";
        } else if (throwable instanceof SSLException) {
            return "SSL_ERROR";
        } else if (throwable instanceof TimeoutException) {
            return "OPERATION_TIMEOUT";
        } else {
            return "UNKNOWN_ERROR";
        }
    }

    private Map<String, Object> extractErrorDetails(Throwable throwable) {
        Map<String, Object> details = new HashMap<>();
        details.put("errorClass", throwable.getClass().getName());
        details.put("errorMessage", throwable.getMessage());
        details.put("rootCause", getRootCause(throwable).getClass().getName());

        if (throwable instanceof ResourceAccessException rae) {
            details.put("errorType", "NETWORK");
        } else if (throwable instanceof TimeoutException) {
            details.put("errorType", "TIMEOUT");
        }

        return details;
    }

    private Throwable getRootCause(Throwable throwable) {
        Throwable cause = throwable;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        return cause;
    }

    private String getStackTraceAsString(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

    private String truncate(String str, int maxLength) {
        if (str == null || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }

    private void saveToLocalFallback(Command command, Throwable throwable, String error) {
        log.error(
                """
        LOCAL FALLBACK: Order processing failed
        Order ID: {}
        Region: {}
        Error Type: {}
        Exception: {}
        Message: {}
        """,
                command.orderId(),
                command.region(),
                error,
                throwable.getClass().getName(),
                throwable.getMessage(),
                throwable);
    }
}
