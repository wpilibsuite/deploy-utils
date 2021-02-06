package edu.wpi.first.embeddedtools.deploy.artifact;

import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.nativeplatform.NativeBinarySpec;
import org.gradle.nativeplatform.tasks.AbstractLinkTask;

import java.util.Set;

import javax.inject.Inject;

public class NativeArtifact extends FileArtifact implements TaskHungryArtifact {

    @Inject
    public NativeArtifact(String name, Project project) {
        super(name, project);
        component = name;
        libraryDirectory = project.getObjects().property(String.class);
    }

    // Accessed in DeployPlugin rules.
    private String component = null;
    private String targetPlatform = null;
    private String buildType = null;
    private String flavor = null;
    private boolean deployLibraries = true;
    private final Property<String> libraryDirectory;

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public String getTargetPlatform() {
        return targetPlatform;
    }

    public void setTargetPlatform(String targetPlatform) {
        this.targetPlatform = targetPlatform;
    }

    public String getBuildType() {
        return buildType;
    }

    public void setBuildType(String buildType) {
        this.buildType = buildType;
    }

    public String getFlavor() {
        return flavor;
    }

    public void setFlavor(String flavor) {
        this.flavor = flavor;
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

    @Override
    public void taskDependenciesAvailable(Set<? extends Task> tasks) {
        AbstractLinkTask[] linkTasks = tasks.stream().filter(x -> x instanceof AbstractLinkTask).map(x -> (AbstractLinkTask)x).toArray(AbstractLinkTask[]::new);
        if (linkTasks.length == 0)
            throw new GradleException(toString() + " does not have any link tasks!");
        if (linkTasks.length > 1)
            throw new GradleException(toString() + " given multiple Link tasks: " + linkTasks);

        RegularFileProperty file = linkTasks[0].getLinkedFile();
        getFile().set(file.getAsFile());
    }

    public void configureLibsArtifact(BinaryLibraryArtifact bla) {
        bla.getTargets().addAll(getTargets());
        if (!libraryDirectory.isPresent()) {
            bla.setDirectory(getDirectory());
        } else {
            bla.setDirectory(libraryDirectory.get());
        }
    }

    public boolean appliesTo(NativeBinarySpec bin) {
        if (!bin.getComponent().getName().equals(component))
            return false;
        if (flavor != null && !getFlavor().equals(bin.getFlavor().getName()))
            return false;
        if (buildType != null && !getBuildType().equals(bin.getBuildType().getName()))
            return false;
        if (!getTargetPlatform().equals(bin.getTargetPlatform().getName()))
            return false;

        return true;
    }

}
