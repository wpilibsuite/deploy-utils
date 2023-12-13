package edu.wpi.first.deployutils;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import edu.wpi.first.deployutils.deploy.DeployPlugin;
import edu.wpi.first.deployutils.log.ETLoggerFactory;

public class DeployUtils implements Plugin<Project> {

    @Override
    public void apply(Project project) {

        ETLoggerFactory.INSTANCE.addColorOutput(project);

        project.getPluginManager().apply(DeployPlugin.class);
    }

    public static boolean isDryRun(Project project) {
        return project.hasProperty("deploy-dry");
    }

    public static boolean isSkipCache(Project project) {
        return project.hasProperty("deploy-dirty");
    }
}
