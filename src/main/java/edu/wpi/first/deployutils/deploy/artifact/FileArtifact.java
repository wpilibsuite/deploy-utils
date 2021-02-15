package edu.wpi.first.deployutils.deploy.artifact;

import java.io.File;

import javax.inject.Inject;

import org.gradle.api.Project;
import org.gradle.api.provider.Property;

import edu.wpi.first.deployutils.Resolver;
import edu.wpi.first.deployutils.deploy.cache.CacheMethod;
import edu.wpi.first.deployutils.deploy.context.DeployContext;
import edu.wpi.first.deployutils.log.ETLogger;

public class FileArtifact extends AbstractArtifact implements CacheableArtifact {

    @Inject
    public FileArtifact(String name, Project project) {
        super(name, project);

        file = project.getObjects().property(File.class);
    }

    private final Property<File> file;

    public Property<File> getFile() {
        return file;
    }

    private String filename = null;

    private Object cache = "md5sum";

    private Resolver<CacheMethod> cacheResolver;

    @Override
    public void deploy(DeployContext context) {
        if (file.isPresent()) {
            File f = file.get();
            context.put(f, (filename == null ? f.getName() : filename), cacheResolver != null ? cacheResolver.resolve(cache) : null);
        } else {
            ETLogger logger = context.getLogger();
            if (logger != null) {
                logger.log("No file provided for " + toString());
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

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }

    @Override
    public void setCacheResolver(Resolver<CacheMethod> resolver) {
        this.cacheResolver = resolver;
    }

    public Resolver<CacheMethod> getCacheResolver() {
        return cacheResolver;
    }

}
