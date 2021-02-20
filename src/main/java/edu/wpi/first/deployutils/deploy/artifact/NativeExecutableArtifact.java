package edu.wpi.first.deployutils.deploy.artifact;

import java.io.File;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import org.gradle.api.Task;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.util.PatternFilterable;
import org.gradle.api.tasks.util.PatternSet;
import org.gradle.nativeplatform.NativeExecutableBinarySpec;
import org.gradle.nativeplatform.tasks.InstallExecutable;

import edu.wpi.first.deployutils.deploy.cache.CacheMethod;
import edu.wpi.first.deployutils.deploy.context.DeployContext;
import edu.wpi.first.deployutils.deploy.target.RemoteTarget;

public class NativeExecutableArtifact extends AbstractArtifact implements CacheableArtifact {

    private final Property<CacheMethod> cacheMethod;

    @Inject
    public NativeExecutableArtifact(String name, RemoteTarget target) {
        super(name, target);
        libraryDirectory = target.getProject().getObjects().property(String.class);
        filename = target.getProject().getObjects().property(String.class);
        executable = target.getProject().getObjects().property(NativeExecutableBinarySpec.class);
        cacheMethod = target.getProject().getObjects().property(CacheMethod.class);

        installTaskProvider = target.getProject().getProviders().provider(() -> {
            return (InstallExecutable)executable.get().getTasks().getInstall();
        });

        dependsOn(installTaskProvider);
    }

    @Override
    public Property<CacheMethod> getCacheMethod() {
        return cacheMethod;
    }

    private boolean deployLibraries = true;
    private final Property<String> libraryDirectory;

    private final Property<String> filename;

    public Property<String> getFilename() {
        return filename;
    }

    private Property<NativeExecutableBinarySpec> executable;

    public Property<NativeExecutableBinarySpec> getExecutable() {
        return executable;
    }

    public boolean isDeployLibraries() {
        return deployLibraries;
    }

    public void setDeployLibraries(boolean deployLibraries) {
        this.deployLibraries = deployLibraries;
    }

    public Property<String> getLibraryDirectory() {
        return libraryDirectory;
    }

    private final PatternFilterable libraryFilter = new PatternSet();

    public PatternFilterable getLibraryFilter() {
        return libraryFilter;
    }

    private final Provider<InstallExecutable> installTaskProvider;

    public Provider<InstallExecutable> getInstallTaskProvider() {
        return installTaskProvider;
    }

    protected File getDeployedFile() {
        InstallExecutable install = (InstallExecutable)executable.get().getTasks().getInstall();
        return install.getExecutableFile().get().getAsFile();
    }

    @Override
    public void deploy(DeployContext context) {
        InstallExecutable install = (InstallExecutable)executable.get().getTasks().getInstall();

        CacheMethod cm = cacheMethod.getOrElse(null);

        File exeFile = getDeployedFile();
        context.put(exeFile, getFilename().getOrElse(exeFile.getName()), cm);

        if (deployLibraries) {
            DeployContext libCtx = context;
            if (libraryDirectory.isPresent()) {
                libCtx = context.subContext(libraryDirectory.get());
            }
            var libFiles = install.getLibs().getAsFileTree().matching(libraryFilter).getFiles();
            libCtx.put(libFiles, cm);
        }
    }
}
