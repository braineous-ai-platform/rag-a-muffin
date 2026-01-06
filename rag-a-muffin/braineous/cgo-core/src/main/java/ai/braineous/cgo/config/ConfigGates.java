package ai.braineous.cgo.config;

public final class ConfigGates {

    private ConfigGates(){}

    public static ConfigGate from(ConfigService cfg){
        return key -> {
            String v = cfg.getProperty(key);
            return v != null && v.trim().equalsIgnoreCase("true");
        };
    }
}

