package edu.wpi.first.deployutils.deploy.artifact;

import javax.inject.Inject;

import org.gradle.api.Action;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.provider.Property;
import org.gradle.nativeplatform.NativeBinarySpec;
import org.gradle.nativeplatform.tasks.AbstractLinkTask;
import org.gradle.platform.base.ComponentSpecContainer;
import org.gradle.platform.base.VariantComponentSpec;

import edu.wpi.first.deployutils.deploy.DeployExtension;
import edu.wpi.first.deployutils.deploy.DeployPlugin;

public class NativeArtifact extends FileArtifact {

    @Inject
    public NativeArtifact(String name, Project project) {
        super(name, project);
        component = name;
        libraryDirectory = project.getObjects().property(String.class);
    }

    // Accessed in DeployPlugin rules.
    private String component = null;
    private String buildType = null;
    private String flavor = null;
    private boolean deployLibraries = true;
    private final Property<String> libraryDirectory;
    private Action<BinaryLibraryArtifact> onBinaryLibraryArtifactCreated;

    public Action<BinaryLibraryArtifact> getOnBinaryLibraryArtifactCreated() {
        return onBinaryLibraryArtifactCreated;
    }

    public void setOnBinaryLibraryArtifactCreated(Action<BinaryLibraryArtifact> onBinaryLibraryArtifactCreated) {
        this.onBinaryLibraryArtifactCreated = onBinaryLibraryArtifactCreated;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
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

    public DeployPlugin.ArtifactBinaryLinkTaskTuple configureFromModel(ComponentSpecContainer components, DeployExtension de) {
        VariantComponentSpec foundComponent = components.withType(VariantComponentSpec.class).get(getComponent());
        if (foundComponent == null) {
            throw new GradleException("Component " + getComponent() + " for " + getName() + " artifact not found");
        }
        AbstractLinkTask linkTask = null;
        NativeBinarySpec foundBin = null;
        for (NativeBinarySpec binary : foundComponent.getBinaries().withType(NativeBinarySpec.class)) {
            if (!appliesTo(binary)) {
                continue;
            }
            DomainObjectSet<AbstractLinkTask> potentialLinkTasks = binary.getTasks().withType(AbstractLinkTask.class);
            if (potentialLinkTasks.isEmpty()) {
                continue;
            }
            linkTask = potentialLinkTasks.iterator().next();
            dependsOn(linkTask);
            getFile().set(linkTask.getLinkedFile().map(x -> x.getAsFile()));
            foundBin = binary;
            break;
        }
        if (foundBin == null) {
            throw new GradleException("Linkable binary in " + getComponent() + " for " + getName() + " artifact not found");
        }
        if (isDeployLibraries()) {
            return new DeployPlugin.ArtifactBinaryLinkTaskTuple(this, foundBin, linkTask);
        }
        return null;
    }

    public void configureBlaArtifact(DeployPlugin.ArtifactBinaryLinkTaskTuple toAdd, DeployExtension de) {
        if (this != toAdd.getArtifact()) {
            throw new GradleException("Can only configure this target");
        }
        de.getArtifacts().binaryLibraryArtifact(toAdd.getArtifact().getName() + "Libraries", bla -> {
            if (onBinaryLibraryArtifactCreated != null) {
                onBinaryLibraryArtifactCreated.execute(bla);
            }
            bla.setBinary(toAdd.getBinary());
            bla.setTarget(this.getTarget());
            if (this.libraryDirectory.isPresent()) {
                bla.getDirectory().set(this.getLibraryDirectory());
            } else {
                bla.getDirectory().set(this.getDirectory());
            }
            bla.dependsOn(toAdd.getLinkTask());
        });
    }

    private boolean appliesTo(NativeBinarySpec bin) {
        if (!bin.getComponent().getName().equals(component))
            return false;
        if (flavor != null && !getFlavor().equals(bin.getFlavor().getName()))
            return false;
        if (buildType != null && !getBuildType().equals(bin.getBuildType().getName()))
            return false;
        if (!getTarget().getTargetPlatform().equals(bin.getTargetPlatform().getName()))
            return false;

        return true;
    }

}
