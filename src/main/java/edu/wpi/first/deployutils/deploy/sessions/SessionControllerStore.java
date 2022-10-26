package edu.wpi.first.deployutils.deploy.sessions;

import java.util.ArrayList;
import java.util.List;

public class SessionControllerStore {
    private static List<SessionController> sessions = new ArrayList<>();

    public static void clearStorage() {
        sessions.clear();
    }

    public static void closeAndClearStorage() {
        for (SessionController sessionController : sessions) {
            try {
                sessionController.close();
            } catch (Exception e) {
            }
        }
        clearStorage();
    }
}
