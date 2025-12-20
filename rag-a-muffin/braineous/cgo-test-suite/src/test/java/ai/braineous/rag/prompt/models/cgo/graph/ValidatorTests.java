package ai.braineous.rag.prompt.models.cgo.graph;

import ai.braineous.rag.prompt.cgo.api.Fact;
import ai.braineous.rag.prompt.cgo.api.Relationship;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ValidatorTests
 *
 * Covers:
 *  - bind(): null input, null facts, null modes, self-edge, atomic/relational mode rules
 *  - validateInsert/validateDelete(): fact validity rules
 *  - validateUpdate(): fact validity + snapshot existence
 *  - validateRelationship(): relationship validity + snapshot existence of nodes
 *
 * NOTE: Adjust helper builders if your Fact/Relationship/Input/BindResult are constructor-based instead of setters.
 */
public class ValidatorTests {

    // -------------------------
    // bind()
    // -------------------------

    @Test
    @DisplayName("bind: null input -> false")
    void bind_nullInput_returnsFalse() {
        Validator v = Validator.getInstance();
        BindResult r = v.bind(null);
        assertNotNull(r);
        assertFalse(r.isOk());
    }

    @Test
    @DisplayName("bind: null from/to/edge -> false")
    void bind_nullFacts_returnsFalse() {
        Validator v = Validator.getInstance();

        Input in1 = input(null, atomicFact("T1"), relationalFact("E1"));
        assertFalse(v.bind(in1).isOk());

        Input in2 = input(atomicFact("F1"), null, relationalFact("E1"));
        assertFalse(v.bind(in2).isOk());

        Input in3 = input(atomicFact("F1"), atomicFact("T1"), null);
        assertFalse(v.bind(in3).isOk());
    }

    @Test
    @DisplayName("bind: null modes -> false")
    void bind_nullModes_returnsFalse() {
        Validator v = Validator.getInstance();

        Fact from = fact("F1", null);
        Fact to   = atomicFact("T1");
        Fact edge = relationalFact("E1");

        assertFalse(v.bind(input(from, to, edge)).isOk());

        from = atomicFact("F1");
        to   = fact("T1", null);
        assertFalse(v.bind(input(from, to, edge)).isOk());

        to   = atomicFact("T1");
        edge = fact("E1", null);
        assertFalse(v.bind(input(from, to, edge)).isOk());
    }

    @Test
    @DisplayName("bind: self-edge not allowed -> false")
    void bind_selfEdge_returnsFalse() {
        Validator v = Validator.getInstance();

        Fact same = atomicFact("F1");
        Fact edge = relationalFact("E1");

        assertFalse(v.bind(input(same, same, edge)).isOk());
    }

    @Test
    @DisplayName("bind: from and to must be atomic -> false otherwise")
    void bind_fromToMustBeAtomic() {
        Validator v = Validator.getInstance();

        Fact fromRel = relationalFact("F1"); // wrong
        Fact toAtomic = atomicFact("T1");
        Fact edgeRel = relationalFact("E1");

        assertFalse(v.bind(input(fromRel, toAtomic, edgeRel)).isOk());

        Fact fromAtomic = atomicFact("F1");
        Fact toRel = relationalFact("T1"); // wrong
        assertFalse(v.bind(input(fromAtomic, toRel, edgeRel)).isOk());
    }

    @Test
    @DisplayName("bind: edge must be relational -> false otherwise")
    void bind_edgeMustBeRelational() {
        Validator v = Validator.getInstance();

        Fact from = atomicFact("F1");
        Fact to   = atomicFact("T1");
        Fact edgeAtomic = atomicFact("E1"); // wrong

        assertFalse(v.bind(input(from, to, edgeAtomic)).isOk());
    }

    @Test
    @DisplayName("bind: happy path -> true")
    void bind_happyPath_returnsTrue() {
        Validator v = Validator.getInstance();

        Fact from = atomicFact("F1");
        Fact to   = atomicFact("T1");
        Fact edge = relationalFact("E1");

        BindResult r = v.bind(input(from, to, edge));
        assertTrue(r.isOk());
    }

    // -------------------------
    // validateInsert / validateDelete
    // -------------------------

    @Test
    @DisplayName("validateInsert: null fact -> false")
    void validateInsert_nullFact_false() {
        Validator v = Validator.getInstance();
        assertFalse(v.validateInsert(null));
    }

    @Test
    @DisplayName("validateInsert: blank id -> false")
    void validateInsert_blankId_false() {
        Validator v = Validator.getInstance();
        assertFalse(v.validateInsert(fact("   ", "atomic")));
        assertFalse(v.validateInsert(fact(null, "atomic")));
    }

    @Test
    @DisplayName("validateInsert: non-atomic mode -> false")
    void validateInsert_nonAtomic_false() {
        Validator v = Validator.getInstance();
        assertFalse(v.validateInsert(fact("F1", "relational")));
        assertFalse(v.validateInsert(fact("F1", "RELATIONAL")));
        assertFalse(v.validateInsert(fact("F1", null)));
    }

