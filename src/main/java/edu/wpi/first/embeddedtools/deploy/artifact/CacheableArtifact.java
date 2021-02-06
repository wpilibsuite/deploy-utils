package edu.wpi.first.embeddedtools.deploy.artifact;

import edu.wpi.first.embeddedtools.Resolver;
import edu.wpi.first.embeddedtools.deploy.cache.CacheMethod;

public interface CacheableArtifact extends Artifact {
    Object getCache();
    void setCache(Object cacheMethod);

    void setCacheResolver(Resolver<CacheMethod> resolver);
}
