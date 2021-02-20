package edu.wpi.first.deployutils.deploy.target;

import java.util.function.Predicate;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.gradle.api.Action;
import org.gradle.api.DomainObjectCollection;
import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer;
import org.gradle.api.Named;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.TaskProvider;

import edu.wpi.first.deployutils.DeployUtils;
import edu.wpi.first.deployutils.deploy.DeployExtension;
import edu.wpi.first.deployutils.deploy.artifact.Artifact;
import edu.wpi.first.deployutils.deploy.artifact.ArtifactDeployTask;
import edu.wpi.first.deployutils.deploy.context.DeployContext;
import edu.wpi.first.deployutils.deploy.target.discovery.TargetDiscoveryTask;
import edu.wpi.first.deployutils.deploy.target.location.DeployLocation;

public class RemoteTarget implements Named {
    private final Logger log;
    private final String name;
    private final Project project;
    private final TaskProvider<Task> deployTask;
    private final TaskProvider<TargetDiscoveryTask> targetDiscoveryTask;
    private final Property<String> targetPlatform;
    private final ExtensiblePolymorphicDomainObjectContainer<Artifact> artifacts;
    private final ExtensiblePolymorphicDomainObjectContainer<DeployLocation> locations;

    public ExtensiblePolymorphicDomainObjectContainer<DeployLocation> getLocations() {
        return locations;
    }

    public ExtensiblePolymorphicDomainObjectContainer<Artifact> getArtifacts() {
        return artifacts;
    }

    @Inject
    public RemoteTarget(String name, Project project, DeployExtension de) {
        this.name = name;
        this.project = project;
        targetPlatform = project.getObjects().property(String.class);
        artifacts = project.getObjects().polymorphicDomainObjectContainer(Artifact.class);
        this.dry = DeployUtils.isDryRun(project);
        locations = project.getObjects().polymorphicDomainObjectContainer(DeployLocation.class);
        log = Logger.getLogger(toString());
        deployTask = project.getTasks().register("deploy" + name, task -> {
            task.setGroup("DeployUtils");
            task.setDescription("Deploy task for " + name);
        });
        targetDiscoveryTask = project.getTasks().register("discover" + name, TargetDiscoveryTask.class, task -> {
            task.setGroup("DeployUtils");
            task.setDescription("Determine the address(es) of target " + name);
            task.setTarget(this);
        });
        de.configureTargetTypes(artifacts, locations, this);
    }

    public Property<String> getTargetPlatform() {
        return targetPlatform;
    }

    public TaskProvider<Task> getDeployTask() {
        return deployTask;
    }

    public TaskProvider<TargetDiscoveryTask> getTargetDiscoveryTask() {
        return targetDiscoveryTask;
    }

    private String directory = null;

    public String getDirectory() {
        return directory;
    }

    public void artifactAdded(Artifact artifact, TaskProvider<ArtifactDeployTask> task) {
        deployTask.configure(x -> x.dependsOn(task));
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    private int timeout = 3;

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    private boolean failOnMissing = true;

    public boolean isFailOnMissing() {
        return failOnMissing;
    }

    public void setFailOnMissing(boolean failOnMissing) {
        this.failOnMissing = failOnMissing;
    }

    private int maxChannels = 1;

    public int getMaxChannels() {
        return maxChannels;
    }

    public void setMaxChannels(int maxChannels) {
        this.maxChannels = maxChannels;
    }

    private boolean dry = false;

    public boolean isDry() {
        return dry;
    }

    public void setDry(boolean dry) {
        this.dry = dry;
    }

    private Predicate<DeployContext> onlyIf = null;;

    public Predicate<DeployContext> getOnlyIf() {
        return onlyIf;
    }

    public void setOnlyIf(Predicate<DeployContext> onlyIf) {
        this.onlyIf = onlyIf;
    }

    @Override
    public String getName() {
        return name;
    }

    public Project getProject() {
        return project;
    }

    @Override
    public String toString() {
        return "RemoteTarget[" + name + "]";
    }

    public boolean verify(DeployContext ctx) {
        if (onlyIf == null) {
            return true;
        }

        log.debug("OnlyIf...");
        boolean toConnect = onlyIf.test(ctx);
        if (!toConnect) {
            log.debug("OnlyIf check failed! Not connecting...");
            return false;
        }
        return true;
    }

}
