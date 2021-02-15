package edu.wpi.first.deployutils.deploy.artifact;

import edu.wpi.first.deployutils.Resolver;
import edu.wpi.first.deployutils.deploy.cache.CacheMethod;

public interface CacheableArtifact extends Artifact {
    Object getCache();
    void setCache(Object cacheMethod);

    void setCacheResolver(Resolver<CacheMethod> resolver);
}
