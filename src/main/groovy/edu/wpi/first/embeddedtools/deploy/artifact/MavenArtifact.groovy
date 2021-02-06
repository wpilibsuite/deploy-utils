package edu.wpi.first.embeddedtools.deploy.artifact

import groovy.transform.CompileStatic
import edu.wpi.first.embeddedtools.Resolver
import edu.wpi.first.embeddedtools.deploy.cache.CacheMethod
import edu.wpi.first.embeddedtools.deploy.context.DeployContext
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import edu.wpi.first.embeddedtools.ActionWrapper

import javax.inject.Inject

@CompileStatic
class MavenArtifact extends FileArtifact {

    Set<File> deployFiles

    @Inject
    MavenArtifact(String name, Project project) {
        super(name, project)

        preWorkerThread << new ActionWrapper({
            if (configuration == null || dependency == null) return
            deployFiles = configuration.files(dependency)
        })
    }

    Dependency dependency
    Configuration configuration

    @Override
    void deploy(DeployContext ctx) {
        if (configuration == null || dependency == null) {
            ctx.logger?.log("No configuration or dependency set")
            return
        }
        def files = deployFiles
        if (files.size() == 1) {
            File f = files.first()
            file.set(f)
            super.deploy(ctx)
        } else {
            ctx.logger?.log("Incorrect number of files found for ${dependency.toString()}")
        }
    }
}
