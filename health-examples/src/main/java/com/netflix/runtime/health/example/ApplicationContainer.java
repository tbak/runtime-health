package com.netflix.runtime.health.example;

import com.netflix.runtime.health.api.HealthIndicator;
import com.netflix.runtime.health.api.HealthIndicatorStatus;
import com.netflix.runtime.health.api.HealthIndicatorStatuses;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 */
public class ApplicationContainer {

    static class ServiceWithHealthIndicator implements HealthIndicator {

        private String serviceName;
        private boolean serviceUp;

        ServiceWithHealthIndicator(String serviceName) {
            this.serviceName = serviceName;
        }

        @Override
        public CompletableFuture<HealthIndicatorStatus> check() {
            return CompletableFuture.completedFuture(
                    serviceUp ? HealthIndicatorStatuses.healthy(serviceName) : HealthIndicatorStatuses.unhealthy(serviceName)
            );
        }

        @Override
        public String getName() {
            return serviceName;
        }

        public void setServiceUp(boolean serviceUp) {
            this.serviceUp = serviceUp;
        }
    }

    public static void main(String[] args) throws Exception {
        ServiceWithHealthIndicator requiredServiceA = new ServiceWithHealthIndicator("requiredServiceA");
        ServiceWithHealthIndicator requiredServiceB = new ServiceWithHealthIndicator("requiredServiceB");

        ServiceWithHealthIndicator optionalServiceA = new ServiceWithHealthIndicator("optionalServiceA");
        ServiceWithHealthIndicator optionalServiceB = new ServiceWithHealthIndicator("optionalServiceB");

        AtomicBoolean strictMode = new AtomicBoolean(true);

        ApplicationHealthIndicator applicationHealth = new ApplicationHealthIndicator(
                Arrays.asList(requiredServiceA, requiredServiceB),
                Arrays.asList(optionalServiceA, optionalServiceB),
                strictMode::get
        );

        // All services are marked down initially, and strict mode is enabled
        System.out.println("All down; status = " + applicationHealth.check().get());

        // Enable required services
        requiredServiceA.setServiceUp(true);
        requiredServiceB.setServiceUp(true);
        System.out.println("Required up, strict mode enabled; status = " + applicationHealth.check().get());

        // Disable strict mode
        strictMode.set(false);
        System.out.println("Required up, strict mode disabled; status = " + applicationHealth.check().get());
    }
}
