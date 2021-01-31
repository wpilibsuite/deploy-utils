package jaci.gradle.deploy.artifact

import groovy.transform.CompileStatic
import edu.wpi.first.embeddedtools.Resolver
import jaci.gradle.deploy.cache.CacheMethod

@CompileStatic
interface CacheableArtifact extends Artifact {
    Object getCache()
    void setCache(Object cacheMethod)

    void setCacheResolver(Resolver<CacheMethod> resolver)
}
