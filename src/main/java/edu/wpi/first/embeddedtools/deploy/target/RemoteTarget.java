package edu.wpi.first.embeddedtools.deploy.target;

import java.util.function.Function;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.gradle.api.Action;
import org.gradle.api.DomainObjectCollection;
import org.gradle.api.Named;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskCollection;

import edu.wpi.first.embeddedtools.deploy.context.DeployContext;
import edu.wpi.first.embeddedtools.deploy.target.discovery.TargetDiscoveryTask;
import edu.wpi.first.embeddedtools.deploy.target.location.DeployLocation;
import edu.wpi.first.embeddedtools.deploy.target.location.DeployLocationSet;

public class RemoteTarget implements Named {
    private Logger log;
    private final String name;
    private final Project project;

    @Inject
    public RemoteTarget(String name, Project project) {
        this.name = name;
        this.project = project;
        this.dry = project.hasProperty("deploy-dry");
        locations = project.getObjects().newInstance(DeployLocationSet.class, project, this);
        log = Logger.getLogger(toString());
    }

    private String directory = null;

    public String getDirectory() {
        return directory;
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

    private DeployLocationSet locations;

    public DeployLocationSet getLocations() {
        return locations;
    }

    public void setLocations(DeployLocationSet locations) {
        this.locations = locations;
    }

    private Function<DeployContext, Boolean> onlyIf = null;;

    public Function<DeployContext, Boolean> getOnlyIf() {
        return onlyIf;
    }

    public void setOnlyIf(Function<DeployContext, Boolean> onlyIf) {
        this.onlyIf = onlyIf;
    }

    @Override
    public String getName() {
        return name;
    }

    public Project getProject() {
        return project;
    }

    public void locations(final Action<DomainObjectCollection<? extends DeployLocation>> action) {
        action.execute(locations);
    }

    public TaskCollection<TargetDiscoveryTask> getDiscoveryTask() {
        return project.getTasks().withType(TargetDiscoveryTask.class).matching(t -> {
            return t.getTarget() == this;
        });
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
        boolean toConnect = onlyIf.apply(ctx);
        if (!toConnect) {
            log.debug("OnlyIf check failed! Not connecting...");
            return false;
        }
        return true;
    }

}
