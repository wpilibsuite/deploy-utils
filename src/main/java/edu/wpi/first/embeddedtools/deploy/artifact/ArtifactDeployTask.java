package edu.wpi.first.embeddedtools.deploy.artifact;

import java.util.Set;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.Task;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;
import org.gradle.workers.WorkerExecutor;

import edu.wpi.first.embeddedtools.deploy.target.RemoteTarget;
import edu.wpi.first.embeddedtools.deploy.target.discovery.TargetDiscoveryTask;

public class ArtifactDeployTask extends DefaultTask {
    private final WorkerExecutor workerExecutor;
    private Artifact artifact;
    private RemoteTarget target;

    @Internal
    public WorkerExecutor getWorkerExecutor() {
        return workerExecutor;
    }

    @Input
    public RemoteTarget getTarget() {
        return target;
    }

    public void setTarget(RemoteTarget target) {
        this.target = target;
    }

    @Input
    public Artifact getArtifact() {
        return artifact;
    }


    public void setArtifact(Artifact artifact) {
        this.artifact = artifact;
    }

    @Inject
    public ArtifactDeployTask(WorkerExecutor workerExecutor) {
        this.workerExecutor = workerExecutor;
    }

    @TaskAction
    public void deployArtifact() {
        Logger log = Logging.getLogger(toString());
        Set<? extends Task> deps = getTaskDependencies().getDependencies(this);

        log.debug("Deploying artifact " + artifact.getName() + " for target " + target.getName());

        if (artifact instanceof TaskHungryArtifact) {
            log.debug("Artifact is task hungry");
            ((TaskHungryArtifact)artifact).taskDependenciesAvailable(deps);
        }

        Stream<TargetDiscoveryTask> targetTasks = deps.stream().filter(x -> {
            if (!(x instanceof TargetDiscoveryTask)) return false;
            TargetDiscoveryTask t = (TargetDiscoveryTask)x;
            return t.available() && t.getTarget().equals(target);
        }).map(x -> (TargetDiscoveryTask)x);

        for (Action<Artifact> toExecute : artifact.getPreWorkerThread()) {
            toExecute.execute(artifact);
        }

        targetTasks.forEach(discover -> {
            int index = ArtifactDeployWorker.submitStorage(discover.activeContext(), artifact);
            workerExecutor.noIsolation().submit(ArtifactDeployWorker.class, config -> {
                config.getIndex().set(index);
            });
        });
        log.debug("Workers submitted...");
    }

}
