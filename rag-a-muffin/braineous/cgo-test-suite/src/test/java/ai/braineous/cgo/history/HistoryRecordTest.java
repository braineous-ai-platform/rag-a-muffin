package ai.braineous.cgo.history;

import ai.braineous.rag.prompt.cgo.api.QueryExecution;
import ai.braineous.rag.prompt.observe.Console;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HistoryRecordTest {

    @Test
    void toJson_when_execution_null_writes_null_and_roundtrips() {
        Console.log("UT", "HistoryRecord.toJson_when_execution_null_writes_null_and_roundtrips");

        ScorerResult sr = ScorerResult.ok("ok");
        HistoryRecord rec = new HistoryRecord(null, sr);

        JsonObject json = rec.toJson();
        Console.log("UT", json);

        assertTrue(json.has("queryExecution"));
        assertTrue(json.get("queryExecution").isJsonNull());

        HistoryRecord out = HistoryRecord.fromJson(json);
        Console.log("UT", out.toJson());

        assertNull(out.getQueryExecution());
        assertNotNull(out.getResult());
        assertEquals(ScorerResult.Status.OK, out.getResult().getStatus());
    }

    @Test
    void fromJson_null_throws_boring() {
        Console.log("UT", "HistoryRecord.fromJson_null_throws_boring");

        try {
            HistoryRecord.fromJson(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            Console.log("UT", "IllegalArgumentException thrown as expected");
        }
    }

    @Test
    void toJson_roundtrip_success_preserves_execution_and_result() {
        Console.log("UT", "HistoryRecord.toJson_roundtrip_success_preserves_execution_and_result");

        // Build QueryExecution via JSON without request to avoid taskType reflection in UT
        JsonObject qxJson = new JsonObject();
        qxJson.add("request", null);
        qxJson.addProperty("rawResponse", "RAW");
        qxJson.add("promptValidation", null);
        qxJson.add("llmResponseValidation", null);
        qxJson.add("domainValidation", null);

        // derived fields optional, but keep them for parity
        qxJson.addProperty("status", "OK");
        qxJson.addProperty("stage", "ok");
        qxJson.addProperty("ok", true);

        QueryExecution<?> qx = QueryExecution.fromJson(qxJson);

        ScorerResult sr = new ScorerResult();
        sr.setStatus(ScorerResult.Status.OK);
        sr.setScore(0.88);
        sr.setReasonCode("GREEN");
        sr.setSummary("all good");
        sr.addReason("r1");

        HistoryRecord rec = new HistoryRecord(qx, sr);

        JsonObject json = rec.toJson();
        Console.log("UT", json);

        HistoryRecord out = HistoryRecord.fromJson(json);
        Console.log("UT", out.toJson());

        assertNotNull(out.getQueryExecution());
        assertNotNull(out.getResult());

        // ScorerResult roundtrip
        assertEquals(ScorerResult.Status.OK, out.getResult().getStatus());
        assertEquals(0.88, out.getResult().getScore(), 0.0000001);
        assertEquals("GREEN", out.getResult().getReasonCode());
        assertEquals("all good", out.getResult().getSummary());
        assertEquals(1, out.getResult().getReasons().size());
        assertEquals("r1", out.getResult().getReasons().get(0));

        // QueryExecution bits we can assert without needing QueryRequest
        assertNull(out.getQueryExecution().getRequest());
        assertEquals("RAW", out.getQueryExecution().getRawResponse());
        assertTrue(out.getQueryExecution().isOk());
    }

    @Test
    void toJson_when_result_null_writes_null_and_roundtrips() {
        Console.log("UT", "HistoryRecord.toJson_when_result_null_writes_null_and_roundtrips");

        JsonObject qxJson = new JsonObject();
        qxJson.add("request", null);
        qxJson.add("rawResponse", null);
        qxJson.add("promptValidation", null);
        qxJson.add("llmResponseValidation", null);
        qxJson.add("domainValidation", null);
        qxJson.addProperty("status", "OK");
        qxJson.addProperty("stage", "ok");
        qxJson.addProperty("ok", true);

        QueryExecution<?> qx = QueryExecution.fromJson(qxJson);

        HistoryRecord rec = new HistoryRecord(qx, null);

        JsonObject json = rec.toJson();
        Console.log("UT", json);

        assertTrue(json.has("result"));
        assertTrue(json.get("result").isJsonNull());

        HistoryRecord out = HistoryRecord.fromJson(json);
        Console.log("UT", out.toJson());

        assertNotNull(out.getQueryExecution());
        assertNull(out.getResult());
    }

}
