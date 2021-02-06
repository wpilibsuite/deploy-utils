package edu.wpi.first.embeddedtools.deploy.artifact;

import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.bundling.Jar;

import java.util.Set;
import java.util.concurrent.Callable;
import javax.inject.Inject;

public class JavaArtifact extends FileArtifact implements TaskHungryArtifact {

    @Inject
    public JavaArtifact(String name, Project project) {
        super(name, project);
        Callable<Object> cbl = () -> jar;
        dependsOn(cbl);
    }

    private Object jar = "jar";

    @Override
    public void taskDependenciesAvailable(Set<? extends Task> tasks) {
        Jar[] jarTasks = tasks.stream().filter(x -> x instanceof Jar).map(x -> (Jar)x).toArray(Jar[]::new);
        if (jarTasks.length > 1) {
            throw new GradleException(toString() + " given multiple Jar tasks: " + jarTasks);
        }

        Provider<RegularFile> file = jarTasks[0].getArchiveFile();
        getFile().set(file.get().getAsFile());
    }

    public void setJar(Object jar) {
        this.jar = jar;
    }

    public Object getJar() {
        return jar;
    }
}
