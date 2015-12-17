package com.netflix.runtime.health.api.internal;

import com.netflix.runtime.health.api.CompositeHealthIndicator;
import com.netflix.runtime.health.api.HealthIndicator;
import com.netflix.runtime.health.api.HealthIndicatorStatus;
import com.netflix.runtime.health.api.HealthIndicatorStatuses;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * A composite health indicator that can be externally controlled to enable/disable unhealthy status propagation.
 */
public class OnOffHealthIndicator implements CompositeHealthIndicator {

    private final List<HealthIndicator> indicators;
    private Supplier<Boolean> trigger;

    public OnOffHealthIndicator(HealthIndicator indicator, Supplier<Boolean> trigger) {
        this.trigger = trigger;
        this.indicators = Collections.singletonList(indicator);
    }

    @Override
    public CompletableFuture<HealthIndicatorStatus> check() {
        return indicators.get(0).check().thenApply(status ->
                trigger.get() ? status : HealthIndicatorStatuses.create(status.getName(), true, status.getAttributes(), null)
        );
    }

    @Override
    public String getName() {
        return "onOff(" + indicators.get(0).getName() + ')';
    }

    @Override
    public List<HealthIndicator> getIndicators() {
        return indicators;
    }
}
