package edu.wpi.first.deployutils.deploy.target.discovery;

import org.gradle.api.provider.Property;
import org.gradle.workers.WorkParameters;

public interface TargetDiscoveryWorkerParameters extends WorkParameters {
    Property<Integer> getIndex();
}
