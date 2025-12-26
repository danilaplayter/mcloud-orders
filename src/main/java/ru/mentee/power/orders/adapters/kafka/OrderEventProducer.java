/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.orders.adapters.kafka;

import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.mentee.power.orders.adapters.metrics.ProducerMetricsRegistry;
import ru.mentee.power.orders.ports.outgoing.OrderEventPort;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventProducer implements OrderEventPort {

    private final KafkaTemplate<String, OrderEventPayload> kafkaTemplate;
    private final KafkaTopicResolver topicResolver;
    private final ProducerMetricsRegistry metricsRegistry;

    @Override
    public CompletableFuture<Void> publish(OrderEventPayload payload) {

        String topic = topicResolver.resolve(payload.priority());
        String key = payload.region();
        ProducerRecord<String, OrderEventPayload> record =
                new ProducerRecord<>(topic, key, payload);
        log.info(
                "Sending order event to topic: {}, key: {}, orderId: {}",
                topic,
                key,
                payload.orderId());
        CompletableFuture<Void> future = new CompletableFuture<>();

        return kafkaTemplate
                .send(record)
                .thenRun(
                        () -> {
                            metricsRegistry.incrementSuccess(topic);
                            log.debug(
                                    "Successfully sent order event to Kafka, orderId: {}",
                                    payload.orderId());
                        })
                .exceptionally(
                        e -> {
                            metricsRegistry.incrementFailure(topic);
                            log.error(
                                    "Failed to send order event to Kafka, orderId: {}",
                                    payload.orderId(),
                                    e);
                            throw new RuntimeException(e);
                        })
                .thenApply(result -> null);
    }
}
