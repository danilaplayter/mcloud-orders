/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.orders.adapters.kafka;

import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.TopicDescription;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaHealthIndicator implements HealthIndicator {

    private final KafkaAdmin kafkaAdmin;
    private static final String TOPIC_NAME = "order-events";
    private static final long TIMEOUT_MS = 5000;

    @Override
    public Health health() {
        try (AdminClient adminClient =
                AdminClient.create(kafkaAdmin.getConfigurationProperties())) {

            if (!isBrokerAvailable(adminClient)) {
                return Health.down().withDetail("error", "Kafka broker is not available").build();
            }

            if (!isTopicExists(adminClient)) {
                return Health.down()
                        .withDetail("error", "Topic '" + TOPIC_NAME + "' does not exist")
                        .build();
            }

            return Health.up()
                    .withDetail("broker", "available")
                    .withDetail("topic", TOPIC_NAME + " exists")
                    .build();

        } catch (KafkaException e) {
            log.error("Kafka health check failed", e);
            return Health.down()
                    .withDetail("error", "Kafka connection failed: " + e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("Unexpected error during Kafka health check", e);
            return Health.down().withDetail("error", "Unexpected error: " + e.getMessage()).build();
        }
    }

    private boolean isBrokerAvailable(AdminClient adminClient) {
        try {
            adminClient.describeCluster().nodes().get(TIMEOUT_MS, TimeUnit.MILLISECONDS);
            return true;
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.warn("Kafka broker is not available: {}", e.getMessage());
            return false;
        }
    }

    private boolean isTopicExists(AdminClient adminClient) {
        try {
            DescribeTopicsResult describeResult =
                    adminClient.describeTopics(Collections.singleton(TOPIC_NAME));
            TopicDescription topicDescription =
                    describeResult.values().get(TOPIC_NAME).get(TIMEOUT_MS, TimeUnit.MILLISECONDS);
            return topicDescription != null;
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.warn(
                    "Topic '{}' does not exist or is not accessible: {}",
                    TOPIC_NAME,
                    e.getMessage());
            return false;
        }
    }
}
