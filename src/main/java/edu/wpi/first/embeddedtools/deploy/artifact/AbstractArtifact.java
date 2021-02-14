package edu.wpi.first.embeddedtools.deploy.artifact;

import java.util.List;
import java.util.function.Predicate;

import javax.inject.Inject;

import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.TaskProvider;

import edu.wpi.first.embeddedtools.deploy.DeployExtension;
import edu.wpi.first.embeddedtools.deploy.context.DeployContext;
import edu.wpi.first.embeddedtools.deploy.target.RemoteTarget;
import edu.wpi.first.embeddedtools.deploy.target.location.DeployLocation;

public abstract class AbstractArtifact implements Artifact {
    private final String name;
    private final Project project;
    private RemoteTarget target;
    private TaskProvider<ArtifactDeployTask> deployTask;

    private boolean disabled = false;
    private boolean explicit = false;

    private final Property<String> directory;
    private List<Action<DeployContext>> predeploy = new WrappedArrayList<>();
    private List<Action<DeployContext>> postdeploy = new WrappedArrayList<>();
    private List<Action<Artifact>> preWorkerThread = new WrappedArrayList<>();
    private Predicate<DeployContext> onlyIf = null;

    @Inject
    public AbstractArtifact(String name, Project project) {
        this.name = name;
        this.project = project;
        directory = project.getObjects().property(String.class);
        directory.set("");
    }

    @Override
    public TaskProvider<ArtifactDeployTask> getDeployTask() {
        return deployTask;
    }

    @Override
    public RemoteTarget getTarget() {
        return target;
    }

    @Override
    public void setTarget(Object tObj) {
        if (this.target != null) {
            throw new GradleException("Can not set target of task twice");
        }
        DeployExtension de = project.getExtensions().getByType(DeployExtension.class);
        RemoteTarget target = de.getTargets().resolve(tObj);
        TaskProvider<ArtifactDeployTask> deployTask = project.getTasks().register("deploy" + name + target.getName(), ArtifactDeployTask.class, task -> {
            task.setArtifact(this);
            task.setTarget(target);
            task.setGroup("EmbeddedTools");
            task.setDescription("Deploys " + name + " to " + target.getName());

            task.dependsOn(target.getTargetDiscoveryTask());

        });
        target.artifactAdded(this, deployTask);
        target.getDeployTask().configure(x -> x.dependsOn(deployTask));
        this.target = target;
        this.deployTask = deployTask;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Project getProject() {
        return project;
    }

    @Override
    public void dependsOn(Object... paths) {
        deployTask.configure(y -> y.dependsOn(paths));
    }

    @Override
    public List<Action<Artifact>> getPreWorkerThread() {
        return preWorkerThread;
    }

    public void setPreWorkerThread(List<Action<Artifact>> preWorkerThread) {
        this.preWorkerThread = preWorkerThread;
    }

    @Override
    public Property<String> getDirectory() {
        return directory;
    }

    @Override
    public List<Action<DeployContext>> getPredeploy() {
        return predeploy;
    }

    @Override
    public List<Action<DeployContext>> getPostdeploy() {
        return postdeploy;
    }

    public Predicate<DeployContext> getOnlyIf() {
        return onlyIf;
    }

    @Override
    public void setOnlyIf(Predicate<DeployContext> action) {
        onlyIf = action;
    }

    @Override
    public boolean isEnabled(DeployContext context) {
        if (disabled) return false;
        if (onlyIf == null) return true;
        if (onlyIf.test(context)) return true;
        if (context != null) {
            DeployLocation loc = context.getDeployLocation();
            if (loc != null) {
                RemoteTarget target = loc.getTarget();
                if (target != null) {
                    return target.isDry();
                }
            }
        }
        return false;
    }

    @Override
    public boolean isDisabled() {
        return disabled;
    }

    @Override
    public void setDisabled() {
        setDisabled(true);
    }

    public void setDisabled(boolean state) {
        this.disabled = state;
    }

    @Override
    public boolean isExplicit() {
        return this.explicit;
    }

    @Override
    public void setExplicit(boolean explicit) {
        this.explicit = explicit;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[" + this.name + "]";
    }
}
