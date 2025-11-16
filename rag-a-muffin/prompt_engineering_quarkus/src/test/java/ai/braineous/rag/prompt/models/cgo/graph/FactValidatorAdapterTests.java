package ai.braineous.rag.prompt.models.cgo.graph;

import ai.braineous.rag.prompt.models.cgo.Fact;
import ai.braineous.rag.prompt.utils.Console;
import org.junit.jupiter.api.Test;

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
}
