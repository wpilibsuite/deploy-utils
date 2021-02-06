package edu.wpi.first.embeddedtools.deploy.artifact

import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import edu.wpi.first.embeddedtools.deploy.CommandDeployResult
import edu.wpi.first.embeddedtools.deploy.context.DeployContext

@CompileStatic
@InheritConstructors(constructorAnnotations = true)
class CommandArtifact extends AbstractArtifact {

    String command = null
    CommandDeployResult result = null

    @Override
    void deploy(DeployContext context) {
        this.result = context.execute(command)
    }

}
