package edu.wpi.first.deployutils.deploy.artifact

import org.gradle.api.provider.Property;

class ArtifactDeployWorkerWrapper extends ArtifactDeployWorker {

    private static class ParametersMock implements ArtifactDeployParameters {
        private Property<Integer> provider;

        public ParametersMock(Property<Integer> index) {
            provider = index
        }

        Property<Integer> getIndex() {
            return provider
        }
    }

    private ArtifactDeployParameters parameters;

    ArtifactDeployWorkerWrapper() {

    }

    ArtifactDeployWorkerWrapper(Property<Integer> index) {
        parameters = new ParametersMock(index)
    }

    @Override
    ArtifactDeployParameters getParameters() {
        return parameters
    }
}
