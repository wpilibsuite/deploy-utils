package edu.wpi.first.deployutils.deploy.target;

import javax.inject.Inject;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.internal.CollectionCallbackActionDecorator;
import org.gradle.api.internal.DefaultNamedDomainObjectSet;
import org.gradle.internal.reflect.DirectInstantiator;

import edu.wpi.first.deployutils.Resolver;

// DefaultNamedDomainObjectSet applies the withType, matching, all and other methods
// that are incredibly useful
public class TargetsExtension extends DefaultNamedDomainObjectSet<RemoteTarget> implements Resolver<RemoteTarget> {
    private final Project project;

    public Project getProject() {
        return project;
    }

    @Inject
    public TargetsExtension(Project project) {
        super(RemoteTarget.class, DirectInstantiator.INSTANCE, CollectionCallbackActionDecorator.NOOP);
        this.project = project;
    }

    public <T extends RemoteTarget> T target(String name, Class<T> type, final Action<T> config) {
        T target = project.getObjects().newInstance(type, name, project);
        config.execute(target);
        this.add(target);
        return target;
    }

    public RemoteTarget target(String name, final Action<RemoteTarget> config) {
        return target(name, RemoteTarget.class, config);
    }

    @Override
    public RemoteTarget resolve(Object o) {
        RemoteTarget result = null;
        if (o instanceof String)
            result = this.findByName(o.toString());
        else if (o instanceof RemoteTarget)
            result = (RemoteTarget)o;
        // TODO more resolution methods

        if (result == null)
            throw new ResolveFailedException("Could not find target " + o.toString() + " (" + o.getClass().getName() + ")");

        return result;
    }
}
