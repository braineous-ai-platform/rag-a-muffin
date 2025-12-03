package ai.braineous.rag.prompt.models.cgo.graph;

import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.models.cgo.graph.data.FNOFactExtractors;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class RuleValidatorTests {

    @Test
    public void testBindOk() throws Exception{
        RuleValidator validator = new RuleValidator();

        Function<Fact, Boolean> validationRule = new FNOFactExtractors.SimpleValidationRuleGenerator();

        Input input = FNOFactExtractors.okInput();

        input.getFrom().setValidationRule(validationRule);

        BindResult bindResult = validator.bind(input);

        //------assertions--------------------------
        assertTrue(bindResult.isOk(), "binding_is_ok");
    }
}
