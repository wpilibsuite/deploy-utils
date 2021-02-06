package edu.wpi.first.embeddedtools.deploy.target.location;

import edu.wpi.first.embeddedtools.deploy.target.RemoteTarget;
import edu.wpi.first.embeddedtools.deploy.target.discovery.action.DiscoveryAction;

public interface DeployLocation {
    DiscoveryAction createAction();

    RemoteTarget getTarget();

    String friendlyString();
}
