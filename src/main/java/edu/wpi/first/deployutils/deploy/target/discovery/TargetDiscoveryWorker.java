package edu.wpi.first.deployutils.deploy.target.discovery;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import org.gradle.workers.WorkAction;

import edu.wpi.first.deployutils.deploy.context.DeployContext;
import edu.wpi.first.deployutils.deploy.target.RemoteTarget;
import edu.wpi.first.deployutils.deploy.target.discovery.action.DiscoveryAction;
import edu.wpi.first.deployutils.deploy.target.location.DeployLocation;
import edu.wpi.first.deployutils.log.ETLogger;
import edu.wpi.first.deployutils.log.ETLoggerFactory;

public abstract class TargetDiscoveryWorker implements WorkAction<TargetDiscoveryWorkerParameters> {

    private static class DiscoveryStorage {
        public final RemoteTarget target;
        public final Consumer<DeployContext> contextSet;

        public DiscoveryStorage(RemoteTarget target, Consumer<DeployContext> context) {
            this.target = target;
            this.contextSet = context;
        }
    }

    private static Map<Integer, DiscoveryStorage> storage = new HashMap<>();
    private static int storageIndex = 0;

    public static void clearStorage() {
        storage.clear();
        storageIndex++;
    }

    public static int submitStorage(RemoteTarget target, Consumer<DeployContext> cb) {
        DiscoveryStorage stg = new DiscoveryStorage(target, cb);
        int currentIndex = storageIndex;
        storageIndex++;
        storage.put(currentIndex, stg);
        return currentIndex;
    }

    public static int storageCount() {
        return storage.size();
    }

    private ETLogger log;

    @Override
    public void execute() {
        DiscoveryStorage lStorage = storage.remove(getParameters().getIndex().get());
        Consumer<DeployContext> callback = lStorage.contextSet;
        RemoteTarget target = lStorage.target;
        run(callback, target);
    }

    public void run(Consumer<DeployContext> callback, RemoteTarget target) {
        log = ETLoggerFactory.INSTANCE.create(this.getClass().getSimpleName() +"[" + target.getName() + "]");

        Collection<DeployLocation> locSet = target.getLocations();
        Set<DiscoveryAction> actions = new HashSet<>(locSet.size());
        for (DeployLocation deployLocation : locSet) {
            actions.add(deployLocation.createAction());
        }
        ExecutorService exec = Executors.newFixedThreadPool(actions.size());

        try {
            DeployContext ctx = exec.invokeAny(actions, target.getTimeout(), TimeUnit.SECONDS);
            succeeded(ctx, callback, target);
        } catch (TimeoutException | ExecutionException | InterruptedException ignored) {
            List<DiscoveryFailedException> ex = new ArrayList<>();
            for (DiscoveryAction action : actions) {
                DiscoveryFailedException e = action.getException();
                if (e == null) {
                    e = new DiscoveryFailedException(action, new TimeoutException("Discovery timed out."));
                }
                ex.add(e);
            }
            failed(ex, callback, target);
        } finally {
            if (log.backingLogger().isInfoEnabled()) {
                List<DiscoveryFailedException> ex = new ArrayList<>();
                for (DiscoveryAction action : actions) {
                    DiscoveryFailedException e = action.getException();
                    if (e != null) {
                        ex.add(e);
                    }
                }
                logAllExceptions(ex);
            }
        }
    }

    private void succeeded(DeployContext ctx, Consumer<DeployContext> callback, RemoteTarget target) {
        log.log("Using " + ctx.friendlyString() + " for target " + target.getName());
        callback.accept(ctx);
    }

    private void failed(List<DiscoveryFailedException> ex, Consumer<DeployContext> callback, RemoteTarget target) {
        callback.accept(null);
        log.withLock(c -> {
            printFailures(ex);
            String failMsg = "Target " + target.getName() + " could not be found at any location! See above for more details.";
            if (target.isFailOnMissing()) {
                throw new TargetNotFoundException(failMsg);
            } else {
                log.log(failMsg);
                log.log(target.getName() + ".failOnMissing is set to false. Skipping this target and moving on...");
            }
        });
    }

    private void logAllExceptions(List<DiscoveryFailedException> exceptions) {
        for (DiscoveryFailedException ex : exceptions) {
            log.info("Exception caught in discovery " + ex.getAction().getDeployLocation().friendlyString());
            StringWriter s = new StringWriter();
            ex.printStackTrace(new PrintWriter(s));
            log.info(s.toString());
        }
    }

    private void printFailures(List<DiscoveryFailedException> failures) {
        Map<DiscoveryState, List<DiscoveryFailedException>> enumMap = new HashMap<>();
        for (DiscoveryFailedException e : failures) {
            if (!enumMap.containsKey(e.getAction().getState())) {
                enumMap.put(e.getAction().getState(), new ArrayList<>());
            }
            enumMap.get(e.getAction().getState()).add(e);
        }

        log.debug("Failures: " + enumMap);
        // TODO lots of failures

        /*
 def enumMap = new HashMap<DiscoveryState, List<DiscoveryFailedException>>()
        // Sort failures into state buckets
        failures.each { DiscoveryFailedException e ->
            if (!enumMap.containsKey(e.action.state))
                enumMap.put(e.action.state, [] as List)
            enumMap.get(e.action.state).add(e)
        }

        log.debug("Failures: ${enumMap}")
        // Sort and iterate by state priority
        def sorted = enumMap.keySet().sort { a -> -a.priority }
        int printFullPriority = sorted.first().priority

        sorted.each { DiscoveryState state ->
            List<DiscoveryFailedException> fails = enumMap[state]
            if (state.priority == printFullPriority || log.backingLogger().isInfoEnabled()){
                fails.each { DiscoveryFailedException failed ->
                    log.logErrorHead("${failed.action.deployLocation.friendlyString()}: ${state.stateLocalized.capitalize()}.")
                    log.push().with {
                        logError("Reason: ${failed.cause.class.simpleName}")
                        logError(failed.cause.message)
                    }
                }
            } else {
                log.logErrorHead("${fails.size()} other action(s) ${state.stateLocalized}.")
            }
        }

        log.log("Run with --info for more details")

        log.log("") // blank line

        */
    }
}
