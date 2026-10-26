package ai.braineous.rag.prompt.cgo.query;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

public class MapToJsonResponseConverter {

    public MapToJsonResponseConverter() {
    }

    public String convert(String rawResponse) {
        String extracted = this.extractResultValue(rawResponse);
        if (extracted == null) {
            return rawResponse;
        }

        JsonObject root = new JsonObject();
        root.addProperty("result", extracted);
        return root.toString();
    }

    private String extractResultValue(String rawResponse) {
        if (rawResponse == null) {
            return null;
        }

        String fromJson = this.extractFromJsonObject(rawResponse);
        if (fromJson != null) {
            return fromJson;
        }

        String fromMap = this.extractFromResultKey(rawResponse);
        if (fromMap != null) {
            return fromMap;
        }

        String stripped = this.stripMarkdownFences(rawResponse);
        if (stripped.equals(rawResponse)) {
            return null;
        }

        String fromStrippedJson = this.extractFromJsonObject(stripped);
        if (fromStrippedJson != null) {
            return fromStrippedJson;
        }

        return this.extractFromResultKey(stripped);
    }

    private String extractFromJsonObject(String candidate) {
        JsonElement parsed = null;

        try {
            parsed = JsonParser.parseString(candidate);
        } catch (JsonParseException e) {
            parsed = null;
        }

        if (parsed == null || !parsed.isJsonObject()) {
            return null;
        }

        JsonObject root = parsed.getAsJsonObject();
        if (!root.has("result")) {
            return null;
        }

        JsonElement result = root.get("result");
        if (result == null || result.isJsonNull() || !result.isJsonPrimitive()) {
            return null;
        }

        if (!result.getAsJsonPrimitive().isString()) {
            return null;
        }

        String value = result.getAsString();
        if (value == null || value.length() == 0) {
            return null;
        }

        return value;
    }

    private String extractFromResultKey(String candidate) {
        String found = null;

        int i = 0;
        int length = candidate.length();

        while (i < length) {
            int resultIndex = this.indexOfResultKey(candidate, i);
            if (resultIndex < 0) {
                return found;
            }

            int valueStart = this.valueStartAfterResultKey(candidate, resultIndex);
            if (valueStart < 0) {
                i = resultIndex + 6;
                continue;
            }

            String value = this.readMapValue(candidate, valueStart);
            if (value == null || value.length() == 0) {
                i = valueStart;
                continue;
            }

            if (found == null) {
                found = value;
            } else {
                if (!found.equals(value)) {
                    return null;
                }
            }

            i = valueStart;
        }

        return found;
    }

    private int indexOfResultKey(String candidate, int fromIndex) {
        int i = fromIndex;
        int length = candidate.length();

        while (i <= length - 6) {
            if (this.isResultKeyAt(candidate, i)) {
                return i;
            }
            i = i + 1;
        }

        return -1;
    }

    private boolean isResultKeyAt(String candidate, int index) {
        if (index + 6 > candidate.length()) {
            return false;
        }

        if (!"result".equals(candidate.substring(index, index + 6))) {
            return false;
        }

        if (index > 0) {
            char previous = candidate.charAt(index - 1);
            if (previous == '"' || previous == '.' || this.isIdentifierChar(previous)) {
                return false;
            }
        }

        if (index + 6 < candidate.length()) {
            char next = candidate.charAt(index + 6);
            if (this.isIdentifierChar(next) || next == '.') {
                return false;
            }
        }

        return true;
    }

    private boolean isIdentifierChar(char ch) {
        if (ch >= 'a' && ch <= 'z') {
            return true;
        }
        if (ch >= 'A' && ch <= 'Z') {
            return true;
        }
        if (ch >= '0' && ch <= '9') {
            return true;
        }
        if (ch == '_') {
            return true;
        }
        return false;
    }

    private int valueStartAfterResultKey(String candidate, int resultIndex) {
        int i = resultIndex + 6;
        int length = candidate.length();

        while (i < length) {
            char ch = candidate.charAt(i);
            if (ch == ' ' || ch == '\t') {
                i = i + 1;
            } else {
                break;
            }
        }

        if (i >= length) {
            return -1;
        }

        char separator = candidate.charAt(i);
        if (separator != '=' && separator != ':') {
            return -1;
        }

        i = i + 1;

        while (i < length) {
            char ch = candidate.charAt(i);
            if (ch == ' ' || ch == '\t') {
                i = i + 1;
            } else {
                break;
            }
        }

        return i;
    }

    private String readMapValue(String candidate, int valueStart) {
        int i = valueStart;
        int length = candidate.length();

        while (i < length) {
            char ch = candidate.charAt(i);
            if (ch == '\n' || ch == '\r' || ch == '`') {
                break;
            }
            i = i + 1;
        }

        return candidate.substring(valueStart, i).trim();
    }

    private String stripMarkdownFences(String rawResponse) {
        String trimmed = rawResponse.trim();

        if (!trimmed.startsWith("```")) {
            return rawResponse;
        }

        int firstNewline = this.indexOfNewline(trimmed);
        if (firstNewline < 0) {
            return rawResponse;
        }

        String rest = trimmed.substring(firstNewline + 1);
        int closingFence = rest.lastIndexOf("```");
        if (closingFence < 0) {
            return rest.trim();
        }

        return rest.substring(0, closingFence).trim();
    }

    private int indexOfNewline(String text) {
        int n = text.indexOf('\n');
        int r = text.indexOf('\r');

        if (n < 0) {
            return r;
        }
        if (r < 0) {
            return n;
        }
        if (n < r) {
            return n;
        }
        return r;
    }
}
