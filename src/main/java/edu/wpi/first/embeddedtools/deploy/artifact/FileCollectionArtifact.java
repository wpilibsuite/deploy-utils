package edu.wpi.first.embeddedtools.deploy.artifact;

import edu.wpi.first.embeddedtools.Resolver;
import edu.wpi.first.embeddedtools.deploy.cache.CacheMethod;
import edu.wpi.first.embeddedtools.deploy.context.DeployContext;
import edu.wpi.first.embeddedtools.log.ETLogger;

import org.gradle.api.Project;
import org.gradle.api.file.ConfigurableFileCollection;

import javax.inject.Inject;

class FileCollectionArtifact extends AbstractArtifact implements CacheableArtifact {

    @Inject
    public FileCollectionArtifact(String name, Project project) {
        super(name, project);
        files = project.getObjects().fileCollection();
    }

    private final ConfigurableFileCollection files;

    public ConfigurableFileCollection getFiles() {
        return files;
    }

    Object cache = "md5sum";
    Resolver<CacheMethod> cacheResolver;

    @Override
    public void deploy(DeployContext context) {
        // TODO see if we need to specially handle file trees now
        if (!files.isEmpty())
            context.put(files.getFiles(), cacheResolver != null ? cacheResolver.resolve(cache) : null);
        else {
            ETLogger logger = context.getLogger();
            if (logger != null) {
                logger.log("No file(s) provided for " + toString());
            }
        }
    }

    @Override
    public void setCache(Object cacheMethod) {
        this.cache = cacheMethod;
    }

    @Override
    public Object getCache() {
        return this.cache;
    }

    @Override
    public void setCacheResolver(Resolver<CacheMethod> resolver) {
        this.cacheResolver = resolver;
    }

    public Resolver<CacheMethod> getCacheResolver() {
        return cacheResolver;
    }
}
