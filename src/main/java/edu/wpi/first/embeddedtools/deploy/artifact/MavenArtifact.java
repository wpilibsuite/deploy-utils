package edu.wpi.first.embeddedtools.deploy.artifact;

import java.io.File;
import java.util.Set;

import javax.inject.Inject;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;

import edu.wpi.first.embeddedtools.deploy.context.DeployContext;
import edu.wpi.first.embeddedtools.log.ETLogger;

public class MavenArtifact extends FileArtifact {

    private Set<File> deployFiles;

    @Inject
    public MavenArtifact(String name, Project project) {
        super(name, project);

        getPreWorkerThread().add(x -> {
            if (configuration == null || dependency == null) return;
            deployFiles = configuration.files(dependency);
        });
    }

    private Dependency dependency;
    private Configuration configuration;

    public Set<File> getDeployFiles() {
        return deployFiles;
    }

    public void setDeployFiles(Set<File> deployFiles) {
        this.deployFiles = deployFiles;
    }

    public Dependency getDependency() {
        return dependency;
    }

    public void setDependency(Dependency dependency) {
        this.dependency = dependency;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void deploy(DeployContext ctx) {
        if (configuration == null || dependency == null) {
            ETLogger logger = ctx.getLogger();
            if (logger != null) {
                logger.log("No configuration or dependency set");
            }
            return;
        }
        Set<File> files = deployFiles;
        if (files.size() == 1) {
            File f = files.iterator().next();
            getFile().set(f);
            super.deploy(ctx);
        } else {
            ETLogger logger = ctx.getLogger();
            if (logger != null) {
                logger.log("Incorrect number of files found for " + dependency.toString());
            }
        }
    }
}
