package edu.wpi.first.deployutils.deploy;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.language.base.plugins.ComponentModelBasePlugin;

import edu.wpi.first.deployutils.deploy.artifact.ArtifactDeployWorker;
import edu.wpi.first.deployutils.deploy.target.discovery.TargetDiscoveryWorker;

public class DeployPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(ComponentModelBasePlugin.class);

        project.getExtensions().create("deploy", DeployExtension.class, project);

        // TODO Move these onto extensions so this becomes a non issue
        project.getGradle().buildFinished(x -> {
            TargetDiscoveryWorker.clearStorage();
            ArtifactDeployWorker.clearStorage();
        });
    }
}
