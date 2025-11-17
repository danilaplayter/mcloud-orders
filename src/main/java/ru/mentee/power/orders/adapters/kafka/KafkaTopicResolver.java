/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.orders.adapters.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.mentee.power.orders.domain.model.Order;

@Slf4j
@Component
public class KafkaTopicResolver {

    @Value("${kafka.topics.orders.priority.high:orders.priority.high}")
    private String highPriorityTopic;

    @Value("${kafka.topics.orders.priority.normal:orders.priority.normal}")
    private String normalPriorityTopic;

    @Value("${kafka.topics.orders.priority.low:orders.priority.low}")
    private String lowPriorityTopic;

    public String resolve(Order.OrderPriority priority) {
        return switch (priority) {
            case HIGH -> highPriorityTopic;
            case NORMAL -> normalPriorityTopic;
            case LOW -> lowPriorityTopic;
        };
    }
}
