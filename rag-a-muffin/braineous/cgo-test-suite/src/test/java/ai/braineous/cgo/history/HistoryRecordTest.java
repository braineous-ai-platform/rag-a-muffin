package ai.braineous.cgo.history;

import ai.braineous.rag.prompt.cgo.api.QueryExecution;
import ai.braineous.rag.prompt.observe.Console;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import java.time.Instant;

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

    @Test
    void toJson_when_status_pending_and_timestamps_roundtrips() {
        Console.log("UT", "HistoryRecord.toJson_when_status_pending_and_timestamps_roundtrips");

        ScorerResult sr = ScorerResult.ok("ok");
        HistoryRecord rec = new HistoryRecord(null, sr);

        Instant now = Instant.parse("2026-02-04T12:00:00Z");
        rec.markPending(now);

        JsonObject json = rec.toJson();
        Console.log("UT", json);

        assertTrue(json.has("status"));
        assertEquals("PENDING", json.get("status").getAsString());

        assertTrue(json.has("approvedCommitId"));
        assertTrue(json.get("approvedCommitId").isJsonNull());

        assertTrue(json.has("createdAt"));
        assertEquals("2026-02-04T12:00:00Z", json.get("createdAt").getAsString());

        assertTrue(json.has("updatedAt"));
        assertEquals("2026-02-04T12:00:00Z", json.get("updatedAt").getAsString());

        HistoryRecord out = HistoryRecord.fromJson(json);
        Console.log("UT", out.toJson());

        assertNotNull(out.getStatus());
        assertEquals(HistoryStatus.PENDING, out.getStatus());
        assertNull(out.getApprovedCommitId());

        assertNotNull(out.getCreatedAt());
        assertNotNull(out.getUpdatedAt());
        assertEquals(now, out.getCreatedAt());
        assertEquals(now, out.getUpdatedAt());
    }

    @Test
    void toJson_when_accepted_sets_commitId_and_roundtrips() {
        Console.log("UT", "HistoryRecord.toJson_when_accepted_sets_commitId_and_roundtrips");

        ScorerResult sr = ScorerResult.ok("ok");
        HistoryRecord rec = new HistoryRecord(null, sr);

        Instant t1 = Instant.parse("2026-02-04T12:00:00Z");
        rec.markPending(t1);

        Instant t2 = Instant.parse("2026-02-04T12:00:10Z");
        rec.markAccepted("c-123", t2);

        JsonObject json = rec.toJson();
        Console.log("UT", json);

        assertTrue(json.has("status"));
        assertEquals("ACCEPTED", json.get("status").getAsString());

        assertTrue(json.has("approvedCommitId"));
        assertEquals("c-123", json.get("approvedCommitId").getAsString());

        assertTrue(json.has("createdAt"));
        assertEquals("2026-02-04T12:00:00Z", json.get("createdAt").getAsString());

        assertTrue(json.has("updatedAt"));
        assertEquals("2026-02-04T12:00:10Z", json.get("updatedAt").getAsString());

        HistoryRecord out = HistoryRecord.fromJson(json);
        Console.log("UT", out.toJson());

        assertNotNull(out.getStatus());
        assertEquals(HistoryStatus.ACCEPTED, out.getStatus());
        assertEquals("c-123", out.getApprovedCommitId());

        assertEquals(t1, out.getCreatedAt());
        assertEquals(t2, out.getUpdatedAt());
    }

    @Test
    void fromJson_when_status_unknown_ignores_for_forward_compat() {
        Console.log("UT", "HistoryRecord.fromJson_when_status_unknown_ignores_for_forward_compat");

        JsonObject json = new JsonObject();
        json.add("queryExecution", null);
        json.add("result", null);

        json.addProperty("status", "FUTURE_STATUS");
        json.addProperty("approvedCommitId", "c-999");
        json.addProperty("createdAt", "2026-02-04T12:00:00Z");
        json.addProperty("updatedAt", "2026-02-04T12:00:10Z");

        Console.log("UT", json);

        HistoryRecord out = HistoryRecord.fromJson(json);
        Console.log("UT", out.toJson());

        // unknown status should not explode; we ignore it (null)
        assertNull(out.getStatus());

        // other fields still parse
        assertEquals("c-999", out.getApprovedCommitId());
        assertEquals(Instant.parse("2026-02-04T12:00:00Z"), out.getCreatedAt());
        assertEquals(Instant.parse("2026-02-04T12:00:10Z"), out.getUpdatedAt());
    }

    @Test
    void markPending_when_now_null_throws_boring() {
        Console.log("UT", "HistoryRecord.markPending_when_now_null_throws_boring");

        HistoryRecord rec = new HistoryRecord(null, null);

        try {
            rec.markPending(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            Console.log("UT", "IllegalArgumentException thrown as expected");
        }
    }

    @Test
    void markAccepted_when_commitId_blank_throws_boring() {
        Console.log("UT", "HistoryRecord.markAccepted_when_commitId_blank_throws_boring");

        HistoryRecord rec = new HistoryRecord(null, null);

        try {
            rec.markAccepted("   ", Instant.parse("2026-02-04T12:00:10Z"));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            Console.log("UT", "IllegalArgumentException thrown as expected");
        }
    }

    @Test
    void markAccepted_when_now_null_throws_boring() {
        Console.log("UT", "HistoryRecord.markAccepted_when_now_null_throws_boring");

        HistoryRecord rec = new HistoryRecord(null, null);

        try {
            rec.markAccepted("c-1", null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            Console.log("UT", "IllegalArgumentException thrown as expected");
        }
    }

    @Test
    void fromJson_when_timestamps_invalid_ignores_without_exploding() {
        Console.log("UT", "HistoryRecord.fromJson_when_timestamps_invalid_ignores_without_exploding");

        JsonObject json = new JsonObject();
        json.add("queryExecution", null);
        json.add("result", null);

        json.addProperty("status", "PENDING");
        json.addProperty("approvedCommitId", "c-777");

        // garbage timestamps should be ignored (null) for forward/back compat
        json.addProperty("createdAt", "not-a-timestamp");
        json.addProperty("updatedAt", "also-not-a-timestamp");

        Console.log("UT", json);

        HistoryRecord out = HistoryRecord.fromJson(json);
        Console.log("UT", out.toJson());

        assertNotNull(out.getStatus());
        assertEquals(HistoryStatus.PENDING, out.getStatus());

        assertEquals("c-777", out.getApprovedCommitId());

        assertNull(out.getCreatedAt());
        assertNull(out.getUpdatedAt());
    }


}
