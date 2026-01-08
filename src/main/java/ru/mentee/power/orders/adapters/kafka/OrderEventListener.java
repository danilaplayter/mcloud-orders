/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.orders.adapters.kafka;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import ru.mentee.power.orders.adapters.metrics.ConsumerMetricsRegistry;
import ru.mentee.power.orders.ports.incoming.ProcessOrderEventPort;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final ProcessOrderEventPort eventPort;
    private final ConsumerMetricsRegistry metricsRegistry;
    private final OrderEventMapper mapper;

    @KafkaListener(
            id = "order-consumer",
            topics = {"orders.priority.high", "orders.priority.normal", "orders.priority.low"},
            containerFactory = "kafkaListenerContainerFactory")
    public void listen(
            @Valid @Payload ru.mentee.power.orders.adapters.kafka.OrderEventPayload payload,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) Integer partition,
            @Header(KafkaHeaders.OFFSET) Long offset,
            Acknowledgment ack) {

        try {
            log.debug(
                    "Получено событие: eventId={}, topic={}, partition={}, offset={}",
                    payload.eventId(),
                    topic,
                    partition,
                    offset);

            metricsRegistry.incrementReceived(payload.priority(), payload.region());

            ProcessOrderEventPort.Command command = mapper.toCommand(payload, partition, offset);
            eventPort.handle(command);

            metricsRegistry.incrementProcessed(payload.priority(), payload.region());

            if (ack != null) {
                ack.acknowledge();
                log.debug("Offset закоммичен для eventId={}", payload.eventId());
            }
        } catch (Exception e) {
            log.error(
                    "Ошибка обработки события: eventId={}, topic={}, error={}",
                    payload.eventId(),
                    topic,
                    e.getMessage(),
                    e);
            metricsRegistry.incrementFailed(payload.priority(), payload.region());
            throw e;
        }
    }
}
