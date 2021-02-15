package edu.wpi.first.deployutils.deploy.cache;

import java.io.File;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.internal.CollectionCallbackActionDecorator;
import org.gradle.api.internal.DefaultNamedDomainObjectSet;
import org.gradle.internal.reflect.DirectInstantiator;

import edu.wpi.first.deployutils.ClosureUtils;
import edu.wpi.first.deployutils.DeployUtils;
import edu.wpi.first.deployutils.Resolver;
import edu.wpi.first.deployutils.deploy.context.DeployContext;
import groovy.lang.Closure;
import groovy.lang.GString;

public class CacheExtension extends DefaultNamedDomainObjectSet<CacheMethod> implements Resolver<CacheMethod> {

    final Project project;

    public Project getProject() {
        return project;
    }

    public CacheExtension(Project project) {
        super(CacheMethod.class, DirectInstantiator.INSTANCE, CollectionCallbackActionDecorator.NOOP);
        this.project = project;

        method("md5file", Md5FileCacheMethod.class, x -> {
        });
        method("md5sum", Md5SumCacheMethod.class, x -> {
        });
    }

    public final <T extends AbstractCacheMethod> T method(String name, Class<T> type, final Action<T> config) {
        T cm = project.getObjects().newInstance(type, name);
        config.execute(cm);
        this.add(cm);
        return cm;
    }

    public DefaultCacheMethod method(String name, final Action<DefaultCacheMethod> config) {
        return method(name, DefaultCacheMethod.class, config);
    }

    public CacheMethod resolve(Object cache) {
        if (DeployUtils.isSkipCache(project)) {
            return null;
        } else if (cache == null || (cache instanceof Boolean && !(Boolean)cache)) {
            return null;
        } else if (cache instanceof CacheMethod) {
            return (CacheMethod)cache;
        } else if (cache instanceof String || cache instanceof GString) {
            return getByName(cache.toString());
        } else if (cache instanceof CacheCheckerFunction) {
            DefaultCacheMethod dcm = new DefaultCacheMethod("customCacheMethod");
            dcm.setNeedsUpdate((CacheCheckerFunction) cache);
            return dcm;
        } else if (cache instanceof Closure) {
            DefaultCacheMethod dcm = new DefaultCacheMethod("customCacheMethod");
            CacheCheckerFunction ccf = new CacheCheckerFunction() {

				@Override
				public boolean check(DeployContext ctx, String filename, File localFile) {
					Closure<?> cc = (Closure<?>) cache;
                    return (Boolean)ClosureUtils.delegateCall(ctx, cc, filename, localFile);
				}

            };
            dcm.setNeedsUpdate(ccf);
            return dcm;
        }

        throw new IllegalArgumentException("Unknown Cache Method Type: ${cache.class}.\nMust be one of:\n" +
                "- instance of CacheMethod\n" +
                "- The name (String) of a CacheMethod stored in deploy.cache\n" +
                "- A closure returning whether the file needs update (true) or not (false)\n" +
                "- Null or False for no caching");
    }

}
