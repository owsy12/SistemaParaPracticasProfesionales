package GUI.Utils;

import GUI.SessionManager.SessionManager;
import Logic.DTOs.User;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class AuditLog {

    private static final Logger LOGGER = Logger.getLogger("SGPP.Audit");
    private static final String UNKNOWN_ACTOR = "desconocido";

    private AuditLog() {
    }

    public static void record(String action) {
        String actor = resolveActor();
        LOGGER.log(Level.INFO, "Auditoria: usuario {0} {1}", new Object[]{actor, action});
    }

    private static String resolveActor() {
        String actor = UNKNOWN_ACTOR;
        User currentUser = SessionManager.getInstance().getUser();
        if (currentUser != null) {
            actor = String.valueOf(currentUser.getId());
        }
        return actor;
    }
}
