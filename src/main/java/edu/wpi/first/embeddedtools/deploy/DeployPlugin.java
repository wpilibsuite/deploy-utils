package edu.wpi.first.embeddedtools.deploy;

import java.util.ArrayList;
import java.util.List;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.language.base.plugins.ComponentModelBasePlugin;
import org.gradle.model.ModelMap;
import org.gradle.model.Mutate;
import org.gradle.model.RuleSource;
import org.gradle.nativeplatform.NativeBinarySpec;
import org.gradle.nativeplatform.tasks.AbstractLinkTask;
import org.gradle.platform.base.BinaryContainer;

import edu.wpi.first.embeddedtools.deploy.artifact.ArtifactDeployWorker;
import edu.wpi.first.embeddedtools.deploy.artifact.NativeArtifact;
import edu.wpi.first.embeddedtools.deploy.target.discovery.TargetDiscoveryWorker;

public class DeployPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(ComponentModelBasePlugin.class);

        DeployExtension deployExt = project.getExtensions().create("deploy", DeployExtension.class, project);

        deployExt.getArtifacts().withType(NativeArtifact.class).all(art -> {});

        // TODO Move these onto extensions so this becomes a non issue
        project.getGradle().buildFinished(x -> {
            TargetDiscoveryWorker.clearStorage();
            ArtifactDeployWorker.clearStorage();
        });
    }

    public static class DeployRules extends RuleSource {
        @Mutate
        public void createBinariesTasks(final ModelMap<Task> tasks, final ExtensionContainer ext, final BinaryContainer binaries) {
            DeployExtension deployExtension = ext.getByType(DeployExtension.class);
            List<NativeArtifact> artifacts = new ArrayList<>(deployExtension.getArtifacts().withType(NativeArtifact.class));
            for (NativeArtifact artifact : artifacts) {
                for (NativeBinarySpec bin : binaries.withType(NativeBinarySpec.class)) {
                    if (artifact.appliesTo(bin)) {
                        bin.getTasks().withType(AbstractLinkTask.class, linkTask -> artifact.dependsOn(linkTask));
                        if (artifact.isDeployLibraries()) {
                            deployExtension.getArtifacts().binaryLibraryArtifact(artifact.getName() + "Libraries", bla -> {
                                bla.setBinary(bin);
                                artifact.configureLibsArtifact(bla);
                                bin.getTasks().withType(AbstractLinkTask.class, linkTask -> bla.dependsOn(linkTask));
                            });
                        }
                    }
                }
            }
        }
    }
}
