package edu.wpi.first.embeddedtools.deploy.target.location;

import javax.inject.Inject;

import edu.wpi.first.embeddedtools.deploy.target.RemoteTarget;

public abstract class AbstractDeployLocation implements DeployLocation {
    private final RemoteTarget target;

    @Inject
    public AbstractDeployLocation(RemoteTarget target) {
        this.target = target;
    }

    @Override
    public RemoteTarget getTarget() {
        return this.target;
    }
}
