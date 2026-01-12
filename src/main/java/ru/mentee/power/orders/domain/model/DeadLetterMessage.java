package ru.mentee.power.orders.domain.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// его нужно сохранять в бд || нет?
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeadLetterMessage {
    private UUID orderId;
    private String region;
    private UUID eventId;
    private Object originalPayload;
    private Instant failureTimestamp;
    private Map<String, Object> errorDetails;
    private String stackTrace;
    private String processingContext;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant timestamp = Instant.now();
}
