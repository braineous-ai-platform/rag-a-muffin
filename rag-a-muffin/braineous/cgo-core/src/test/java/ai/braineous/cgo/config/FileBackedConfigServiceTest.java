package ai.braineous.cgo.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedConfigServiceTest {

    @TempDir
    Path tmp;

    @Test
    void loads_defaults_from_classpath() {
        ConfigService config = new FileBackedConfigService();

        assertEquals("local", config.getProperty("dd.env"));
        assertEquals("8080", config.getProperty("dd.http.port"));
        assertEquals("true", config.getProperty("dd.feature.replay.enabled"));
        assertEquals("true", config.getProperty("dd.feature.dlq.enabled"));
    }

    @Test
    void override_file_takes_precedence_over_defaults() throws Exception {
        Path ddProps = tmp.resolve("dd.properties");

        Files.writeString(ddProps, """
            dd.env=test
            dd.feature.replay.enabled=false
        """);

        System.setProperty("dd.config", ddProps.toString());
        try {
            ConfigService config = new FileBackedConfigService();

            assertEquals("test", config.getProperty("dd.env"));
            assertEquals("false", config.getProperty("dd.feature.replay.enabled"));

            // untouched default still applies
            assertEquals("8080", config.getProperty("dd.http.port"));
        } finally {
            System.clearProperty("dd.config");
        }
    }
}