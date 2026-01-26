package ai.braineous.rag.prompt.cgo.prompt;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.HashMap;
import java.util.Map;

public class CatalogSnapshot {

    private String name;
    private String version;
    private Map<String, CatalogEntry> byQueryKind = new HashMap<>();
    private String description;
    private String createdAt;
    private String updatedAt;

    //------------------------------------------


    public CatalogSnapshot() {

    }

    //-----------------------------------------

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Map<String, CatalogEntry> getByQueryKind() {
        return byQueryKind;
    }

    public void setByQueryKind(Map<String, CatalogEntry> byQueryKind) {
        this.byQueryKind = byQueryKind;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    //-------------------------
    public JsonObject toJson() {
        JsonObject root = new JsonObject();

        if (this.name != null) {
            String t = this.name.trim();
            if (!t.isEmpty()) {
                root.addProperty("name", t);
            }
        }

        if (this.version != null) {
            String t = this.version.trim();
            if (!t.isEmpty()) {
                root.addProperty("version", t);
            }
        }

        if (this.description != null) {
            String t = this.description.trim();
            if (!t.isEmpty()) {
                root.addProperty("description", t);
            }
        }

        if (this.createdAt != null) {
            String t = this.createdAt.trim();
            if (!t.isEmpty()) {
                root.addProperty("createdAt", t);
            }
        }

        if (this.updatedAt != null) {
            String t = this.updatedAt.trim();
            if (!t.isEmpty()) {
                root.addProperty("updatedAt", t);
            }
        }

        JsonObject by = new JsonObject();
        if (this.byQueryKind != null) {
            for (Map.Entry<String, CatalogEntry> me : this.byQueryKind.entrySet()) {
                if (me == null) {
                    continue;
                }
                String key = me.getKey();
                CatalogEntry ce = me.getValue();
                String normalizedKey = null;

                if (ce != null) {
                    normalizedKey = ce.safeQueryKind();
                }

                if (normalizedKey == null) {
                    if (key != null) {
                        String kt = key.trim();
                        if (!kt.isEmpty()) {
                            normalizedKey = kt;
                        }
                    }
                }

                if (normalizedKey == null) {
                    continue;
                }

                if (ce == null) {
                    continue;
                }

                JsonObject ceJson = ce.toJson();
                by.add(normalizedKey, JsonParser.parseString(ceJson.toString()).getAsJsonObject());
            }
        }
        root.add("byQueryKind", by);

        return root;
    }

    public String toJsonString() {
        return toJson().toString();
    }

    public static CatalogSnapshot fromJson(JsonObject json) {
        if (json == null) {
            return null;
        }

        CatalogSnapshot s = new CatalogSnapshot();

        if (json.has("name") && !json.get("name").isJsonNull()) {
            try {
                s.setName(json.get("name").getAsString());
            } catch (RuntimeException re) {
                // ignore
            }
        }

        if (json.has("version") && !json.get("version").isJsonNull()) {
            try {
                s.setVersion(json.get("version").getAsString());
            } catch (RuntimeException re) {
                // ignore
            }
        }

        if (json.has("description") && !json.get("description").isJsonNull()) {
            try {
                s.setDescription(json.get("description").getAsString());
            } catch (RuntimeException re) {
                // ignore
            }
        }

        if (json.has("createdAt") && !json.get("createdAt").isJsonNull()) {
            try {
                s.setCreatedAt(json.get("createdAt").getAsString());
            } catch (RuntimeException re) {
                // ignore
            }
        }

        if (json.has("updatedAt") && !json.get("updatedAt").isJsonNull()) {
            try {
                s.setUpdatedAt(json.get("updatedAt").getAsString());
            } catch (RuntimeException re) {
                // ignore
            }
        }

        Map<String, CatalogEntry> map = new HashMap<String, CatalogEntry>();

        if (json.has("byQueryKind") && !json.get("byQueryKind").isJsonNull()) {
            try {
                JsonElement el = json.get("byQueryKind");
                if (el != null && el.isJsonObject()) {
                    JsonObject obj = el.getAsJsonObject();
                    for (Map.Entry<String, JsonElement> me : obj.entrySet()) {
                        if (me == null) {
                            continue;
                        }
                        String key = me.getKey();
                        if (key == null) {
                            continue;
                        }
                        String kt = key.trim();
                        if (kt.isEmpty()) {
                            continue;
                        }

                        JsonElement ve = me.getValue();
                        if (ve == null || ve.isJsonNull() || !ve.isJsonObject()) {
                            continue;
                        }

                        CatalogEntry ce = CatalogEntry.fromJson(ve.getAsJsonObject());
                        if (ce == null) {
                            continue;
                        }

                        String normalizedKey = ce.safeQueryKind();
                        if (normalizedKey == null) {
                            normalizedKey = kt;
                        }

                        map.put(normalizedKey, ce);
                    }
                }
            } catch (RuntimeException re) {
                // ignore
            }
        }

        s.setByQueryKind(map);
        return s;
    }

    public static CatalogSnapshot fromJsonString(String json) {
        if (json == null) {
            return null;
        }
        String trimmed = json.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        try {
            JsonElement el = JsonParser.parseString(trimmed);
            if (el == null || !el.isJsonObject()) {
                return null;
            }
            return fromJson(el.getAsJsonObject());
        } catch (RuntimeException re) {
            return null;
        }
    }
}
