package ru.mentee.power.orders.domain.usecase;

import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import ru.mentee.power.orders.adapters.integration.PricingHttpClient;
import ru.mentee.power.orders.ports.incoming.ProcessOrderEventPort;
import ru.mentee.power.orders.ports.outgoing.DeadLetterPublisher;
import ru.mentee.power.orders.ports.outgoing.OrderPersistencePort;

// нужно сделать этот класс под PricingProcessingUseCase
@Component
@Primary
@RequiredArgsConstructor
public class OrderProcessingUseCase implements ProcessOrderEventPort {

    private final PricingHttpClient orderClient;
    private final OrderPersistencePort persistencePort;
    private final DeadLetterPublisher deadLetterPublisher;

    @Override
    @Retry(name = "order-processing", fallbackMethod = "fallback")
    public void handle(Command command) {
        // var order = orderClient.fetchOrder(command.orderId(), command.region());
        // persistencePort.save(command.toDomain(order), command.eventId());
    }

    @SuppressWarnings("unused")
    private void fallback(Command command, Throwable throwable) {
        deadLetterPublisher.publish(command, throwable);
    }
}