    @Test
    @DisplayName("validateInsert: atomic + nonblank id -> true")
    void validateInsert_atomic_true() {
        Validator v = Validator.getInstance();
        assertTrue(v.validateInsert(atomicFact("F1")));
    }

    @Test
    @DisplayName("validateDelete: same rules as insert")
    void validateDelete_rules() {
        Validator v = Validator.getInstance();
        assertFalse(v.validateDelete(null));
        assertFalse(v.validateDelete(fact("", "atomic")));
        assertFalse(v.validateDelete(fact("F1", "relational")));
        assertTrue(v.validateDelete(atomicFact("F1")));
    }

    // -------------------------
    // validateUpdate
    // -------------------------

    @Test
    @DisplayName("validateUpdate: invalid fact -> false and snapshot not queried")
    void validateUpdate_invalidFact_false() {
        Validator v = Validator.getInstance();

        GraphSnapshot snap = mock(GraphSnapshot.class);
        Fact bad = fact("F1", "relational"); // invalid for fact rules

        assertFalse(v.validateUpdate(snap, bad));
        verifyNoInteractions(snap);
    }

    @Test
    @DisplayName("validateUpdate: valid fact but missing in snapshot -> false")
    void validateUpdate_missingInSnapshot_false() {
        Validator v = Validator.getInstance();

        GraphSnapshot snap = mock(GraphSnapshot.class);
        Fact f = atomicFact("F1");

        when(snap.doesFactExist(f)).thenReturn(false);

        assertFalse(v.validateUpdate(snap, f));
        verify(snap, times(1)).doesFactExist(f);
    }

    @Test
    @DisplayName("validateUpdate: valid fact exists in snapshot -> true")
    void validateUpdate_exists_true() {
        Validator v = Validator.getInstance();

        GraphSnapshot snap = mock(GraphSnapshot.class);
        Fact f = atomicFact("F1");

        when(snap.doesFactExist(f)).thenReturn(true);

        assertTrue(v.validateUpdate(snap, f));
        verify(snap, times(1)).doesFactExist(f);
    }

    // -------------------------
    // validateRelationship
    // -------------------------

    @Test
    @DisplayName("validateRelationship: null snapshot -> false")
    void validateRelationship_nullSnapshot_false() {
        Validator v = Validator.getInstance();

        Relationship rel = relationship(atomicFact("F1"), atomicFact("T1"), relationalFact("E1"));
        assertFalse(v.validateRelationship(null, rel));
    }

    @Test
    @DisplayName("validateRelationship: invalid relationship -> false and snapshot not queried")
    void validateRelationship_invalidRelationship_false() {
        Validator v = Validator.getInstance();

        GraphSnapshot snap = mock(GraphSnapshot.class);

        // invalid because edge is atomic (should be relational)
        Relationship bad = relationship(atomicFact("F1"), atomicFact("T1"), atomicFact("E1"));

        assertFalse(v.validateRelationship(snap, bad));
        verifyNoInteractions(snap);
    }

    @Test
    @DisplayName("validateRelationship: nodes must exist in snapshot -> false when missing")
    void validateRelationship_nodesMustExist() {
        Validator v = Validator.getInstance();

        GraphSnapshot snap = mock(GraphSnapshot.class);

        Fact from = atomicFact("F1");
        Fact to = atomicFact("T1");
        Fact edge = relationalFact("E1");
        Relationship rel = relationship(from, to, edge);

        when(snap.doesFactExist(from)).thenReturn(true);
        when(snap.doesFactExist(to)).thenReturn(false);

        assertFalse(v.validateRelationship(snap, rel));

        verify(snap, times(1)).doesFactExist(from);
        verify(snap, times(1)).doesFactExist(to);
    }

    @Test
    @DisplayName("validateRelationship: both nodes exist -> true")
    void validateRelationship_happyPath_true() {
        Validator v = Validator.getInstance();

        GraphSnapshot snap = mock(GraphSnapshot.class);

        Fact from = atomicFact("F1");
        Fact to = atomicFact("T1");
        Fact edge = relationalFact("E1");
        Relationship rel = relationship(from, to, edge);

        when(snap.doesFactExist(from)).thenReturn(true);
        when(snap.doesFactExist(to)).thenReturn(true);

        assertTrue(v.validateRelationship(snap, rel));

        verify(snap, times(1)).doesFactExist(from);
        verify(snap, times(1)).doesFactExist(to);
    }

    // -------------------------
    // Helpers (adjust to your model)
    // -------------------------

    private Fact atomicFact(String id) {
        return fact(id, "atomic");
    }

    private Fact relationalFact(String id) {
        return fact(id, "relational");
    }

    private Fact fact(String id, String mode) {
        Fact f = new Fact();
        f.setId(id);
        f.setMode(mode);
        return f;
    }

    private Input input(Fact from, Fact to, Fact edge) {
        Input in = new Input();
        in.setFrom(from);
        in.setTo(to);
        in.setEdge(edge);
        return in;
    }

    private Relationship relationship(Fact from, Fact to, Fact edge) {
        Relationship r = new Relationship();
        r.setFrom(from);
        r.setTo(to);
        r.setEdge(edge);
        return r;
    }
}
