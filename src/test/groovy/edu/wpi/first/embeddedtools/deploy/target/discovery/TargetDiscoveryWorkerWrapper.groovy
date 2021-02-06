package edu.wpi.first.embeddedtools.deploy.target.discovery

import org.gradle.api.provider.Property;

class TargetDiscoveryWorkerWrapper extends TargetDiscoveryWorker {

    private static class TargetDiscoveryWorkerParametersMock implements TargetDiscoveryWorkerParameters {
        private Property<Integer> provider;

        public TargetDiscoveryWorkerParametersMock(Property<Integer> index) {
            provider = index
        }

        Property<Integer> getIndex() {
            return provider
        }
    }

    private TargetDiscoveryWorkerParameters parameters;

    TargetDiscoveryWorkerWrapper() {

    }

    TargetDiscoveryWorkerWrapper(Property<Integer> index) {
        parameters = new TargetDiscoveryWorkerParametersMock(index)
    }

    @Override
    TargetDiscoveryWorkerParameters getParameters() {
        return parameters
    }
}
