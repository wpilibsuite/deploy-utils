package edu.wpi.first.embeddedtools.deploy.artifact;

import java.util.Set;

import org.gradle.api.Task;

public interface TaskHungryArtifact extends Artifact {
    void taskDependenciesAvailable(Set<? extends Task> tasks);
}
