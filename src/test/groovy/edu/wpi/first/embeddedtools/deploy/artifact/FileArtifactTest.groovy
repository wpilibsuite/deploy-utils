package edu.wpi.first.embeddedtools.deploy.artifact

import edu.wpi.first.embeddedtools.Resolver
import edu.wpi.first.embeddedtools.deploy.cache.CacheMethod
import edu.wpi.first.embeddedtools.deploy.context.DeployContext

class FileArtifactTest extends AbstractArtifactTestSpec {

    FileArtifact artifact
    def ctx = Mock(DeployContext)

    def setup() {
        artifact = new FileArtifact(name, project)
    }

    def "deploy (no file)"() {

        when:
        artifact.deploy(ctx)
        then:
        0* ctx.put(_, _, _)
    }

    def "deploy (no filename)"() {
        def file = Mock(File) {
            getName() >> "filename"
        }

        artifact.file.set(file)

        when:
        artifact.deploy(ctx)
        then:
        1 * ctx.put(file, file.getName(), null)
        0 * ctx.put(_, _, _)
    }

    def "deploy (filename)"() {
        def file = Mock(File) {
            getName() >> "filename"
        }

        artifact.file.set(file)
        artifact.setFilename("othername")

        when:
        artifact.deploy(ctx)
        then:
        1 * ctx.put(file, "othername", null)
        0 * ctx.put(_, _, _)
    }

    def "deploy cache"() {
        def file = Mock(File)
        def cache = Mock(CacheMethod)
        def resolver = Mock(Resolver) {
            resolve(_) >> cache
        }

        artifact.setCacheResolver(resolver)
        artifact.file.set(file)

        when:
        artifact.deploy(ctx)
        then:
        1 * ctx.put(file, file.getName(), cache)
        0 * ctx.put(_, _, _)
    }
}
