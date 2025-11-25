package ai.braineous.rag.prompt.models.cgo.graph;

import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.cgo.api.GraphView;
import ai.braineous.rag.prompt.cgo.api.Relationship;
import ai.braineous.rag.prompt.cgo.api.RelationshipValidatorRule;
import ai.braineous.rag.prompt.observe.Console;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RelationshipValidatorTests {

    @Test
    public void testValidateSimpleRelationshipSuccess() {
        Console.log("testValidateSimpleRelationshipSuccess", null);

        // arrange
        Fact from = new Fact("FROM", "{\"id\":\"FROM\"}");
        Fact to   = new Fact("TO",   "{\"id\":\"TO\"}");
        Fact edge = new Fact("EDGE", "{\"id\":\"EDGE\"}");  // relational fact for this test

        // however your Relationship is constructed in your codebase
        Relationship relationship = new Relationship(from, to, edge);

        GraphView view = new GraphView() {
            @Override
            public Fact getFactById(String id) {
                // Not used in this simple success test
                return null;
            }
        };

        RelationshipValidatorRule rule = (rel, v) -> true; // always-true rule

        RelationshipValidatorAdapter adapter = new RelationshipValidatorAdapter();

        // act
        boolean result = adapter.validate(rule, relationship, view);

        // assert
        assertTrue(result);
    }

    @Test
    public void testValidateRelationshipFailsWhenAnyPartIsNull() {
        Console.log("testValidateRelationshipFailsWhenAnyPartIsNull", null);

        // arrange: relationship with null 'from'
        Fact from = null;
        Fact to   = new Fact("TO",   "{\"id\":\"TO\"}");
        Fact edge = new Fact("EDGE", "{\"id\":\"EDGE\"}");

        Relationship relationship = new Relationship(from, to, edge);

        GraphView view = new GraphView() {
            @Override
            public Fact getFactById(String id) {
                return null; // not used in this test
            }
        };

        // rule: all parts must be non-null
        RelationshipValidatorRule rule = (rel, v) -> {
            if (rel == null) return false;
            return rel.getFrom() != null
                    && rel.getTo()   != null
                    && rel.getEdge() != null;
        };

        RelationshipValidatorAdapter adapter = new RelationshipValidatorAdapter();

        // act
        boolean result = adapter.validate(rule, relationship, view);

        // assert
        assertFalse(result);
    }

    @Test
    public void testValidateRelationshipSuccessWhenAllPartsPresent() {
        Console.log("testValidateRelationshipSuccessWhenAllPartsPresent", null);

        // arrange: fully valid relationship
        Fact from = new Fact("FROM", "{\"id\":\"FROM\"}");
        Fact to   = new Fact("TO",   "{\"id\":\"TO\"}");
        Fact edge = new Fact("EDGE", "{\"id\":\"EDGE\"}");

        Relationship relationship = new Relationship(from, to, edge);

        GraphView view = new GraphView() {
            @Override
            public Fact getFactById(String id) {
                return null; // not used in this test either
            }
        };

        RelationshipValidatorRule rule = (rel, v) -> {
            if (rel == null) return false;
            return rel.getFrom() != null
                    && rel.getTo()   != null
                    && rel.getEdge() != null;
        };

        RelationshipValidatorAdapter adapter = new RelationshipValidatorAdapter();

        // act
        boolean result = adapter.validate(rule, relationship, view);

        // assert
        assertTrue(result);
    }
}
