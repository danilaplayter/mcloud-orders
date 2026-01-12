package ru.mentee.power.orders.ports.outgoing;

import org.springframework.stereotype.Component;
import ru.mentee.power.orders.ports.incoming.ProcessOrderEventPort.Command;

@Component
public interface DeadLetterPublisher {
    void publish(Command command, Throwable throwable);
}
