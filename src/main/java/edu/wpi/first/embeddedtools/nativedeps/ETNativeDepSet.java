package edu.wpi.first.embeddedtools.nativedeps;

import edu.wpi.first.embeddedtools.files.IDirectoryTree;
import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.nativeplatform.BuildType;
import org.gradle.nativeplatform.Flavor;
import org.gradle.nativeplatform.NativeDependencySet;
import org.gradle.nativeplatform.platform.NativePlatform;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

public class ETNativeDepSet implements NativeDependencySet, SystemLibsDependencySet {

    private Project         project;
    private String          name;
    private boolean         resolvedDebug;
    private FileCollection  linkLibs, dynamicLibs, debugLibs;
    private IDirectoryTree  headers, sources;
    private List<String>    systemLibs;
    private NativePlatform  targetPlatform;
    private Flavor          flavor;
    private BuildType       buildType;

    public Project getProject() {
        return project;
    }

    public String getName() {
        return name;
    }

    public boolean isResolvedDebug() {
        return resolvedDebug;
    }

    public FileCollection getLinkLibs() {
        return linkLibs;
    }

    public FileCollection getDynamicLibs() {
        return dynamicLibs;
    }

    public FileCollection getDebugLibs() {
        return debugLibs;
    }

    public IDirectoryTree getHeaders() {
        return headers;
    }

    public IDirectoryTree getSources() {
        return sources;
    }

    public List<String> getSystemLibs() {
        return systemLibs;
    }

    public NativePlatform getTargetPlatform() {
        return targetPlatform;
    }

    public Flavor getFlavor() {
        return flavor;
    }

    public BuildType getBuildType() {
        return buildType;
    }

    public ETNativeDepSet(Project project, String name,
                          IDirectoryTree headers, IDirectoryTree sources,
                          FileCollection linkLibs, FileCollection dynamicLibs,
                          FileCollection debugLibs, List<String> systemLibs,
                          NativePlatform targetPlatform, Flavor flavor,
                          BuildType buildType) {
        this.project = project;
        this.name = name;

        this.headers = headers;
        this.sources = sources;

        this.linkLibs = linkLibs;
        this.dynamicLibs = dynamicLibs;
        this.debugLibs = debugLibs;
        this.systemLibs = systemLibs;

        this.targetPlatform = targetPlatform;
        this.flavor = flavor;
        this.buildType = buildType;
    }

    @Override
    public FileCollection getIncludeRoots() {
        Callable<Set<File>> cbl = () -> headers.getDirectories();
        return project.files(cbl);
    }

    public FileCollection getSourceRoots() {
        Callable<Set<File>> cbl = () -> sources.getDirectories();
        return project.files(cbl);
    }

    @Override
    public FileCollection getLinkFiles() {
        if (!resolvedDebug) {
            debugLibs.getFiles();
            resolvedDebug = true;
        }
        return linkLibs;
    }

    @Override
    public FileCollection getRuntimeFiles() {
        // Needed to have a flat set, as otherwise the install tasks do not work
        // properly
        Callable<Set<File>> cbl = () -> dynamicLibs.getFiles();
        return project.files(cbl);
    }

    public FileCollection getDebugFiles() {
        return debugLibs;
    }

    public boolean appliesTo(Flavor flav, BuildType btype, NativePlatform plat) {
        if (flavor != null && !flavor.equals(flav))
            return false;
        if (buildType != null && !buildType.equals(btype))
            return false;
        if (targetPlatform == null || !targetPlatform.equals(plat))
            return false;

        return true;
    }

    public boolean appliesTo(String flavorName, String buildTypeName, String platformName) {
        if (flavor != null && !flavor.getName().equals(flavorName))
            return false;
        if (buildType != null && !buildType.getName().equals(buildTypeName))
            return false;
        if (targetPlatform == null || !targetPlatform.getName().equals(platformName))
            return false;

        return true;
    }

    @Override
    public String toString() {
        return "ETNativeDepSet[" + getName() + " F:" + getFlavor() + " BT:" + getBuildType() + " P:" + getTargetPlatform() + "]";
    }
}
