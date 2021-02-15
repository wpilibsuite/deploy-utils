package edu.wpi.first.deployutils.deploy.artifact;

import javax.inject.Inject;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.internal.CollectionCallbackActionDecorator;
import org.gradle.api.internal.DefaultNamedDomainObjectSet;
import org.gradle.internal.reflect.DirectInstantiator;

import edu.wpi.first.deployutils.Resolver;

// DefaultNamedDomainObjectSet applies the withType, matching, all and other methods
// that are incredibly useful
public class ArtifactsExtension extends DefaultNamedDomainObjectSet<Artifact> implements Resolver<Artifact> {
    private final Project project;

    public Project getProject() {
        return project;
    }

    @Inject
    public ArtifactsExtension(Project project) {
        super(Artifact.class, DirectInstantiator.INSTANCE, CollectionCallbackActionDecorator.NOOP);
        this.project = project;
    }

    public <T extends Artifact> T artifact(String name, Class<T> type, final Action<T> config) {
        T artifact = project.getObjects().newInstance(type, name, project);
        config.execute(artifact);
        this.add(artifact);
        return artifact;
    }

    public ActionArtifact actionArtifact(String name, final Action<ActionArtifact> config) {
        return artifact(name, ActionArtifact.class, config);
    }

    public FileArtifact fileArtifact(String name, final Action<FileArtifact> config) {
        return artifact(name, FileArtifact.class, config);
    }

    public FileCollectionArtifact fileCollectionArtifact(String name, final Action<FileCollectionArtifact> config) {
        return artifact(name, FileCollectionArtifact.class, config);
    }

    public FileTreeArtifact fileTreeArtifact(String name, final Action<FileTreeArtifact> config) {
        return artifact(name, FileTreeArtifact.class, config);
    }

    public CommandArtifact commandArtifact(String name, final Action<CommandArtifact> config) {
        return artifact(name, CommandArtifact.class, config);
    }

    public MultiCommandArtifact multiCommandArtifact(String name, final Action<MultiCommandArtifact> config) {
        return artifact(name, MultiCommandArtifact.class, config);
    }

    public JavaArtifact javaArtifact(String name, final Action<JavaArtifact> config) {
        return artifact(name, JavaArtifact.class, config);
    }

    public NativeArtifact nativeArtifact(String name, final Action<NativeArtifact> config) {
        return artifact(name, NativeArtifact.class, config);
    }

    public BinaryLibraryArtifact binaryLibraryArtifact(String name, final Action<BinaryLibraryArtifact> config) {
        return artifact(name, BinaryLibraryArtifact.class, config);
    }

    public MavenArtifact mavenArtifact(String name, final Action<MavenArtifact> config) {
        return artifact(name, MavenArtifact.class, config);
    }

    @Override
    public Artifact resolve(Object o) {
        Artifact result = null;
        if (o instanceof String)
            result = this.findByName(o.toString());
        else if (o instanceof Artifact)
            result = (Artifact)o;
        // TODO more resolution methods

        if (result == null)
            throw new ResolveFailedException("Could not find artifact " + o.toString() + " (" + o.getClass().getName() + ")");

        return result;
    }
}
