package edu.wpi.first.embeddedtools.deploy.artifact;

import org.gradle.api.provider.Property;
import org.gradle.workers.WorkParameters;

public interface ArtifactDeployParameters extends WorkParameters {
    Property<Integer> getIndex();
}
