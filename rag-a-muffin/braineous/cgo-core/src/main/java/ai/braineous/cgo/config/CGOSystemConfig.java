package ai.braineous.cgo.config;

import java.io.InputStream;
import java.util.Properties;

public class CGOSystemConfig {

    private static final String DEFAULT_MONGO_URI = "mongodb://localhost:27017";

    // Keep names boring + consistent with your application.properties key
    private static final String PROP_MONGO_URI = "cgo.mongodb.uri";

    // Env var for operators (docker/compose/k8s)
    private static final String ENV_MONGO_URI = "CGO_MONGODB_URI";

    private CGOSystemConfig() {
    }

    public static String resolveMongoDBUri() {

        // 1) JVM system property (highest precedence)
        String sys = safe(System.getProperty(PROP_MONGO_URI));
        if (sys != null) {
            return sys;
        }

        // 2) Environment variable
        String env = safe(System.getenv(ENV_MONGO_URI));
        if (env != null) {
            return env;
        }

        // 3) application.properties on classpath
        String fromProps = safe(readApplicationProperty(PROP_MONGO_URI));
        if (fromProps != null) {
            return fromProps;
        }

        // 4) hard default
        return DEFAULT_MONGO_URI;
    }

    // -------------------------
    // helpers
    // -------------------------

    private static String readApplicationProperty(String key) {
        Properties p = new Properties();

        InputStream in = null;
        try {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            if (cl == null) {
                cl = CGOSystemConfig.class.getClassLoader();
            }

            in = cl.getResourceAsStream("application.properties");
            if (in == null) {
                return null;
            }

            p.load(in);
            return p.getProperty(key);

        } catch (Exception e) {
            return null;

        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception ignored) {
                    // ignore
                }
            }
        }
    }

    private static String safe(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        if (t.isEmpty()) {
            return null;
        }
        return t;
    }
}

