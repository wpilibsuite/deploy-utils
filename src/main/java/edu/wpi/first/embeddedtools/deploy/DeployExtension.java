package edu.wpi.first.embeddedtools.deploy;

import java.util.concurrent.Callable;

import javax.inject.Inject;

import org.codehaus.groovy.runtime.StringGroovyMethods;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectCollection;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.TaskCollection;

import edu.wpi.first.embeddedtools.deploy.artifact.Artifact;
import edu.wpi.first.embeddedtools.deploy.artifact.ArtifactDeployTask;
import edu.wpi.first.embeddedtools.deploy.artifact.ArtifactsExtension;
import edu.wpi.first.embeddedtools.deploy.artifact.CacheableArtifact;
import edu.wpi.first.embeddedtools.deploy.cache.CacheExtension;
import edu.wpi.first.embeddedtools.deploy.cache.CacheMethod;
import edu.wpi.first.embeddedtools.deploy.target.RemoteTarget;
import edu.wpi.first.embeddedtools.deploy.target.TargetsExtension;
import edu.wpi.first.embeddedtools.deploy.target.discovery.TargetDiscoveryTask;

public class DeployExtension {
    private final TargetsExtension targets;
    private final ArtifactsExtension artifacts;
    private final CacheExtension cache;

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

    @Inject
    public DeployExtension(Project project) {
        this.project = project;
        targets = ((ExtensionAware)this).getExtensions().create("targets", TargetsExtension.class, project);
        artifacts = ((ExtensionAware)this).getExtensions().create("artifacts", ArtifactsExtension.class, project);
        cache = ((ExtensionAware)this).getExtensions().create("cache", CacheExtension.class, project);

        this.targets.all(target -> {
            String targetName = StringGroovyMethods.capitalize((CharSequence)target.getName());
            project.getTasks().register("discover" + targetName, TargetDiscoveryTask.class, task -> {
                task.setGroup("EmbeddedTools");
                task.setDescription("Determine the address(es) of target " + targetName);
                task.setTarget(target);
            });
        });

        this.artifacts.all(artifact -> {
            if (artifact instanceof CacheableArtifact) {
                ((CacheableArtifact)artifact).setCacheResolver(this.getCache());
            }

            artifact.getTargets().all(tObj -> {
                RemoteTarget target = this.getTargets().resolve(tObj);
                String artifactName = StringGroovyMethods.capitalize((CharSequence)artifact.getName());
                String targetName = StringGroovyMethods.capitalize((CharSequence)target.getName());
                project.getTasks().register("deploy" + artifactName + targetName, ArtifactDeployTask.class, task -> {
                    task.setArtifact(artifact);
                    task.setTarget(target);
                    task.setGroup("EmbeddedTools");

                    task.dependsOn(new Callable<TaskCollection<TargetDiscoveryTask>>() {
                        @Override
                        public TaskCollection<TargetDiscoveryTask> call() {
                            return project.getTasks().withType(TargetDiscoveryTask.class).matching(new Spec<TargetDiscoveryTask>() {
                                @Override
                                public boolean isSatisfiedBy(TargetDiscoveryTask t) {
                                    return t.getTarget().equals(target);
                                }
                            });
                        }
                    });
                    task.dependsOn(artifact.getDependencies());
                });
            });
        });

        project.getTasks().register("deploy", task -> {
            task.setGroup("EmbeddedTools");
            task.setDescription("Deploy all artifacts on all targets");
            task.dependsOn(new Callable<TaskCollection<ArtifactDeployTask>>() {
                @Override
                public TaskCollection<ArtifactDeployTask> call() {
                    return project.getTasks().withType(ArtifactDeployTask.class).matching(new Spec<ArtifactDeployTask>() {
                        @Override
                        public boolean isSatisfiedBy(ArtifactDeployTask t) {
                            return !t.getArtifact().isExplicit();
                        }
                    });
                }
            });
            // project.getTasks().withType(ArtifactDeployTask.class).all(task2 -> {
            //     task.dependsOn(task2);
            // });
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
