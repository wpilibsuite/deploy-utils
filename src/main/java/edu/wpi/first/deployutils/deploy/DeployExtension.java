package edu.wpi.first.deployutils.deploy;

import javax.inject.Inject;

import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectCollection;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.tasks.TaskProvider;

import edu.wpi.first.deployutils.deploy.artifact.Artifact;
import edu.wpi.first.deployutils.deploy.artifact.ArtifactsExtension;
import edu.wpi.first.deployutils.deploy.artifact.CacheableArtifact;
import edu.wpi.first.deployutils.deploy.cache.CacheExtension;
import edu.wpi.first.deployutils.deploy.cache.CacheMethod;
import edu.wpi.first.deployutils.deploy.target.RemoteTarget;
import edu.wpi.first.deployutils.deploy.target.TargetsExtension;

public class DeployExtension {
    private final TargetsExtension targets;
    private final ArtifactsExtension artifacts;
    private final CacheExtension cache;
    private final TaskProvider<Task> deployTask;

    private final Project project;

    public TargetsExtension getTargets() {
        return targets;
    }

    public ArtifactsExtension getArtifacts() {
        return artifacts;
    }

    public CacheExtension getCache() {
        return cache;
    }

    public Project getProject() {
        return project;
    }

    public TaskProvider<Task> getDeployTask() {
        return deployTask;
    }

    @Inject
    public DeployExtension(Project project) {
        this.project = project;
        targets = ((ExtensionAware)this).getExtensions().create("targets", TargetsExtension.class, project);
        artifacts = ((ExtensionAware)this).getExtensions().create("artifacts", ArtifactsExtension.class, project);
        cache = ((ExtensionAware)this).getExtensions().create("cache", CacheExtension.class, project);

        this.artifacts.withType(CacheableArtifact.class).all(ce -> {
            ce.setCacheResolver(this.getCache());
        });

        deployTask = project.getTasks().register("deploy", task -> {
            task.setGroup("DeployUtils");
            task.setDescription("Deploy all artifacts on all targets");
            targets.all(x -> {
                task.dependsOn(x.getDeployTask());
            });
        });
    }

    public void targets(final Action<NamedDomainObjectCollection<? extends RemoteTarget>> action) {
        action.execute(targets);
    }

    public void artifacts(final Action<NamedDomainObjectCollection<? extends Artifact>> action) {
        action.execute(artifacts);
    }

    public void cache(final Action<NamedDomainObjectCollection<? extends CacheMethod>> action) {
        action.execute(cache);
    }
}
