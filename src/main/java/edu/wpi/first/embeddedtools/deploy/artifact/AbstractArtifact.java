package edu.wpi.first.embeddedtools.deploy.artifact;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import javax.inject.Inject;

import org.gradle.api.Action;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskCollection;

import edu.wpi.first.embeddedtools.deploy.context.DeployContext;
import edu.wpi.first.embeddedtools.deploy.target.RemoteTarget;
import edu.wpi.first.embeddedtools.deploy.target.location.DeployLocation;

public abstract class AbstractArtifact implements Artifact {
    private final String name;
    private final Project project;

    private final DomainObjectSet<Object> dependencies;
    private final DomainObjectSet<Object> targets;

    private boolean disabled = false;
    private boolean explicit = false;

    private String directory = null;
    private List<Action<DeployContext>> predeploy = new WrappedArrayList<>();
    private List<Action<DeployContext>> postdeploy = new WrappedArrayList<>();
    private List<Action<Artifact>> preWorkerThread = new WrappedArrayList<>();
    private Predicate<DeployContext> onlyIf = null;

    @Inject
    public AbstractArtifact(String name, Project project) {
        this.name = name;
        this.project = project;
        this.dependencies = project.getObjects().domainObjectSet(Object.class);
        this.targets = project.getObjects().domainObjectSet(Object.class);
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
        dependencies.addAll(Arrays.asList(paths));
    }

    @Override
    public List<Action<Artifact>> getPreWorkerThread() {
        return preWorkerThread;
    }

    public void setPreWorkerThread(List<Action<Artifact>> preWorkerThread) {
        this.preWorkerThread = preWorkerThread;
    }

    @Override
    public DomainObjectSet<Object> getDependencies() {
        return dependencies;
    }

    @Override
    public DomainObjectSet<Object> getTargets() {
        return targets;
    }

    @Override
    public TaskCollection<ArtifactDeployTask> getTasks() {
        return project.getTasks().withType(ArtifactDeployTask.class).matching(x -> x.getArtifact() == this);
    }

    @Override
    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    @Override
    public List<Action<DeployContext>> getPredeploy() {
        return predeploy;
    }

    public void setPredeploy(List<Action<DeployContext>> predeploy) {
        this.predeploy = predeploy;
    }

    @Override
    public List<Action<DeployContext>> getPostdeploy() {
        return postdeploy;
    }

    public void setPostdeploy(List<Action<DeployContext>> postdeploy) {
        this.postdeploy = postdeploy;
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
