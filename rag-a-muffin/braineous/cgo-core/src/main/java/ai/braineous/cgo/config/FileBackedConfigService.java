package ai.braineous.cgo.config;

import java.util.Properties;

public class FileBackedConfigService implements ConfigService{

    private Properties props = new Properties();

    private Properties defaultProps = new Properties();

    public FileBackedConfigService(){
        //load the default properties
        this.loadDefaultProps();

        //load the properties name/value pairs
        this.loadProps();
    }

    public String getProperty(String key){
        if(key == null) return null;
        String k = key.trim();
        if(k.isEmpty()) return null;

        // spec guard: only allow keys we define
        if(!isSpecKey(k)) return null;

        // 1) JVM -D overrides
        String v = System.getProperty(k);
        if(v != null && !v.trim().isEmpty()) return v.trim();

        // 2) ENV overrides (DD_FOO_BAR)
        v = System.getenv(toEnvKey(k));
        if(v != null && !v.trim().isEmpty()) return v.trim();

        // 3) dd.properties overrides
        v = props.getProperty(k);
        if(v != null && !v.trim().isEmpty()) return v.trim();

        // 4) spec defaults
        v = defaultProps.getProperty(k);
        return (v == null) ? null : v.trim();
    }

    @Override
    public void setProperty(String key, String value) {
        if(key == null ||
                value == null ||
                key.trim().length() == 0 ||
                value.trim().length() == 0
        ) return;

        if(!isSpecKey(key)) return;

        this.props.setProperty(key, value);
    }

    //----------------------------------------------------------------
    private boolean isSpecKey(String key){
        if(key == null) return false;
        if(!key.startsWith("dd.")) return false;

        // IMPORTANT: containsKey, not contains
        return defaultProps.containsKey(key);
    }

    private String toEnvKey(String ddKey){
        // dd.kafkadd.replay.limit.default  ->  DD_KAFKADD_REPLAY_LIMIT_DEFAULT
        return "DD_" + ddKey
                .substring(3)           // drop "dd."
                .replace('.', '_')
                .toUpperCase();
    }

    private void loadDefaultProps() {
        Properties loaded = new Properties();

        try {
            // 1) File system override (Docker mount)
            String path = System.getenv("DD_CONFIG_PATH");
            if (path == null || path.trim().isEmpty()) {
                path = "/dd-default.properties";
            }

            java.io.File f = new java.io.File(path);
            if (f.exists() && f.isFile()) {
                try (java.io.InputStream in = new java.io.FileInputStream(f)) {
                    loaded.load(in);
                    this.defaultProps = loaded;

                    return;
                } catch (Exception ignore) {
                    // fall through to classpath
                }
            }

            // 2) Classpath fallback (dev / jar defaults)
            try (java.io.InputStream in =
                         FileBackedConfigService.class.getResourceAsStream("/dd-default.properties")) {
                if (in != null) {
                    loaded.load(in);
                }
            } catch (Exception ignore) {
            }

            this.defaultProps = loaded;

        } finally {
            Properties p = this.defaultProps;
            if (p == null) {
                p = loaded; // at least log what we had
            }
            if (p == null) {
                p = new Properties();
            }

            ai.braineous.rag.prompt.observe.Console.log("dd_default_props_count", "" + p.size());
            ai.braineous.rag.prompt.observe.Console.log("dd_default_props", "" + p);
        }
    }




    private void loadProps() {
        Properties loaded = new Properties();
        java.io.InputStream in = null;

        try {
            java.nio.file.Path p = resolveConfigPath();
            if (p != null && java.nio.file.Files.exists(p)) {
                in = java.nio.file.Files.newInputStream(p);
                loaded.load(in);
            } else {
                in = FileBackedConfigService.class.getResourceAsStream("/dd.properties");
                if (in != null) loaded.load(in);
            }
        } catch (Exception ignore) {
        } finally {
            if (in != null) try { in.close(); } catch (Exception ignore2) {}
        }

        this.props = filterToSpecKeys(loaded);
    }



    private java.nio.file.Path resolveConfigPath() {
        String explicit = System.getProperty("dd.config");
        if (explicit != null && !explicit.trim().isEmpty()) {
            return java.nio.file.Paths.get(explicit.trim());
        }
        return java.nio.file.Paths.get("dd.properties");
    }


    private Properties filterToSpecKeys(Properties loaded) {
        Properties out = new Properties();
        if (loaded == null) return out;

        for (String key : loaded.stringPropertyNames()) {
            if (this.defaultProps.containsKey(key)) {
                out.setProperty(key, loaded.getProperty(key));
            }
        }
        return out;
    }


    public java.util.Map<String,String> snapshot(){
        java.util.Map<String,String> m = new java.util.TreeMap<>();
        for(String k : defaultProps.stringPropertyNames()){
            m.put(k, getProperty(k));
        }
        return java.util.Collections.unmodifiableMap(m);
    }

    void reloadForTest(Properties overrides){
        this.props = filterToSpecKeys(overrides);
    }

}
