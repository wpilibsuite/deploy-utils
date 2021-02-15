package edu.wpi.first.deployutils.deploy;

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
import org.gradle.platform.base.ComponentSpecContainer;

import edu.wpi.first.deployutils.deploy.artifact.ArtifactDeployWorker;
import edu.wpi.first.deployutils.deploy.artifact.NativeArtifact;
import edu.wpi.first.deployutils.deploy.target.discovery.TargetDiscoveryWorker;

public class DeployPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(ComponentModelBasePlugin.class);

        DeployExtension deployExt = project.getExtensions().create("deploy", DeployExtension.class, project);

        deployExt.getArtifacts().withType(NativeArtifact.class).all(art -> {
        });

        // TODO Move these onto extensions so this becomes a non issue
        project.getGradle().buildFinished(x -> {
            TargetDiscoveryWorker.clearStorage();
            ArtifactDeployWorker.clearStorage();
        });
    }

    public static class ArtifactBinaryLinkTaskTuple {
        private final NativeArtifact artifact;
        private final NativeBinarySpec binary;
        private final AbstractLinkTask linkTask;

        public NativeArtifact getArtifact() {
            return artifact;
        }

        public NativeBinarySpec getBinary() {
            return binary;
        }

        public AbstractLinkTask getLinkTask() {
            return linkTask;
        }

        public ArtifactBinaryLinkTaskTuple(NativeArtifact artifact, NativeBinarySpec binary,
                AbstractLinkTask linkTask) {
            this.artifact = artifact;
            this.binary = binary;
            this.linkTask = linkTask;
        }


    }

    public static class DeployRules extends RuleSource {
        @Mutate
        public void createBinariesTasks(final ModelMap<Task> tasks, final ExtensionContainer ext, final BinaryContainer binaries, final ComponentSpecContainer components) {
            DeployExtension deployExtension = ext.getByType(DeployExtension.class);
            List<NativeArtifact> artifacts = new ArrayList<>(deployExtension.getArtifacts().withType(NativeArtifact.class));
            if (artifacts.size() == 0) {
                return;
            }
            List<ArtifactBinaryLinkTaskTuple> blaArtifacts = new ArrayList<>();
            for (NativeArtifact artifact : artifacts) {
                ArtifactBinaryLinkTaskTuple foundBinaryToConfigureBla = artifact.configureFromModel(components, deployExtension);
                if (foundBinaryToConfigureBla != null) {
                    blaArtifacts.add(foundBinaryToConfigureBla);
                }
            }

            for (ArtifactBinaryLinkTaskTuple toAdd : blaArtifacts) {
                toAdd.getArtifact().configureBlaArtifact(toAdd, deployExtension);
            }
        }
    }
}
