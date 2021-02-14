package edu.wpi.first.embeddedtools.deploy.artifact;

import javax.inject.Inject;

import org.gradle.api.Project;

import edu.wpi.first.embeddedtools.deploy.CommandDeployResult;
import edu.wpi.first.embeddedtools.deploy.context.DeployContext;

public class CommandArtifact extends AbstractArtifact {

    private String command = null;
    private CommandDeployResult result = null;

    @Inject
    public CommandArtifact(String name, Project project) {
        super(name, project);
    }

    @Override
    public void deploy(DeployContext context) {
        this.result = context.execute(command);
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public CommandDeployResult getResult() {
        return result;
    }

}
