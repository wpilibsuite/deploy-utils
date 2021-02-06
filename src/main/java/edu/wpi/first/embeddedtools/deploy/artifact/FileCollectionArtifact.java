package edu.wpi.first.embeddedtools.deploy.artifact;

import edu.wpi.first.embeddedtools.Resolver;
import edu.wpi.first.embeddedtools.deploy.cache.CacheMethod;
import edu.wpi.first.embeddedtools.deploy.context.DeployContext;
import edu.wpi.first.embeddedtools.log.ETLogger;

import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.Property;

import javax.inject.Inject;

class FileCollectionArtifact extends AbstractArtifact implements CacheableArtifact {

    @Inject
    public FileCollectionArtifact(String name, Project project) {
        super(name, project);
        files = project.getObjects().property(FileCollection.class);
    }

    private final Property<FileCollection> files;

    public Property<FileCollection> getFiles() {
        return files;
    }

    Object cache = "md5sum";
    Resolver<CacheMethod> cacheResolver;

    @Override
    public void deploy(DeployContext context) {
        if (files.isPresent())
            context.put(files.get().getFiles(), cacheResolver != null ? cacheResolver.resolve(cache) : null);
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
