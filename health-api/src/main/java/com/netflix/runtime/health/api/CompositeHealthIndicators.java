package com.netflix.runtime.health.api;

import com.netflix.runtime.health.api.internal.CompositeHealthIndicatorImpl;
import com.netflix.runtime.health.api.internal.OnOffHealthIndicator;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 */
public final class CompositeHealthIndicators {

    private CompositeHealthIndicators() {
    }

    public static CompositeHealthIndicator and(HealthIndicator... healthIndicators) {
        return and(Arrays.asList(healthIndicators));
    }

    public static CompositeHealthIndicator and(List<HealthIndicator> healthIndicators) {
        return new CompositeHealthIndicatorImpl("and", healthIndicators, (status, acc) -> status.isHealthy() && acc);
    }

    public static CompositeHealthIndicator or(HealthIndicator... healthIndicators) {
        return or(Arrays.asList(healthIndicators));
    }

    public static CompositeHealthIndicator or(List<HealthIndicator> healthIndicators) {
        return new CompositeHealthIndicatorImpl("or", healthIndicators, (status, acc) -> status.isHealthy() || acc);
    }

    public static HealthIndicator onOff(HealthIndicator indicator, Supplier<Boolean> trigger) {
        return new OnOffHealthIndicator(indicator, trigger);
    }
}
