package edu.wpi.first.deployutils.deploy.target.location;

import java.util.Optional;

import javax.inject.Inject;

import org.apache.sshd.client.session.ClientSession;
import org.gradle.api.Action;

import edu.wpi.first.deployutils.deploy.target.RemoteTarget;
import edu.wpi.first.deployutils.deploy.target.discovery.action.DiscoveryAction;
import edu.wpi.first.deployutils.deploy.target.discovery.action.DryDiscoveryAction;
import edu.wpi.first.deployutils.deploy.target.discovery.action.SshDiscoveryAction;

public class SshDeployLocation extends AbstractDeployLocation {
    private String address = null;

    private boolean ipv6 = false;

    private String user = null;
    private String password = "";

    private Optional<Action<ClientSession>> clientSessionConfiguration = Optional.empty();

    public Optional<Action<ClientSession>> getClientSessionConfiguration() {
        return clientSessionConfiguration;
    }

    public void setClientSessionConfiguration(Action<ClientSession> clientSessionConfiguration) {
        this.clientSessionConfiguration = Optional.ofNullable(clientSessionConfiguration);
    }

    @Inject
    public SshDeployLocation(String name, RemoteTarget target) {
        super(name, target);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public boolean isIpv6() {
        return ipv6;
    }

    public void setIpv6(boolean ipv6) {
        this.ipv6 = ipv6;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public DiscoveryAction createAction() {
        if (address == null || user == null) {
            throw new IllegalArgumentException("Address and User must not be null for SshDeployLocation");
        }
        if (getTarget().isDry()) {
            return new DryDiscoveryAction(this);
        }
        return new SshDiscoveryAction(this);
    }

    @Override
    public String friendlyString() {
        return getUser() + " @ " + getAddress();
    }

    @Override
    public String toString() {
        return "SshDeployLocation[" + friendlyString() + "]";
    }

}
