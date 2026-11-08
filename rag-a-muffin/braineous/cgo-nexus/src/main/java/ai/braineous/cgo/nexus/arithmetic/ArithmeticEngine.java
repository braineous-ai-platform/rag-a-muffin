package ai.braineous.cgo.nexus.arithmetic;

import ai.braineous.cgo.nexus.model.ResolvedGraph;
import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.models.cgo.graph.GraphSnapshot;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ArithmeticEngine {

    public BigDecimal evaluate(String expression, ResolvedGraph graph) {
        GraphSnapshot snapshot = null;
        if (graph != null) {
            snapshot = graph.getSnapshot();
        }
        List<Token> tokens = tokenize(expression);
        Node parsed = parse(tokens);
        List<String> operations = new ArrayList<String>();
        Map<String, String> bindings = new LinkedHashMap<String, String>();
        return parsed.evaluate(snapshot, operations, bindings);
    }


    public static final MathContext DECIMAL = MathContext.DECIMAL128;

    public enum TokenType {
        IDENT,
        NUMBER,
        DOT,
        PLUS,
        MINUS,
        STAR,
        SLASH,
        LPAREN,
        RPAREN
    }

    public static class Token {
        public final TokenType type;
        public final String text;

        public Token(TokenType type, String text) {
            this.type = type;
            this.text = text;
        }

        public String toString() {
            return this.type.name() + ":" + this.text;
        }
    }

    public abstract static class Node {
        public abstract String describe();

        public abstract BigDecimal evaluate(
                GraphSnapshot snapshot,
                List<String> operations,
                Map<String, String> bindings);
    }

    public static class NumberNode extends Node {
        public final BigDecimal value;

        public NumberNode(BigDecimal value) {
            this.value = value;
        }

        public String describe() {
            return "NUMBER(" + this.value.toPlainString() + ")";
        }

        public BigDecimal evaluate(
                GraphSnapshot snapshot,
                List<String> operations,
                Map<String, String> bindings) {
            operations.add("literal " + this.value.toPlainString());
            return this.value;
        }
    }

    public static class FieldRefNode extends Node {
        public final String kind;
        public final String field;

        public FieldRefNode(String kind, String field) {
            this.kind = kind;
            this.field = field;
        }

        public String describe() {
            return "FIELD(" + this.kind + "." + this.field + ")";
        }

        public BigDecimal evaluate(
                GraphSnapshot snapshot,
                List<String> operations,
                Map<String, String> bindings) {
            Fact fact = this.findKind(snapshot, this.kind);
            if (fact == null) {
                throw new IllegalStateException("no Fact for kind " + this.kind);
            }
            JsonObject json = JsonParser.parseString(fact.getText()).getAsJsonObject();
            JsonElement element = json.get(this.field);
            if (element == null || element.isJsonNull()) {
                throw new IllegalStateException(
                        "field " + this.kind + "." + this.field
                                + " missing on " + fact.getId());
            }
            BigDecimal value = element.getAsBigDecimal();
            String key = this.kind + "." + this.field;
            bindings.put(key, fact.getId() + " = " + value.toPlainString());
            operations.add("bind " + key + " from " + fact.getId()
                    + " -> " + value.toPlainString());
            return value;
        }

        private Fact findKind(GraphSnapshot snapshot, String kind) {
            Fact found = null;
            for (Map.Entry<String, Fact> entry : snapshot.nodes().entrySet()) {
                Fact fact = entry.getValue();
                if (fact == null || fact.getId() == null) {
                    continue;
                }
                if (fact.getId().startsWith(kind + ":")) {
                    if (found != null) {
                        throw new IllegalStateException(
                                "multiple Facts for kind " + kind);
                    }
                    found = fact;
                }
            }
            return found;
        }
    }

    public static class UnaryNode extends Node {
        public final String operator;
        public final Node operand;

        public UnaryNode(String operator, Node operand) {
            this.operator = operator;
            this.operand = operand;
        }

        public String describe() {
            return "UNARY(" + this.operator + ", " + this.operand.describe() + ")";
        }

        public BigDecimal evaluate(
                GraphSnapshot snapshot,
                List<String> operations,
                Map<String, String> bindings) {
            BigDecimal value = this.operand.evaluate(snapshot, operations, bindings);
            BigDecimal result;
            if ("-".equals(this.operator)) {
                result = value.negate(DECIMAL);
            } else if ("+".equals(this.operator)) {
                result = value.plus(DECIMAL);
            } else {
                throw new IllegalStateException("unknown unary operator " + this.operator);
            }
            operations.add("unary " + this.operator + " "
                    + value.toPlainString() + " = " + result.toPlainString());
            return result;
        }
    }

    public static class BinaryNode extends Node {
        public final String operator;
        public final Node left;
        public final Node right;

        public BinaryNode(String operator, Node left, Node right) {
            this.operator = operator;
            this.left = left;
            this.right = right;
        }

        public String describe() {
            return "BINARY(" + this.operator + ", "
                    + this.left.describe() + ", "
                    + this.right.describe() + ")";
        }

        public BigDecimal evaluate(
                GraphSnapshot snapshot,
                List<String> operations,
                Map<String, String> bindings) {
            BigDecimal leftValue = this.left.evaluate(snapshot, operations, bindings);
            BigDecimal rightValue = this.right.evaluate(snapshot, operations, bindings);
            BigDecimal result;
            if ("+".equals(this.operator)) {
                result = leftValue.add(rightValue, DECIMAL);
            } else if ("-".equals(this.operator)) {
                result = leftValue.subtract(rightValue, DECIMAL);
            } else if ("*".equals(this.operator)) {
                result = leftValue.multiply(rightValue, DECIMAL);
            } else if ("/".equals(this.operator)) {
                result = leftValue.divide(rightValue, DECIMAL);
            } else {
                throw new IllegalStateException("unknown operator " + this.operator);
            }
            operations.add("op " + leftValue.toPlainString() + " "
                    + this.operator + " " + rightValue.toPlainString()
                    + " = " + result.toPlainString());
            return result;
        }
    }

    private static List<Token> tokenize(String expression) {
        List<Token> tokens = new ArrayList<Token>();
        int index = 0;
        while (index < expression.length()) {
            char ch = expression.charAt(index);
            if (ch == ' ' || ch == '\n' || ch == '\r' || ch == '\t') {
                index = index + 1;
                continue;
            }
            if (ch == '(') {
                tokens.add(new Token(TokenType.LPAREN, "("));
                index = index + 1;
                continue;
            }
            if (ch == ')') {
                tokens.add(new Token(TokenType.RPAREN, ")"));
                index = index + 1;
                continue;
            }
            if (ch == '+') {
                tokens.add(new Token(TokenType.PLUS, "+"));
                index = index + 1;
                continue;
            }
            if (ch == '-') {
                tokens.add(new Token(TokenType.MINUS, "-"));
                index = index + 1;
                continue;
            }
            if (ch == '*') {
                tokens.add(new Token(TokenType.STAR, "*"));
                index = index + 1;
                continue;
            }
            if (ch == '/') {
                tokens.add(new Token(TokenType.SLASH, "/"));
                index = index + 1;
                continue;
            }
            if (ch == '.') {
                tokens.add(new Token(TokenType.DOT, "."));
                index = index + 1;
                continue;
            }
            if ((ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z') || ch == '_') {
                int start = index;
                index = index + 1;
                while (index < expression.length()) {
                    char next = expression.charAt(index);
                    if ((next >= 'A' && next <= 'Z')
                            || (next >= 'a' && next <= 'z')
                            || (next >= '0' && next <= '9')
                            || next == '_') {
                        index = index + 1;
                    } else {
                        break;
                    }
                }
                tokens.add(new Token(TokenType.IDENT, expression.substring(start, index)));
                continue;
            }
            if ((ch >= '0' && ch <= '9')) {
                int start = index;
                index = index + 1;
                while (index < expression.length()) {
                    char next = expression.charAt(index);
                    if ((next >= '0' && next <= '9') || next == '.') {
                        index = index + 1;
                    } else {
                        break;
                    }
                }
                tokens.add(new Token(TokenType.NUMBER, expression.substring(start, index)));
                continue;
            }
            throw new IllegalArgumentException("unexpected character '" + ch + "'");
        }
        return tokens;
    }

    private static Node parse(List<Token> tokens) {
        Parser parser = new Parser(tokens);
        Node node = parser.parseExpression();
        parser.expectEnd();
        return node;
    }

    private static class Parser {
        private final List<Token> tokens;
        private int index;

        private Parser(List<Token> tokens) {
            this.tokens = tokens;
            this.index = 0;
        }

        private Node parseExpression() {
            Node left = this.parseTerm();
            while (this.match(TokenType.PLUS) || this.match(TokenType.MINUS)) {
                Token operator = this.previous();
                Node right = this.parseTerm();
                left = new BinaryNode(operator.text, left, right);
            }
            return left;
        }

        private Node parseTerm() {
            Node left = this.parseFactor();
            while (this.match(TokenType.STAR) || this.match(TokenType.SLASH)) {
                Token operator = this.previous();
                Node right = this.parseFactor();
                left = new BinaryNode(operator.text, left, right);
            }
            return left;
        }

        private Node parseFactor() {
            if (this.match(TokenType.PLUS) || this.match(TokenType.MINUS)) {
                Token operator = this.previous();
                Node operand = this.parseFactor();
                return new UnaryNode(operator.text, operand);
            }
            if (this.match(TokenType.LPAREN)) {
                Node inner = this.parseExpression();
                this.consume(TokenType.RPAREN, "expected )");
                return inner;
            }
            if (this.match(TokenType.NUMBER)) {
                return new NumberNode(new BigDecimal(this.previous().text));
            }
            if (this.match(TokenType.IDENT)) {
                String kind = this.previous().text;
                this.consume(TokenType.DOT, "expected . after kind");
                this.consume(TokenType.IDENT, "expected field after .");
                String field = this.previous().text;
                return new FieldRefNode(kind, field);
            }
            throw new IllegalArgumentException("expected field, number, or (");
        }

        private boolean match(TokenType type) {
            if (this.check(type)) {
                this.index = this.index + 1;
                return true;
            }
            return false;
        }

        private boolean check(TokenType type) {
            if (this.index >= this.tokens.size()) {
                return false;
            }
            return this.tokens.get(this.index).type == type;
        }

        private Token previous() {
            return this.tokens.get(this.index - 1);
        }

        private void consume(TokenType type, String message) {
            if (!this.match(type)) {
                throw new IllegalArgumentException(message);
            }
        }

        private void expectEnd() {
            if (this.index < this.tokens.size()) {
                throw new IllegalArgumentException(
                        "unexpected token " + this.tokens.get(this.index));
            }
        }
    }
}
