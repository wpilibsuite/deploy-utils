package edu.wpi.first.embeddedtools.deploy.artifact

import edu.wpi.first.embeddedtools.Resolver
import edu.wpi.first.embeddedtools.deploy.cache.CacheMethod
import edu.wpi.first.embeddedtools.deploy.context.DeployContext
import org.gradle.api.file.FileCollection

class FileCollectionArtifactTest extends AbstractArtifactTestSpec {

    FileCollectionArtifact artifact
    def ctx = Mock(DeployContext)

    def setup() {
        artifact = new FileCollectionArtifact(name, project)
    }

    def "deploy (no files)"() {

        when:
        artifact.deploy(ctx)
        then:
        0 * ctx.put(_, _)
    }

    def "deploy"() {
        def actualFiles = [Mock(File), Mock(File)] as Set
        def files = Mock(FileCollection) {
            getFiles() >> actualFiles
        }

        artifact.setFiles(files)

        when:
        artifact.deploy(ctx)
        then:
        1 * ctx.put(actualFiles, null)
        0 * ctx.put(_, _)
    }

    def "deploy cache"() {
        def files = Mock(FileCollection)

        def cache = Mock(CacheMethod)
        def resolver = Mock(Resolver) {
            resolve(_) >> cache
        }

        artifact.setCacheResolver(resolver)
        artifact.setFiles(files)

        when:
        artifact.deploy(ctx)
        then:
        1 * ctx.put(_, cache)
        0 * ctx.put(_, _)
    }
}
