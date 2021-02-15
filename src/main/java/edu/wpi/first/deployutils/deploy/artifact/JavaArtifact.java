package edu.wpi.first.deployutils.deploy.artifact;

import javax.inject.Inject;

import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.Jar;

public class JavaArtifact extends FileArtifact {

    @Inject
    public JavaArtifact(String name, Project project) {
        super(name, project);
    }

    private boolean isSet = false;

    public void setJarTask(TaskProvider<Jar> jarTask) {
        if (isSet) {
            throw new GradleException("Can not set jar task twice");
        }
        dependsOn(jarTask);
        getFile().set(jarTask.get().getArchiveFile().map(x -> x.getAsFile()));
    }

    public void setJarTask(Jar jarTask) {
        if (isSet) {
            throw new GradleException("Can not set jar task twice");
        }
        dependsOn(jarTask);
        isSet = true;
        getFile().set(jarTask.getArchiveFile().map(x -> x.getAsFile()));
    }
}
