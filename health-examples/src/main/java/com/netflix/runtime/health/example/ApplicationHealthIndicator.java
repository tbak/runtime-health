package com.netflix.runtime.health.example;

import com.netflix.runtime.health.api.CompositeHealthIndicator;
import com.netflix.runtime.health.api.HealthIndicator;
import com.netflix.runtime.health.api.HealthIndicatorStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static com.netflix.runtime.health.api.CompositeHealthIndicators.and;
import static com.netflix.runtime.health.api.CompositeHealthIndicators.onOff;

/**
 * In this example application health check indicators are divided into required, and optional. Optional health indicators could
 * inform about degraded state of a service, and depending on a user requirement could be silenced (accept degraded mode), or
 * propagated (require full SLA).
 */
public class ApplicationHealthIndicator implements CompositeHealthIndicator {

    private final List<HealthIndicator> allIndicators;
    private final CompositeHealthIndicator healthIndicator;

    public ApplicationHealthIndicator(List<HealthIndicator> requiredIndicators, List<HealthIndicator> optionalIndicators, Supplier<Boolean> strict) {
        this.allIndicators = Collections.unmodifiableList(collect(requiredIndicators, optionalIndicators));
        this.healthIndicator = and(
                and(requiredIndicators),
                onOff(and(optionalIndicators), strict)
        );
    }

    @Override
    public List<HealthIndicator> getIndicators() {
        return allIndicators;
    }

    @Override
    public CompletableFuture<HealthIndicatorStatus> check() {
        return healthIndicator.check();
    }

    @Override
    public String getName() {
        return "applicationHealth";
    }

    private static ArrayList<HealthIndicator> collect(List<HealthIndicator> requiredIndicators, List<HealthIndicator> optionalIndicators) {
        ArrayList<HealthIndicator> allIndicators = new ArrayList<>(requiredIndicators);
        allIndicators.addAll(optionalIndicators);
        return allIndicators;
    }
}
