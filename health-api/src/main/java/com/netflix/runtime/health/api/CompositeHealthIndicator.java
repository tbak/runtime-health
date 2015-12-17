package com.netflix.runtime.health.api;

import java.util.List;

/**
 */
public interface CompositeHealthIndicator extends HealthIndicator {

    /**
     * Provide access to the individual {@link HealthIndicator}s that are aggregated by this composite.
     * The aggregation rules are specific to each {@link CompositeHealthIndicator} implementation.
     */
    List<HealthIndicator> getIndicators();
}
