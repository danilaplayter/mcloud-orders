package ru.mentee.power.orders.ports.outgoing;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.mentee.power.orders.adapters.kafka.OrderEventPayload;
import ru.mentee.power.orders.adapters.metrics.ConsumerMetricsRegistry;
import ru.mentee.power.orders.ports.incoming.ProcessOrderEventPort;

@Component
@RequiredArgsConstructor
public class DeadLetterPublisher {

  private final KafkaTemplate<String, OrderEventPayload> kafkaTemplate;
  private final ConsumerMetricsRegistry metrics;
  //DLQ надо самому реализовать или как-то подтянуть?
  private final DlqPayloadFactory payloadFactory;

  public void publish(ProcessOrderEventPort.Command command, Throwable throwable) {
    var payload = payloadFactory.create(command, throwable);
    kafkaTemplate.send("orders.priority.dlq", command.region(), payload);
    metrics.incrementDlq(command.priority());
  }
}