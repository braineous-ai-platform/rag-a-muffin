package ai.braineous.cgo.config;

@FunctionalInterface
public interface ConfigGate {
    boolean on(String key);
}

