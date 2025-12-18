package ai.braineous.fno.controllers;

import ai.braineous.rag.prompt.observe.Console;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class IngestionControllerTests {
    @Test
    void ingestEndpoint_acceptsFlightsArray_andReturnsGraphSnapshotString() {
        String body =
                "[" +
                        "{\"id\":\"F102\",\"origin\":\"AUS\",\"dest\":\"DFW\",\"dep_utc\":\"2025-10-22T11:30:00Z\",\"arr_utc\":\"2025-10-22T12:40:00Z\"}," +
                        "{\"id\":\"F103\",\"origin\":\"DFW\",\"dest\":\"IAH\",\"dep_utc\":\"2025-10-22T13:30:00Z\",\"arr_utc\":\"2025-10-22T14:35:00Z\"}" +
                        "]";

        Console.log("test.ingest.in", body);

        String resp =
                given()
                        .contentType("application/json")
                        .body(body)
                        .when()
                        .post("/fno/ingest")
                        .then()
                        .statusCode(200)
                        .extract()
                        .asString();

        Console.log("test.ingest.out", resp);

        // calibration asserts (loose, but signal-rich)
        assertNotNull(resp);
        assertFalse(resp.isBlank());

        // prove substrate came back (stringified GraphSnapshot)
        assertTrue(resp.contains("GraphSnapshot") || resp.contains("GraphSnapshot{"));
        assertTrue(resp.contains("Airport:AUS"));
        assertTrue(resp.contains("Airport:DFW"));
        assertTrue(resp.contains("Airport:IAH"));
        assertTrue(resp.contains("Flight:F102"));
        assertTrue(resp.contains("Flight:F103"));
    }

    @Test
    void ingestEndpoint_rejectsInvalidPayloadShape() {
        String body = "{\"item\":\"Book\",\"quantity\":2,\"price\":10.5}";
        Console.log("test.ingest.bs.shape.in", body);

        String resp =
                given()
                        .contentType("application/json")
                        .body(body)
                        .when()
                        .post("/fno/ingest")
                        .then()
                        .statusCode(400)
                        .extract()
                        .asString();

        Console.log("test.ingest.bs.shape.out", resp);

        assertNotNull(resp);
        assertTrue(resp.contains("invalid payload") || resp.contains("parse failed"));
    }


    @Test
    void ingestEndpoint_bookArray_returnsEmptyGraphSnapshot() {
        String body =
                "[" +
                        "{\"item\":\"Book\",\"quantity\":2,\"price\":10.5}" +
                        "]";

        Console.log("test.ingest.bs.bookArray.in", body);

        String resp =
                given()
                        .contentType("application/json")
                        .body(body)
                        .when()
                        .post("/fno/ingest")
                        .then()
                        .statusCode(200)
                        .extract()
                        .asString();

        Console.log("test.ingest.bs.bookArray.out", resp);

        assertNotNull(resp);
        assertFalse(resp.isBlank());

        // calibration: confirms “fail-soft → empty substrate”
        assertTrue(resp.contains("GraphSnapshot"));
        assertTrue(resp.contains("nodes={}") || resp.contains("nodes={}"));
    }

    @Test
    void ingestEndpoint_mixedArray_ignoresGarbage_andKeepsValidFacts() {
        String body =
                "[" +
                        "{\"id\":\"F102\",\"origin\":\"AUS\",\"dest\":\"DFW\",\"dep_utc\":\"2025-10-22T11:30:00Z\",\"arr_utc\":\"2025-10-22T12:40:00Z\"}," +
                        "{\"item\":\"Book\",\"quantity\":2,\"price\":10.5}" +
                        "]";

        Console.log("test.ingest.mixed.in", body);

        String resp =
                given()
                        .contentType("application/json")
                        .body(body)
                        .when()
                        .post("/fno/ingest")
                        .then()
                        .statusCode(200)
                        .extract()
                        .asString();

        Console.log("test.ingest.mixed.out", resp);

        assertNotNull(resp);
        assertFalse(resp.isBlank());

        // Calibration: valid flight facts must appear
        assertTrue(resp.contains("Flight:F102"));
        assertTrue(resp.contains("Airport:AUS"));
        assertTrue(resp.contains("Airport:DFW"));

        // Calibration: garbage should not create nodes
        assertFalse(resp.contains("Book"));
        assertFalse(resp.contains("quantity"));
        assertFalse(resp.contains("price"));
    }


    @Test
    void ingestEndpoint_acceptsFlightsWrapperObject() {
        String body =
                "{" +
                        "\"flights\":[" +
                        "{\"id\":\"F102\",\"origin\":\"AUS\",\"dest\":\"DFW\",\"dep_utc\":\"2025-10-22T11:30:00Z\",\"arr_utc\":\"2025-10-22T12:40:00Z\"}," +
                        "{\"id\":\"F103\",\"origin\":\"DFW\",\"dest\":\"IAH\",\"dep_utc\":\"2025-10-22T13:30:00Z\",\"arr_utc\":\"2025-10-22T14:35:00Z\"}" +
                        "]" +
                        "}";

        Console.log("test.ingest.wrapper.in", body);

        String resp =
                given()
                        .contentType("application/json")
                        .body(body)
                        .when()
                        .post("/fno/ingest")
                        .then()
                        .statusCode(200)
                        .extract()
                        .asString();

        Console.log("test.ingest.wrapper.out", resp);

        assertNotNull(resp);
        assertFalse(resp.isBlank());

        assertTrue(resp.contains("Flight:F102"));
        assertTrue(resp.contains("Flight:F103"));
        assertTrue(resp.contains("Airport:AUS"));
        assertTrue(resp.contains("Airport:DFW"));
        assertTrue(resp.contains("Airport:IAH"));
    }

    @Test
    void ingestEndpoint_emptyArray_returnsEmptyGraphSnapshot() {
        String body = "[]";
        Console.log("test.ingest.empty.in", body);

        String resp =
                given()
                        .contentType("application/json")
                        .body(body)
                        .when()
                        .post("/fno/ingest")
                        .then()
                        .statusCode(200)
                        .extract()
                        .asString();

        Console.log("test.ingest.empty.out", resp);

        assertNotNull(resp);
        assertFalse(resp.isBlank());

        // Calibration invariant: empty input → empty substrate
        assertTrue(resp.contains("GraphSnapshot"));
        assertTrue(resp.contains("nodes={}"));
    }

    @Test
    void ingestEndpoint_malformedJson_returns400() {
        String body = "[{";
        Console.log("test.ingest.malformed.in", body);

        String resp =
                given()
                        .contentType("application/json")
                        .body(body)
                        .when()
                        .post("/fno/ingest")
                        .then()
                        .statusCode(400)
                        .extract()
                        .asString();

        Console.log("test.ingest.malformed.out", resp);

        assertNotNull(resp);
        assertTrue(resp.contains("parse failed"));
    }
}
