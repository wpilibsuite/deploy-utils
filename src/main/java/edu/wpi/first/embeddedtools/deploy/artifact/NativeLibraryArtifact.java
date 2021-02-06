package edu.wpi.first.embeddedtools.deploy.artifact;

import edu.wpi.first.embeddedtools.deploy.context.DeployContext;
import edu.wpi.first.embeddedtools.nativedeps.DependencySpecExtension;
import edu.wpi.first.embeddedtools.nativedeps.ETNativeDepSet;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

class NativeLibraryArtifact extends FileCollectionArtifact {

    @Inject
    public NativeLibraryArtifact(String name, Project project) {
        super(name, project);
        library = name;
    }

    String library = null;
    String targetPlatform = null;
    String flavor = null;
    String buildType = null;

    public String getLibrary() {
        return library;
    }

    public void setLibrary(String library) {
        this.library = library;
    }

    public String getTargetPlatform() {
        return targetPlatform;
    }

    public void setTargetPlatform(String targetPlatform) {
        this.targetPlatform = targetPlatform;
    }

    public String getFlavor() {
        return flavor;
    }

    public void setFlavor(String flavor) {
        this.flavor = flavor;
    }

    public String getBuildType() {
        return buildType;
    }

    public void setBuildType(String buildType) {
        this.buildType = buildType;
    }

    @Override
    public void deploy(DeployContext ctx) {
        List<ETNativeDepSet> sets = getProject().getExtensions().getByType(DependencySpecExtension.class).getSets();

        Optional<FileCollection> candidates = sets.stream()
            .filter(set -> set.getName().equals(library) && set.appliesTo(getFlavor(), getBuildType(), getTargetPlatform()))
            .map(it -> it.getRuntimeFiles())
            .reduce((a, b) -> a.plus(b));

        if (candidates.isEmpty()) {
            throw new GradleException(toString() + " cannot find a sutable dependency for library "
                + library + ", platform " + targetPlatform + ", flavor " + flavor + ", buildType " + buildType);
        }

        getFiles().set(candidates.get());
        super.deploy(ctx);
    }
}
