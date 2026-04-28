package ai.braineous.rag.prompt.cgo.querygen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class DeclarativeQueryCompiler {

    public JsonObject compile(String sql) {
        if (sql == null) {
            throw new IllegalArgumentException("sql must not be null");
        }

        String normalized = normalize(sql);

        String selectText = readBetween(normalized, "select ", " from ");
        String factId = readQuotedValue(normalized, "factId");
        String relatedFactIdsText = readQuotedValue(normalized, "relatedFactIds");
        JsonObject controls = readControls(normalized);

        List<String> selectFields = splitCsv(selectText);
        List<String> relatedFactIds = splitCsv(relatedFactIdsText);

        if (selectFields.isEmpty()) {
            throw new IllegalArgumentException("select must contain at least one field");
        }

        if (factId == null || factId.trim().length() == 0) {
            throw new IllegalArgumentException("factId must be provided");
        }

        String intentType = readIntentType(controls, selectFields);

        JsonObject root = new JsonObject();
        JsonObject task = new JsonObject();

        JsonObject intent = new JsonObject();
        intent.addProperty("type", intentType);
        intent.addProperty("goal", intentType);

        task.add("intent", intent);
        task.addProperty("factId", factId);
        task.add("relatedFactIds", toJsonArray(relatedFactIds));

        JsonObject constraints = new JsonObject();
        constraints.add("control", controls);
        task.add("constraints", constraints);

        task.add("select", toJsonArray(selectFields));

        root.add("task", task);

        return root;
    }

    private String normalize(String sql) {
        String trimmed = sql.trim();

        String collapsed = trimmed.replace('\n', ' ');
        collapsed = collapsed.replace('\r', ' ');
        collapsed = collapsed.replace('\t', ' ');

        while (collapsed.contains("  ")) {
            collapsed = collapsed.replace("  ", " ");
        }

        return collapsed;
    }

    private String readBetween(String text, String startToken, String endToken) {
        String lower = text.toLowerCase();

        int start = lower.indexOf(startToken);
        if (start < 0) {
            throw new IllegalArgumentException("missing " + startToken.trim());
        }

        start = start + startToken.length();

        int end = lower.indexOf(endToken, start);
        if (end < 0) {
            throw new IllegalArgumentException("missing " + endToken.trim());
        }

        String value = text.substring(start, end).trim();
        if (value.length() == 0) {
            throw new IllegalArgumentException("empty " + startToken.trim());
        }

        return value;
    }

    private String readQuotedValue(String text, String key) {
        int keyIndex = findExactKeyIndex(text, key);
        if (keyIndex < 0) {
            return null;
        }

        int equalsIndex = text.indexOf("=", keyIndex);
        if (equalsIndex < 0) {
            return null;
        }

        int firstQuote = text.indexOf("'", equalsIndex);
        if (firstQuote < 0) {
            return null;
        }

        int secondQuote = text.indexOf("'", firstQuote + 1);
        if (secondQuote < 0) {
            return null;
        }

        return text.substring(firstQuote + 1, secondQuote).trim();
    }

    private int findExactKeyIndex(String text, String key) {
        if (text == null) {
            return -1;
        }

        if (key == null) {
            return -1;
        }

        String lower = text.toLowerCase();
        String lowerKey = key.toLowerCase();

        int searchFrom = 0;

        while (searchFrom < lower.length()) {
            int index = lower.indexOf(lowerKey, searchFrom);
            if (index < 0) {
                return -1;
            }

            if (isExactKeyMatch(lower, lowerKey, index)) {
                return index;
            }

            searchFrom = index + lowerKey.length();
        }

        return -1;
    }

    private boolean isExactKeyMatch(String lowerText, String lowerKey, int index) {
        int beforeIndex = index - 1;
        int afterIndex = index + lowerKey.length();

        if (beforeIndex >= 0) {
            char before = lowerText.charAt(beforeIndex);
            if (isIdentifierChar(before)) {
                return false;
            }
        }

        while (afterIndex < lowerText.length()) {
            char current = lowerText.charAt(afterIndex);

            if (current == ' ') {
                afterIndex = afterIndex + 1;
                continue;
            }

            if (current == '=') {
                return true;
            }

            return false;
        }

        return false;
    }

    private boolean isIdentifierChar(char value) {
        if (value >= 'a' && value <= 'z') {
            return true;
        }

        if (value >= 'A' && value <= 'Z') {
            return true;
        }

        if (value >= '0' && value <= '9') {
            return true;
        }

        if (value == '_') {
            return true;
        }

        return false;
    }

    private JsonObject readControls(String text) {
        JsonObject controls = new JsonObject();

        String lower = text.toLowerCase();
        int controlIndex = lower.indexOf(" control ");
        if (controlIndex < 0) {
            return controls;
        }

        String block = text.substring(controlIndex + " control ".length()).trim();
        if (block.length() == 0) {
            return controls;
        }

        List<String> pairs = splitCsv(block);

        for (int i = 0; i < pairs.size(); i++) {
            String pair = pairs.get(i);
            int equalsIndex = pair.indexOf("=");

            if (equalsIndex < 0) {
                continue;
            }

            String controlKey = pair.substring(0, equalsIndex).trim();
            String controlValue = pair.substring(equalsIndex + 1).trim();

            controlValue = stripQuotes(controlValue);

            if (controlKey.length() == 0) {
                continue;
            }

            controls.addProperty(controlKey, controlValue);
        }

        return controls;
    }

    private String readIntentType(JsonObject controls, List<String> selectFields) {
        if (controls != null && controls.has("decision_mode")) {
            String decisionMode = controls.get("decision_mode").getAsString();
            if (decisionMode != null && decisionMode.trim().length() > 0) {
                return decisionMode;
            }
        }

        return selectFields.get(0);
    }

    private List<String> splitCsv(String csv) {
        List<String> values = new ArrayList<String>();

        if (csv == null) {
            return values;
        }

        String[] parts = csv.split(",");
        for (int i = 0; i < parts.length; i++) {
            String value = stripQuotes(parts[i].trim());

            if (value.length() == 0) {
                continue;
            }

            values.add(value);
        }

        return values;
    }

    private String stripQuotes(String value) {
        if (value == null) {
            return "";
        }

        String trimmed = value.trim();

        if (trimmed.startsWith("'") && trimmed.endsWith("'") && trimmed.length() >= 2) {
            return trimmed.substring(1, trimmed.length() - 1);
        }

        if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length() >= 2) {
            return trimmed.substring(1, trimmed.length() - 1);
        }

        return trimmed;
    }

    private JsonArray toJsonArray(List<String> values) {
        JsonArray array = new JsonArray();

        if (values == null) {
            return array;
        }

        for (int i = 0; i < values.size(); i++) {
            array.add(values.get(i));
        }

        return array;
    }
}