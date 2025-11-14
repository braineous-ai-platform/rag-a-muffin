package ai.braineous.rag.prompt.models.cgo.graph;

import ai.braineous.rag.prompt.models.cgo.Fact;
import ai.braineous.rag.prompt.models.cgo.graph.data.FNOFactExtractors;
import ai.braineous.rag.prompt.utils.Console;
import ai.braineous.rag.prompt.utils.Resources;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

public class ValidatorTests {

    @Test
    public void testBindOk() throws Exception{
        Validator validator = new Validator();

        Input input = FNOFactExtractors.okInput();
        BindResult bindResult = validator.bind(input);

        //------assertions--------------------------
        assertTrue(bindResult.isOk(), "binding_is_ok");
    }

    //----Failure cases -----------
    @Test
    public void testBindRejectsNullInput() {
        Validator validator = new Validator();
        BindResult bindResult = validator.bind(null);
        assertFalse(bindResult.isOk(), "null_input_should_fail");
    }

    @Test
    public void testBindRejectsNullFacts() throws Exception {
        Validator validator = new Validator();

        // from = null
        Input input1 = new Input(null, FNOFactExtractors.dummyFact("Airport:DFW", "atomic"),
                FNOFactExtractors.dummyFact("Flight:F100", "relational"));
        assertFalse(validator.bind(input1).isOk(), "null_from_should_fail");

        // to = null
        Input input2 = new Input(FNOFactExtractors.dummyFact("Airport:AUS", "atomic"), null,
                FNOFactExtractors.dummyFact("Flight:F100", "relational"));
        assertFalse(validator.bind(input2).isOk(), "null_to_should_fail");

        // edge = null
        Input input3 = new Input(FNOFactExtractors.dummyFact("Airport:AUS", "atomic"),
                FNOFactExtractors.dummyFact("Airport:DFW", "atomic"), null);
        assertFalse(validator.bind(input3).isOk(), "null_edge_should_fail");
    }

    @Test
    public void testBindRejectsNonAtomicFrom() throws Exception {
        Validator validator = new Validator();
        Input input = FNOFactExtractors.inputFromExtractor(new FNOFactExtractors.NonAtomicFromExtractor());
        BindResult result = validator.bind(input);

        assertFalse(result.isOk(), "non_atomic_from_should_fail");
    }

    @Test
    public void testBindRejectsNonAtomicTo() throws Exception {
        Validator validator = new Validator();
        Input input = FNOFactExtractors.inputFromExtractor(new FNOFactExtractors.NonAtomicToExtractor());
        BindResult result = validator.bind(input);

        assertFalse(result.isOk(), "non_atomic_to_should_fail");
    }

    @Test
    public void testBindRejectsNonRelationalEdge() throws Exception {
        Validator validator = new Validator();
        Input input = FNOFactExtractors.inputFromExtractor(new FNOFactExtractors.NonRelationalEdgeExtractor());
        BindResult result = validator.bind(input);

        assertFalse(result.isOk(), "non_relational_edge_should_fail");
    }

    @Test
    public void testBindRejectsSelfEdge() throws Exception {
        Validator validator = new Validator();
        Input input = FNOFactExtractors.inputFromExtractor(new
                FNOFactExtractors.SelfEdgeExtractor());
        BindResult result = validator.bind(input);

        assertFalse(result.isOk(), "self_edge_should_fail");
    }

    @Test
    public void testBindRejectsNullModes() throws Exception {
        Validator validator = new Validator();
        Input input = FNOFactExtractors.inputFromExtractor(new FNOFactExtractors.NullModesExtractor());
        BindResult result = validator.bind(input);

        assertFalse(result.isOk(), "null_modes_should_fail");
    }
}
