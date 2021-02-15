package edu.wpi.first.deployutils.deploy.artifact;

import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.nativeplatform.NativeBinarySpec;

import edu.wpi.first.deployutils.Resolver;
import edu.wpi.first.deployutils.deploy.cache.CacheMethod;
import edu.wpi.first.deployutils.deploy.context.DeployContext;
import edu.wpi.first.deployutils.log.ETLogger;

import java.io.File;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

public class BinaryLibraryArtifact extends AbstractArtifact implements CacheableArtifact {
    private Set<File> files;
    private boolean doDeploy = false;

    private NativeBinarySpec binary;

    private Object cache = "md5sum";
    private Resolver<CacheMethod> cacheResolver;

    @Inject
    public BinaryLibraryArtifact(String name, Project project) {
        super(name, project);

        getPreWorkerThread().add(v -> {
            Optional<FileCollection> libs = binary.getLibs().stream().map(x -> x.getRuntimeFiles()).reduce((a, b) -> a.plus(b));
            if (libs.isPresent()) {
                files = libs.get().getFiles();
                doDeploy = true;
            }
        });
    }

    public NativeBinarySpec getBinary() {
        return binary;
    }

    public void setBinary(NativeBinarySpec binary) {
        this.binary = binary;
    }

    public boolean isDoDeploy() {
        return doDeploy;
    }

    public void setDoDeploy(boolean doDeploy) {
        this.doDeploy = doDeploy;
    }

    public Set<File> getFiles() {
        return files;
    }

    public void setFiles(Set<File> files) {
        this.files = files;
    }

    @Override
    public void deploy(DeployContext context) {
        if (doDeploy) {
            context.put(files, cacheResolver != null ? cacheResolver.resolve(cache) : null);
        } else {
            ETLogger logger = context.getLogger();
            if (logger != null) {
                logger.log("No file(s) provided for " + toString());
            }
        }
    }

    @Override
    public Object getCache() {
        return cache;
    }

    @Override
    public void setCache(Object cacheMethod) {
        this.cache = cacheMethod;
    }

    @Override
    public void setCacheResolver(Resolver<CacheMethod> resolver) {
        this.cacheResolver = resolver;
    }

    public Resolver<CacheMethod> getCacheResolver() {
        return this.cacheResolver;
    }
}
