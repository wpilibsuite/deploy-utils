package edu.wpi.first.deployutils.deploy.artifact;

import org.gradle.api.Action;
import org.gradle.api.Project;

import edu.wpi.first.deployutils.deploy.context.DeployContext;

public class ActionArtifact extends AbstractArtifact {
    private Action<DeployContext> deployAction;

    public ActionArtifact(String name, Project project) {
        super(name, project);
    }

    @Override
    public void deploy(DeployContext context) {
        deployAction.execute(context);
    }

    public Action<DeployContext> getDeployAction() {
        return deployAction;
    }

    public void setDeployAction(Action<DeployContext> deployAction) {
        this.deployAction = deployAction;
    }

}
