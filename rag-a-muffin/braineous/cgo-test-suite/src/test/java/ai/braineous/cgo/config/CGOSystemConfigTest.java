package ai.braineous.cgo.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CGOSystemConfigTest {

    @AfterEach
    void cleanup() {
        System.clearProperty("cgo.mongodb.uri");
    }

    @Test
    void resolveMongoDBUri_when_systemProperty_set_wins() {
        System.setProperty("cgo.mongodb.uri", "mongodb://sysprop:27017");
        String uri = CGOSystemConfig.resolveMongoDBUri();
        assertEquals("mongodb://sysprop:27017", uri);
    }

    @Test
    void resolveMongoDBUri_when_systemProperty_not_set_uses_application_properties_if_present() {
        // IMPORTANT:
        // Put src/test/resources/application.properties with:
        // cgo.mongodb.uri=mongodb://localhost:27017
        String uri = CGOSystemConfig.resolveMongoDBUri();
        assertEquals("mongodb://localhost:27017", uri);
    }
}

