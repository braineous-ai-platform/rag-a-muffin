package ai.braineous.rag.prompt.models.cgo.graph;

import ai.braineous.rag.prompt.models.cgo.Fact;
import ai.braineous.rag.prompt.utils.Console;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FactValidatorAdapterTests {

    @Test
    public void testValidateSimpleFactSuccess() {
        Console.log("testValidateSimpleFactSuccess", null);

        // arrange
        Fact fact = new Fact("F1", "{\"id\":\"F1\"}"); // adjust ctor to your Fact class

        GraphView view = new GraphView() {
            @Override
            public Fact getFactById(String id) {
                if ("F1".equals(id)) {
                    return fact;
                }
                return null;
            }
        };

        FactValidatorRule rule = (f, v) -> true; // always passes

        FactValidatorAdapter adapter = new FactValidatorAdapter();

        // act
        boolean result = adapter.validate(rule, fact, view);

        // assert
        assertTrue(result);
    }

    @Test
    public void testValidateFactFailsWhenIdIsNull() {
        Console.log("testValidateFactFailsWhenIdIsNull", null);

        // arrange: fact with null id
        Fact fact = new Fact(null, "{\"id\":null}"); // or whatever fits your `Fact` API

        GraphView view = new GraphView() {
            @Override
            public Fact getFactById(String id) {
                // not used in this test, can just return null
                return null;
            }
        };

        // real rule: Fact.id must not be null
        FactValidatorRule rule = (f, v) -> f != null && f.getId() != null;

        FactValidatorAdapter adapter = new FactValidatorAdapter();

        // act
        boolean result = adapter.validate(rule, fact, view);

        // assert
        assertFalse(result);
    }

    @Test
    public void testValidateFactFailsWhenNotPresentInGraphView() {
        Console.log("testValidateFactFailsWhenNotPresentInGraphView", null);

        // arrange
        Fact fact = new Fact("F_MISSING", "{\"id\":\"F_MISSING\"}");

        GraphView view = new GraphView() {
            @Override
            public Fact getFactById(String id) {
                // Simulate that this fact is NOT in the current graph
                return null;
            }
        };

        // rule: Fact must be present in GraphView by its id
        FactValidatorRule rule = (f, v) -> {
            if (f == null || f.getId() == null) {
                return false;
            }
            Fact fromView = v.getFactById(f.getId());
            return fromView != null;
        };

        FactValidatorAdapter adapter = new FactValidatorAdapter();

        // act
        boolean result = adapter.validate(rule, fact, view);

        // assert
        assertFalse(result);
    }

    @Test
    public void testValidateFactSuccessWhenPresentInGraphView() {
        Console.log("testValidateFactSuccessWhenPresentInGraphView", null);

        // arrange
        Fact fact = new Fact("F1", "{\"id\":\"F1\"}");

        GraphView view = new GraphView() {
            @Override
            public Fact getFactById(String id) {
                if ("F1".equals(id)) {
                    return fact;
                }
                return null;
            }
        };

        FactValidatorRule rule = (f, v) -> {
            if (f == null || f.getId() == null) {
                return false;
            }
            Fact fromView = v.getFactById(f.getId());
            return fromView != null;
        };

        FactValidatorAdapter adapter = new FactValidatorAdapter();

        // act
        boolean result = adapter.validate(rule, fact, view);

        // assert
        assertTrue(result);
    }
}
