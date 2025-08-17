package ru.dmitartur.client.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class BusinessMetrics {
    private final Counter userRegistrationCounter;
    private final Counter startCommandCounter;

    public BusinessMetrics(MeterRegistry meterRegistry) {
        this.userRegistrationCounter = Counter.builder("oficiant_business_events_total")
            .description("Business events counter")
            .tag("type", "UserRegistrationSubmit")
            .register(meterRegistry);
            
        this.startCommandCounter = Counter.builder("oficiant_business_events_total")
            .description("Business events counter")
            .tag("type", "StartCommand")
            .register(meterRegistry);
    }

    public void incrementUserRegistration() {
        userRegistrationCounter.increment();
    }

    public void incrementStartCommand() {
        startCommandCounter.increment();
    }
}









