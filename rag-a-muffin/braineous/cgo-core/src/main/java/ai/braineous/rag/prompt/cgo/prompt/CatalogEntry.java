package ai.braineous.rag.prompt.cgo.prompt;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;

public class CatalogEntry {

    private String queryKind;

    private JsonObject responseContract;

    private List<String> llmInstructions;

    private String description;

    private String catalogVersion;

    public CatalogEntry() {
    }
    //------------------------------------------------------

    public String getQueryKind() {
        return queryKind;
    }

    public void setQueryKind(String queryKind) {
        this.queryKind = queryKind;
    }

    public JsonObject getResponseContract() {
        return responseContract;
    }

    public void setResponseContract(JsonObject responseContract) {
        this.responseContract = responseContract;
    }

    public List<String> getLlmInstructions() {
        return llmInstructions;
    }

    public void setLlmInstructions(List<String> llmInstructions) {
        this.llmInstructions = llmInstructions;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCatalogVersion() {
        return catalogVersion;
    }

    public void setCatalogVersion(String catalogVersion) {
        this.catalogVersion = catalogVersion;
    }

    // -------------------------
// Hardening helpers
// -------------------------

    public String safeQueryKind() {
        if (this.queryKind == null) {
            return null;
        }
        String trimmed = this.queryKind.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed;
    }

    public String safeCatalogVersion() {
        if (this.catalogVersion == null) {
            return null;
        }
        String trimmed = this.catalogVersion.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed;
    }

    public List<String> safeLlmInstructions() {
        if (this.llmInstructions == null) {
            return java.util.Collections.emptyList();
        }
        return this.llmInstructions;
    }

// -------------------------
// JSON serialization
// -------------------------

    public JsonObject toJson() {
        JsonObject root = new JsonObject();

        String qk = safeQueryKind();
        if (qk != null) {
            root.addProperty("queryKind", qk);
        }

        if (this.responseContract != null) {
            // store as JSON object (deep-ish copy via parse/string to avoid shared references)
            root.add("responseContract",
                    com.google.gson.JsonParser.parseString(this.responseContract.toString()).getAsJsonObject());
        }

        JsonArray instr = new JsonArray();
        List<String> list = safeLlmInstructions();
        for (int i = 0; i < list.size(); i++) {
            String s = list.get(i);
            if (s != null) {
                String t = s.trim();
                if (!t.isEmpty()) {
                    instr.add(t);
                }
            }
        }
        root.add("llmInstructions", instr);

        if (this.description != null) {
            String d = this.description.trim();
            if (!d.isEmpty()) {
                root.addProperty("description", d);
            }
        }

        String v = safeCatalogVersion();
        if (v != null) {
            root.addProperty("catalogVersion", v);
        }

        return root;
    }

    public String toJsonString() {
        return toJson().toString();
    }

    public static CatalogEntry fromJson(JsonObject json) {
        if (json == null) {
            return null;
        }

        CatalogEntry e = new CatalogEntry();

        // queryKind
        if (json.has("queryKind") && !json.get("queryKind").isJsonNull()) {
            try {
                e.setQueryKind(json.get("queryKind").getAsString());
            } catch (RuntimeException re) {
                // ignore bad type
            }
        }

        // responseContract
        if (json.has("responseContract") && !json.get("responseContract").isJsonNull()) {
            try {
                if (json.get("responseContract").isJsonObject()) {
                    JsonObject rc = json.get("responseContract").getAsJsonObject();
                    e.setResponseContract(com.google.gson.JsonParser.parseString(rc.toString()).getAsJsonObject());
                }
            } catch (RuntimeException re) {
                // ignore bad json
            }
        }

        // llmInstructions
        if (json.has("llmInstructions") && !json.get("llmInstructions").isJsonNull()) {
            try {
                if (json.get("llmInstructions").isJsonArray()) {
                    JsonArray arr = json.get("llmInstructions").getAsJsonArray();
                    java.util.ArrayList<String> list = new java.util.ArrayList<String>();
                    for (int i = 0; i < arr.size(); i++) {
                        if (!arr.get(i).isJsonNull()) {
                            try {
                                String s = arr.get(i).getAsString();
                                if (s != null) {
                                    String t = s.trim();
                                    if (!t.isEmpty()) {
                                        list.add(t);
                                    }
                                }
                            } catch (RuntimeException re2) {
                                // skip non-string
                            }
                        }
                    }
                    e.setLlmInstructions(list);
                }
            } catch (RuntimeException re) {
                // ignore
            }
        }

        // description
        if (json.has("description") && !json.get("description").isJsonNull()) {
            try {
                e.setDescription(json.get("description").getAsString());
            } catch (RuntimeException re) {
                // ignore
            }
        }

        // catalogVersion
        if (json.has("catalogVersion") && !json.get("catalogVersion").isJsonNull()) {
            try {
                e.setCatalogVersion(json.get("catalogVersion").getAsString());
            } catch (RuntimeException re) {
                // ignore
            }
        }

        return e;
    }

    public static CatalogEntry fromJsonString(String json) {
        if (json == null) {
            return null;
        }
        String trimmed = json.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        try {
            com.google.gson.JsonElement el = com.google.gson.JsonParser.parseString(trimmed);
            if (el == null || !el.isJsonObject()) {
                return null;
            }
            return fromJson(el.getAsJsonObject());
        } catch (RuntimeException re) {
            return null;
        }
    }
}
