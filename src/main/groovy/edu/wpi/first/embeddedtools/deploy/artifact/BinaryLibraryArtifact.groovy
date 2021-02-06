package edu.wpi.first.embeddedtools.deploy.artifact

import groovy.transform.CompileStatic
import edu.wpi.first.embeddedtools.Resolver
import edu.wpi.first.embeddedtools.deploy.cache.CacheMethod
import edu.wpi.first.embeddedtools.deploy.context.DeployContext
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.nativeplatform.NativeBinarySpec
import edu.wpi.first.embeddedtools.ActionWrapper

import javax.inject.Inject

@CompileStatic
class BinaryLibraryArtifact extends AbstractArtifact implements CacheableArtifact {
    Set<File> files
    boolean doDeploy = false

    @Inject
    BinaryLibraryArtifact(String name, Project project) {
        super(name, project)

        preWorkerThread << new ActionWrapper({

            def libs = binary.libs.collect { it.getRuntimeFiles() }
            if (libs.size() != 0) {
                def collection = libs.inject { a,b -> a + b } as FileCollection
                files = collection.files
                doDeploy = true
            }
        })
    }

    NativeBinarySpec binary

    Object cache = "md5sum"
    Resolver<CacheMethod> cacheResolver

    @Override
    void deploy(DeployContext context) {
        if (doDeploy)
            context.put(files, cacheResolver?.resolve(cache))
        else
            context.logger?.log("No file(s) provided for ${toString()}")
    }
}
