/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.orders.adapters.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.mentee.power.orders.adapters.mapper.OrderMapper;
import ru.mentee.power.orders.domain.usecase.PlaceOrderUseCase;
import ru.mentee.power.orders.domain.validator.OrderValidator;
import ru.mentee.power.orders.ports.incoming.PlaceOrderPort;
import ru.mentee.power.orders.ports.outgoing.OrderEventPort;

@Configuration
@RequiredArgsConstructor
public class UseCaseConfig {

    private final OrderEventPort orderEventPort;
    private final OrderMapper orderMapper;
    private final OrderValidator orderValidator;

    @Bean
    public PlaceOrderPort placeOrderPort() {
        return new PlaceOrderUseCase(orderEventPort, orderMapper, orderValidator);
    }
}
