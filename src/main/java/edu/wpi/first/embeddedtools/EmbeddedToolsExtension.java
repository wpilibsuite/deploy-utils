package edu.wpi.first.embeddedtools;

import javax.inject.Inject;

import org.gradle.api.Project;
import org.gradle.nativeplatform.NativeBinarySpec;
import org.gradle.platform.base.VariantComponentSpec;

import edu.wpi.first.embeddedtools.nativedeps.DelegatedDependencySet;
import edu.wpi.first.embeddedtools.nativedeps.DependencySpecExtension;

public class EmbeddedToolsExtension {
    private DependencySpecExtension dse = null;
    private Project project;

    @Inject
    public EmbeddedToolsExtension(Project project) {
        this.project = project;
    }

    public void useLibrary(VariantComponentSpec component, boolean skipOnUnknown, String... libraries) {
        component.getBinaries().withType(NativeBinarySpec.class).all(binary -> {
            useLibrary(binary, skipOnUnknown, libraries);
        });
    }

    public void useLibrary(VariantComponentSpec component, String... libraries) {
        useLibrary(component, false, libraries);
    }

    public void useLibrary(NativeBinarySpec binary, boolean skipOnUnknown, String... libraries) {
        if (dse == null) {
            dse = project.getExtensions().getByType(DependencySpecExtension.class);
        }
        for (String library : libraries) {
            binary.lib(new DelegatedDependencySet(library, binary, dse, skipOnUnknown));
        }
    }

    public void useLibrary(NativeBinarySpec binary, String... libraries) {
        useLibrary(binary, false, libraries);
    }
}
