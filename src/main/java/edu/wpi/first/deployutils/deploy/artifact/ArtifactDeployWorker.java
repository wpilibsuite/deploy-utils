package edu.wpi.first.deployutils.deploy.artifact;

import java.util.HashMap;
import java.util.Map;

import org.gradle.workers.WorkAction;

import edu.wpi.first.deployutils.deploy.context.DeployContext;

public abstract class ArtifactDeployWorker implements WorkAction<ArtifactDeployParameters> {

    private static class DeployStorage {
        DeployContext context;
        Artifact artifact;
    }

    private static Map<Integer, DeployStorage> deployerStorage = new HashMap<>();
    private static int deployerIndex = 0;

    public static void clearStorage() {
        deployerStorage.clear();
        deployerIndex = 0;
    }

    public static int submitStorage(DeployContext context, Artifact artifact) {
        DeployStorage ds = new DeployStorage();
        ds.context = context;
        ds.artifact = artifact;
        int currentIndex = deployerIndex;
        deployerIndex++;
        deployerStorage.put(currentIndex, ds);
        return currentIndex;
    }

    public static int storageCount() {
        return deployerStorage.size();
    }

    @Override
    public void execute() {
        Integer index = getParameters().getIndex().get();
        DeployStorage storage = deployerStorage.remove(index);

        DeployContext rootContext = storage.context;
        Artifact artifact = storage.artifact;
        run(rootContext, artifact);
    }

    public void run(DeployContext rootContext, Artifact artifact) {
        DeployContext context = rootContext.subContext(artifact.getDirectory().get());
        boolean enabled = artifact.isEnabled(context);
        if (enabled) {
            ArtifactRunner.runDeploy(artifact, context);
        } else {
            context.getLogger().log("Artifact skipped");
        }
    }
}
