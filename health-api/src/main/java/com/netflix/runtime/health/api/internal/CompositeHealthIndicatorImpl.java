package com.netflix.runtime.health.api.internal;

import com.netflix.runtime.health.api.CompositeHealthIndicator;
import com.netflix.runtime.health.api.HealthIndicator;
import com.netflix.runtime.health.api.HealthIndicatorStatus;
import com.netflix.runtime.health.api.HealthIndicatorStatuses;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

/**
 */
public class CompositeHealthIndicatorImpl implements CompositeHealthIndicator {

    private final String name;
    private final List<HealthIndicator> indicators;
    private final BiFunction<HealthIndicatorStatus, Boolean, Boolean> eval;

    public CompositeHealthIndicatorImpl(String operatorName,
                                        List<HealthIndicator> indicators,
                                        BiFunction<HealthIndicatorStatus, Boolean, Boolean> eval) {
        this(buildNameFrom(operatorName, indicators), operatorName, indicators, eval);
    }

    private CompositeHealthIndicatorImpl(String name,
                                         String operatorName,
                                         List<HealthIndicator> indicators,
                                         BiFunction<HealthIndicatorStatus, Boolean, Boolean> eval) {
        assertNonEmpty(indicators);
        this.name = name;
        this.indicators = indicators;
        this.eval = eval;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<HealthIndicator> getIndicators() {
        return indicators;
    }

    @Override
    public CompletableFuture<HealthIndicatorStatus> check() {
        return checkIndicators().thenApply(this::mergeStatuses);
    }

    private CompletableFuture<List<HealthIndicatorStatus>> checkIndicators() {
        final CompletableFuture<List<HealthIndicatorStatus>> future = new CompletableFuture<>();

        List<HealthIndicatorStatus> statuses = new CopyOnWriteArrayList<>();
        // Run all the HealthIndicators and collect the statuses.
        final AtomicInteger counter = new AtomicInteger(indicators.size());
        for (HealthIndicator indicator : indicators) {
            indicator.check().thenAccept((result) -> {
                // Aggregate the health checks
                statuses.add(result);

                // Reached the last health check so complete the future
                if (counter.decrementAndGet() == 0) {
                    future.complete(statuses);
                }
            });
        }
        return future;
    }

    private HealthIndicatorStatus mergeStatuses(List<HealthIndicatorStatus> statuses) {
        boolean result = statuses.get(0).isHealthy();
        for (int i = 1; i < statuses.size(); i++) {
            result = eval.apply(statuses.get(i), result);
        }
        return result ? HealthIndicatorStatuses.healthy(name) : HealthIndicatorStatuses.unhealthy(name);
    }


    private static String buildNameFrom(String operator, List<HealthIndicator> indicators) {
        StringBuilder sb = new StringBuilder();

        sb.append(operator).append('(');

        for (HealthIndicator indicator : indicators) {
            sb.append(indicator.getName()).append(',');
        }
        sb.setCharAt(sb.length() - 1, ')');
        return sb.toString();
    }

    private static void assertNonEmpty(List<HealthIndicator> healthIndicators) {
        if (healthIndicators == null || healthIndicators.size() < 2) {
            throw new IllegalArgumentException("composite HealthIndicator requires at least two arguments");
        }
    }
}
