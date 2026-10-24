package ai.braineous.rag.prompt.cgo.query;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

public class MapToJsonResponseConverter {

    private static final String RESULT_PREFIX = "result.";

    public MapToJsonResponseConverter() {
    }

    public String convert(String rawResponse) {
        JsonElement parsed = null;

        try {
            parsed = JsonParser.parseString(rawResponse);
        } catch (JsonParseException e) {
            parsed = null;
        }

        if (parsed != null && parsed.isJsonObject()) {
            return rawResponse;
        }

        String converted = this.convertMap(rawResponse);
        if (converted == null) {
            return rawResponse;
        }

        return converted;
    }

    private String convertMap(String rawResponse) {
        JsonObject result = new JsonObject();

        int start = 0;
        int i = 0;
        int length = rawResponse.length();

        while (i <= length) {
            boolean endOfLine = false;

            if (i == length) {
                endOfLine = true;
            } else {
                if (rawResponse.charAt(i) == '\n') {
                    endOfLine = true;
                }
            }

            if (endOfLine) {
                String line = rawResponse.substring(start, i);
                boolean added = this.addRecord(result, line);
                if (!added) {
                    return null;
                }
                start = i + 1;
            }

            i = i + 1;
        }

        if (!result.has("result")) {
            return null;
        }

        JsonObject root = new JsonObject();
        root.add("result", result);
        return root.toString();
    }

    private boolean addRecord(JsonObject result, String line) {
        String trimmedLine = line.trim();

        if (trimmedLine.isEmpty()) {
            return true;
        }

        int equalsIndex = trimmedLine.indexOf('=');
        int colonIndex = trimmedLine.indexOf(':');

        if (equalsIndex < 0 && colonIndex < 0) {
            return false;
        }

        int delimiterIndex = this.firstDelimiterIndex(equalsIndex, colonIndex);

        String key = trimmedLine.substring(0, delimiterIndex).trim();
        String value = trimmedLine.substring(delimiterIndex + 1).trim();

        if (key.startsWith(RESULT_PREFIX)) {
            key = key.substring(RESULT_PREFIX.length());
        }

        if ("result".equals(key)) {
            result.addProperty("result", value);
            return true;
        }

        if ("reason".equals(key)) {
            result.addProperty("reason", value);
            return true;
        }

        return true;
    }

    private int firstDelimiterIndex(int equalsIndex, int colonIndex) {
        if (equalsIndex >= 0 && colonIndex >= 0) {
            if (equalsIndex < colonIndex) {
                return equalsIndex;
            }
            return colonIndex;
        }

        if (equalsIndex >= 0) {
            return equalsIndex;
        }

        return colonIndex;
    }
}
