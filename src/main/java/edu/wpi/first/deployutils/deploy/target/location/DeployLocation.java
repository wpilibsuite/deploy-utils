package edu.wpi.first.deployutils.deploy.target.location;

import edu.wpi.first.deployutils.deploy.target.RemoteTarget;
import edu.wpi.first.deployutils.deploy.target.discovery.action.DiscoveryAction;

public interface DeployLocation {
    DiscoveryAction createAction();

    RemoteTarget getTarget();

    String friendlyString();
}
