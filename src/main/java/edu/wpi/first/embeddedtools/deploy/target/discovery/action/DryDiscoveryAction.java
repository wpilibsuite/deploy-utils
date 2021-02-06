package edu.wpi.first.embeddedtools.deploy.target.discovery.action;

import edu.wpi.first.embeddedtools.deploy.context.DefaultDeployContext;
import edu.wpi.first.embeddedtools.deploy.context.DeployContext;
import edu.wpi.first.embeddedtools.deploy.sessions.DrySessionController;
import edu.wpi.first.embeddedtools.deploy.target.discovery.DiscoveryState;
import edu.wpi.first.embeddedtools.deploy.target.location.DeployLocation;
import edu.wpi.first.embeddedtools.log.ETLogger;
import edu.wpi.first.embeddedtools.log.ETLoggerFactory;

public class DryDiscoveryAction extends AbstractDiscoveryAction {

    private ETLogger log;

    public DryDiscoveryAction(DeployLocation loc) {
        super(loc);
        this.log = ETLoggerFactory.INSTANCE.create(toString());
    }

    @Override
    public DeployContext discover() {
        DrySessionController controller = new DrySessionController();
        return new DefaultDeployContext(controller, log, getDeployLocation(), getDeployLocation().getTarget().getDirectory());
    }

    @Override
    public DiscoveryState getState() {
        return DiscoveryState.CONNECTED;
    }
}

