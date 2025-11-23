package ai.braineous.rag.prompt.models.cgo.graph;

import ai.braineous.rag.prompt.cgo.api.BusinessRule;
import ai.braineous.rag.prompt.cgo.api.GraphView;
import ai.braineous.rag.prompt.cgo.api.WorldMutation;
import ai.braineous.rag.prompt.observe.Console;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

import ai.braineous.rag.prompt.cgo.api.Fact;

public class RulepackTests {

    //@Test
    public void testExecuteAggregatesProposalsFromAllRules() {
        Console.log("testExecuteAggregatesProposalsFromAllRules", null);

        // arrange
        Rulepack rulepack = new Rulepack();

        WorldMutation p1 = new WorldMutation();// adjust ctor as needed
        WorldMutation p2 = new WorldMutation(); // adjust ctor as needed

        BusinessRule rule1 = view -> p1;
        BusinessRule rule2 = view -> p2;

        List<BusinessRule> rules = new ArrayList<>();
        rules.add(rule1);
        rules.add(rule2);

        rulepack.setRules(rules);

        GraphView view = new GraphView() {
            @Override
            public Fact getFactById(String id) {
                // for this test, we don't care about lookup
                return null;
            }
        };

        // act
        Set<Proposal> proposals = rulepack.execute(view);

        // assert
        assertNotNull(proposals);
        assertEquals(2, proposals.size());
        assertTrue(proposals.contains(p1));
        assertTrue(proposals.contains(p2));
    }

    //@Test
    public void testExecutePropagatesExceptionFromRule() {
        Console.log("testExecutePropagatesExceptionFromRule", null);

        // arrange
        Rulepack rulepack = new Rulepack();

        BusinessRule badRule = view -> {
            throw new RuntimeException("boom while generating delta");
        };

        List<BusinessRule> rules = new ArrayList<>();
        rules.add(badRule);

        rulepack.setRules(rules);

        GraphView view = new GraphView() {
            @Override
            public Fact getFactById(String id) {
                return null;
            }
        };

        // act + assert (expected = RuntimeException)
        rulepack.execute(view);
    }
}
