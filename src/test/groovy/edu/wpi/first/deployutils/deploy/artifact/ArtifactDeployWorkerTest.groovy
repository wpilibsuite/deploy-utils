package edu.wpi.first.deployutils.deploy.artifact

import edu.wpi.first.deployutils.deploy.context.DeployContext
import edu.wpi.first.deployutils.log.ETLogger
import org.gradle.api.provider.Property
import spock.lang.Specification

class ArtifactDeployWorkerTest extends Specification {

    def subctx = Mock(DeployContext) {
        getLogger() >> Mock(ETLogger)
    }
    def context = Mock(DeployContext) {
        subContext(_) >> subctx
    }
    def enabledArtifact = Mock(Artifact) {
        isEnabled(_) >> true
    }
    def disabledArtifact = Mock(Artifact) {
        isEnabled(_) >> false
    }

    def "runs deploy enabled"() {
        def worker = new ArtifactDeployWorkerWrapper()

        when:
        worker.run(context, enabledArtifact)

        // We should only call runDeploy with the correct subcontext
        then:
        1 * enabledArtifact.deploy(subctx)
        0 * enabledArtifact.deploy(_)
    }

    def "runs skipped disabled"() {
        def worker = new ArtifactDeployWorkerWrapper()

        when:
        worker.run(context, disabledArtifact)

        then:
        0 * disabledArtifact.deploy(_)
    }

    def "storage"() {
        ArtifactDeployWorker.clearStorage()

        // Check that it gets inserted
        def hc = 0
        when:
        hc = ArtifactDeployWorker.submitStorage(context, enabledArtifact)
        then:
        ArtifactDeployWorker.storageCount() == 1

        // Check that, after construction, it is removed from that map
        // and its attributes match
        when:
        def property = Stub(Property) {
            get() >> hc
        }
        def worker = new ArtifactDeployWorkerWrapper(property)
        worker.execute()
        then:
        ArtifactDeployWorker.storageCount() == 0
    }

}
