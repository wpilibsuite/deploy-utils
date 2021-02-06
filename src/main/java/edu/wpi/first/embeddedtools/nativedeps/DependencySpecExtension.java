package edu.wpi.first.embeddedtools.nativedeps;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.gradle.api.Project;
import org.gradle.nativeplatform.BuildType;
import org.gradle.nativeplatform.Flavor;
import org.gradle.nativeplatform.NativeBinarySpec;
import org.gradle.nativeplatform.platform.NativePlatform;

public class DependencySpecExtension {
    List<ETNativeDepSet> sets;
    final Project project;

    public List<ETNativeDepSet> getSets() {
        return sets;
    }

    public void setSets(List<ETNativeDepSet> sets) {
        this.sets = sets;
    }

    public Project getProject() {
        return project;
    }

    public DependencySpecExtension(Project project) {
        sets = new ArrayList<>();
        this.project = project;
    }

    public ETNativeDepSet find(String name, NativeBinarySpec binary) {
        return find(name, binary.getFlavor(), binary.getBuildType(), binary.getTargetPlatform());
    }

    public ETNativeDepSet find(String name, Flavor flavor, BuildType buildType, NativePlatform targetPlatform) {
        Optional<ETNativeDepSet> first = sets.stream().filter(x -> x.getName().equals(name) && x.appliesTo(flavor, buildType, targetPlatform)).findFirst();
        if (first.isPresent()) {
            return first.get();
        } else {
            return null;
        }
    }
}
