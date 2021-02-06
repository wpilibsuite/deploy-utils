package edu.wpi.first.embeddedtools.deploy.target.discovery;

import java.util.function.Consumer;

import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;
import org.gradle.workers.WorkerExecutor;

import edu.wpi.first.embeddedtools.deploy.context.DeployContext;
import edu.wpi.first.embeddedtools.deploy.target.RemoteTarget;
import edu.wpi.first.embeddedtools.log.ETLogger;
import edu.wpi.first.embeddedtools.log.ETLoggerFactory;

public class TargetDiscoveryTask extends DefaultTask implements Consumer<DeployContext> {

    private final WorkerExecutor workerExecutor;

    @Internal
    public final WorkerExecutor getWorkerExecutor() {
        return workerExecutor;
    }

    private DeployContext activeContext;

    private RemoteTarget target;

    public void setTarget(RemoteTarget target) {
        this.target = target;
    }

    @Input
    public RemoteTarget getTarget() {
        return target;
    }


    @Inject
    public TargetDiscoveryTask(WorkerExecutor workerExecutor) {
        this.workerExecutor = workerExecutor;
    }

    public boolean available() {
        return activeContext != null;
    }

    public DeployContext activeContext() {
        if (activeContext != null) {
            return activeContext;
        } else {
            throw new GradleException("Target " + target.getName() + " is not available");
        }
    }

    @Override
    public void accept(DeployContext ctx) {
        this.activeContext = ctx;
    }

    @TaskAction
    public void discoverTarget() {
        ETLogger log = ETLoggerFactory.INSTANCE.create("TargetDiscoveryTask[" + target.getName() + "]");

        log.log("Discovering Target " + target.getName());
        int hashcode = TargetDiscoveryWorker.submitStorage(target, this);

        // We use the Worker API since it allows for multiple of this task to run at the
        // same time. Inside the worker we split off into a threadpool so we can introduce
        // our own timeout logic.
        log.debug("Submitting worker ${hashcode}...");
        workerExecutor.noIsolation().submit(TargetDiscoveryWorker.class, config -> {
            config.getIndex().set(hashcode);
        });
        log.debug("Submitted!");
    }
}
