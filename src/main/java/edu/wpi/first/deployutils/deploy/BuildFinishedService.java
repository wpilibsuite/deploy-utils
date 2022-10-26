package edu.wpi.first.deployutils.deploy;

import javax.inject.Inject;

import org.gradle.api.services.BuildService;
import org.gradle.api.services.BuildServiceParameters;

import edu.wpi.first.deployutils.deploy.artifact.ArtifactDeployWorker;
import edu.wpi.first.deployutils.deploy.sessions.SessionControllerStore;
import edu.wpi.first.deployutils.deploy.target.discovery.TargetDiscoveryWorker;

public abstract class BuildFinishedService implements BuildService<BuildServiceParameters.None>, AutoCloseable {

    @Inject
    public BuildFinishedService() {
    }

    @Override
    public void close() {
        TargetDiscoveryWorker.clearStorage();
        ArtifactDeployWorker.clearStorage();
        SessionControllerStore.closeAndClearStorage();
    }
}
