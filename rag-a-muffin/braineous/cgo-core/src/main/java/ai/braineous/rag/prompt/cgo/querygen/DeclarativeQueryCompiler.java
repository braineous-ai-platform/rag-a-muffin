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

        assertProjectionFreeSelect(normalized);

        String factId = readQuotedValue(normalized, "factId");
        String relatedFactIdsText = readQuotedValue(normalized, "relatedFactIds");
        JsonObject controls = readControls(normalized);

        List<String> relatedFactIds = splitCsv(relatedFactIdsText);

        if (factId == null || factId.trim().length() == 0) {
            throw new IllegalArgumentException("factId must be provided");
        }

        JsonObject root = new JsonObject();
        JsonObject task = new JsonObject();

        JsonObject intent = new JsonObject();

        task.add("intent", intent);
        task.addProperty("factId", factId);
        task.add("relatedFactIds", toJsonArray(relatedFactIds));

        JsonObject constraints = new JsonObject();
        constraints.add("control", controls);
        task.add("constraints", constraints);

        task.add("select", new JsonArray());

        root.add("task", task);

        return root;
    }

    private String normalize(String sql) {
        String trimmed = sql.trim();
        StringBuilder collapsed = new StringBuilder();

        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        boolean lastWasSpace = false;

        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);

            if (c == '\'' && !inDoubleQuote) {
                inSingleQuote = !inSingleQuote;
                collapsed.append(c);
                lastWasSpace = false;
                continue;
            }

            if (c == '"' && !inSingleQuote) {
                inDoubleQuote = !inDoubleQuote;
                collapsed.append(c);
                lastWasSpace = false;
                continue;
            }

            if (inSingleQuote || inDoubleQuote) {
                collapsed.append(c);
                lastWasSpace = false;
                continue;
            }

            if (c == '\n' || c == '\r' || c == '\t' || c == ' ') {
                if (!lastWasSpace) {
                    collapsed.append(' ');
                    lastWasSpace = true;
                }
                continue;
            }

            collapsed.append(c);
            lastWasSpace = false;
        }

        return collapsed.toString().trim();
    }

    private void assertProjectionFreeSelect(String text) {
        String lower = text.toLowerCase();

        int selectIndex = findKeyword(lower, "select", 0);
        if (selectIndex < 0) {
            throw new IllegalArgumentException("missing select");
        }

        int afterSelect = selectIndex + "select".length();

        int fromIndex = findKeyword(lower, "from", afterSelect);
        if (fromIndex < 0) {
            throw new IllegalArgumentException("missing from");
        }

        String projection = text.substring(afterSelect, fromIndex).trim();
        if (projection.length() > 0) {
            throw new IllegalArgumentException("projection not supported");
        }
    }

    private int findKeyword(String lower, String keyword, int searchFrom) {
        int from = searchFrom;

        while (from < lower.length()) {
            int index = lower.indexOf(keyword, from);
            if (index < 0) {
                return -1;
            }

            if (isKeywordMatch(lower, keyword, index)) {
                return index;
            }

            from = index + keyword.length();
        }

        return -1;
    }

    private boolean isKeywordMatch(String lower, String keyword, int index) {
        int beforeIndex = index - 1;
        int afterIndex = index + keyword.length();

        if (beforeIndex >= 0) {
            char before = lower.charAt(beforeIndex);
            if (isIdentifierChar(before)) {
                return false;
            }
        }

        if (afterIndex < lower.length()) {
            char after = lower.charAt(afterIndex);
            if (isIdentifierChar(after)) {
                return false;
            }
        }

        return true;
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

        int controlIndex = findKeywordOutsideQuotes(text, "control", 0);
        if (controlIndex < 0) {
            return controls;
        }

        String block = text.substring(controlIndex + "control".length());
        int i = 0;

        while (i < block.length()) {
            i = skipWhitespace(block, i);
            if (i >= block.length()) {
                break;
            }

            int keyStart = i;
            while (i < block.length() && isIdentifierChar(block.charAt(i))) {
                i = i + 1;
            }

            if (i == keyStart) {
                throw new IllegalArgumentException("malformed control assignment");
            }

            String controlKey = block.substring(keyStart, i);

            i = skipWhitespace(block, i);
            if (i >= block.length() || block.charAt(i) != '=') {
                throw new IllegalArgumentException("malformed control assignment");
            }
            i = i + 1;

            i = skipWhitespace(block, i);
            if (i >= block.length()) {
                throw new IllegalArgumentException("malformed control assignment");
            }

            char quote = block.charAt(i);
            if (quote != '\'' && quote != '"') {
                throw new IllegalArgumentException("malformed control assignment");
            }
            i = i + 1;

            int valueStart = i;
            int closingQuote = -1;
            while (i < block.length()) {
                if (block.charAt(i) == quote) {
                    closingQuote = i;
                    break;
                }
                i = i + 1;
            }

            if (closingQuote < 0) {
                throw new IllegalArgumentException("malformed control assignment");
            }

            String controlValue = block.substring(valueStart, closingQuote);
            i = closingQuote + 1;

            if (!isLegalControlKey(controlKey)) {
                throw new IllegalArgumentException("unknown control key: " + controlKey);
            }

            if (controls.has(controlKey)) {
                throw new IllegalArgumentException("ambiguous control assignment");
            }

            controls.addProperty(controlKey, controlValue);

            int afterValue = skipWhitespace(block, i);
            if (afterValue < block.length() && afterValue == i) {
                throw new IllegalArgumentException("malformed control assignment");
            }
        }

        return controls;
    }

    private int findKeywordOutsideQuotes(String text, String keyword, int searchFrom) {
        if (text == null) {
            return -1;
        }

        if (keyword == null) {
            return -1;
        }

        String lower = text.toLowerCase();
        String lowerKeyword = keyword.toLowerCase();

        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        int i = searchFrom;

        while (i < text.length()) {
            char c = text.charAt(i);

            if (c == '\'' && !inDoubleQuote) {
                inSingleQuote = !inSingleQuote;
                i = i + 1;
                continue;
            }

            if (c == '"' && !inSingleQuote) {
                inDoubleQuote = !inDoubleQuote;
                i = i + 1;
                continue;
            }

            if (!inSingleQuote && !inDoubleQuote) {
                if (i + lowerKeyword.length() <= lower.length()) {
                    if (lower.startsWith(lowerKeyword, i) && isKeywordMatch(lower, lowerKeyword, i)) {
                        return i;
                    }
                }
            }

            i = i + 1;
        }

        return -1;
    }

    private int skipWhitespace(String text, int index) {
        int i = index;

        while (i < text.length()) {
            char c = text.charAt(i);
            if (c != ' ' && c != '\n' && c != '\r' && c != '\t') {
                break;
            }
            i = i + 1;
        }

        return i;
    }

    private boolean isLegalControlKey(String controlKey) {
        if ("method".equals(controlKey)) {
            return true;
        }

        if ("parameters".equals(controlKey)) {
            return true;
        }

        if ("expression".equals(controlKey)) {
            return true;
        }

        if ("spec".equals(controlKey)) {
            return true;
        }

        if ("result".equals(controlKey)) {
            return true;
        }

        return false;
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