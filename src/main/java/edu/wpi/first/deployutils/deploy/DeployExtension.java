package edu.wpi.first.deployutils.deploy;

import java.util.concurrent.Callable;

import javax.inject.Inject;

import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.diagnostics.TaskReportTask;

import edu.wpi.first.deployutils.deploy.artifact.Artifact;
import edu.wpi.first.deployutils.deploy.artifact.FileArtifact;
import edu.wpi.first.deployutils.deploy.artifact.JavaArtifact;
import edu.wpi.first.deployutils.deploy.artifact.NativeExecutableArtifact;
import edu.wpi.first.deployutils.deploy.cache.CacheMethod;
import edu.wpi.first.deployutils.deploy.cache.Md5SumCacheMethod;
import edu.wpi.first.deployutils.deploy.target.RemoteTarget;

public class DeployExtension {
    private final TaskProvider<Task> deployTask;
    private final ExtensiblePolymorphicDomainObjectContainer<RemoteTarget> targets;
    private final ExtensiblePolymorphicDomainObjectContainer<CacheMethod> cache;

    public ExtensiblePolymorphicDomainObjectContainer<RemoteTarget> getTargets() {
        return targets;
    }

    public ExtensiblePolymorphicDomainObjectContainer<CacheMethod> getCache() {
        return cache;
    }

    public TaskProvider<Task> getDeployTask() {
        return deployTask;
    }

    public void configureArtifactTypes(ExtensiblePolymorphicDomainObjectContainer<Artifact> artifacts, RemoteTarget target) {
        ObjectFactory objects = target.getProject().getObjects();
        artifacts.registerFactory(NativeExecutableArtifact.class, name -> {
            var art = objects.newInstance(NativeExecutableArtifact.class, name, target);
            art.getCacheMethod().set(cache.getByName("md5sum"));
            return art;
        });
        artifacts.registerFactory(FileArtifact.class, name -> {
            var art = objects.newInstance(FileArtifact.class, name, target);
            art.getCacheMethod().set(cache.getByName("md5sum"));
            return art;
        });
        artifacts.registerFactory(JavaArtifact.class, name -> {
            var art = objects.newInstance(JavaArtifact.class, name, target);
            art.getCacheMethod().set(cache.getByName("md5sum"));
            return art;
        });
    }

    @Inject
    public DeployExtension(Project project, ObjectFactory objects) {

        targets = objects.polymorphicDomainObjectContainer(RemoteTarget.class);
        cache = objects.polymorphicDomainObjectContainer(CacheMethod.class);

        cache.registerFactory(Md5SumCacheMethod.class, name -> {
            return objects.newInstance(Md5SumCacheMethod.class, name);
        });

        cache.register("md5sum", Md5SumCacheMethod.class);

        targets.registerFactory(RemoteTarget.class, name -> {
            return objects.newInstance(RemoteTarget.class, name, project, this);
        });

        // Empty all forces all registered items to be instantly resolved.
        // Without this, any registered tasks will crash when deploy is called
        // Also resolve all inner artifacts for same reason
        targets.all(x -> x.getArtifacts().all(y -> {}));

        deployTask = project.getTasks().register("deploy", task -> {
            task.setGroup("DeployUtils");
            task.setDescription("Deploy all artifacts on all targets");
            targets.all(x -> {
                task.dependsOn(x.getDeployTask());
            });
        });

        project.getTasks().withType(TaskReportTask.class).configureEach(t -> {
            Callable<Task> enumTask = () -> {
                targets.all(x -> x.getArtifacts().all(y -> {}));
                return null;
            };
            t.dependsOn(enumTask);
        });
    }
}
