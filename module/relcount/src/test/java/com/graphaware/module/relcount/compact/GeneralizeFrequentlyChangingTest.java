package com.graphaware.module.relcount.compact;

import com.graphaware.common.description.relationship.DetachedRelationshipDescription;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.graphaware.common.description.predicate.Predicates.any;
import static com.graphaware.common.description.predicate.Predicates.equalTo;
import static com.graphaware.common.description.relationship.RelationshipDescriptionFactory.literal;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.neo4j.graphdb.Direction.INCOMING;
import static org.neo4j.graphdb.Direction.OUTGOING;

/**
 * Unit test for {@link com.graphaware.module.relcount.compact.GeneralizeFrequentlyChanging}.
 */
public class GeneralizeFrequentlyChangingTest {

    @Test
    public void shouldPreferEliminationOfFrequentlyChangingProperties() {
        Map<DetachedRelationshipDescription, Integer> cachedCounts = new HashMap<>();
        cachedCounts.put(literal("T1", INCOMING).with("k1", equalTo("v1")).with("k2", equalTo("v1")), 1);
        cachedCounts.put(literal("T1", INCOMING).with("k1", equalTo("v2")).with("k2", equalTo("v1")), 1);
        cachedCounts.put(literal("T1", OUTGOING).with("k1", equalTo("v3")).with("k2", equalTo("v2")), 1);
        cachedCounts.put(literal("T1", OUTGOING).with("k1", equalTo("v4")).with("k2", equalTo("v2")), 1);

        DetachedRelationshipDescription result = new GeneralizeFrequentlyChanging().produceGeneralization(cachedCounts);
        assertTrue(literal("T1", INCOMING).with("k1", any()).with("k2", equalTo("v1")).equals(result)
                || literal("T1", OUTGOING).with("k1", any()).with("k2", equalTo("v2")).equals(result));
    }

    @Test
    public void shouldPreferEliminationOfTypeWithMoreCachedCounts() {
        Map<DetachedRelationshipDescription, Integer> cachedCounts = new HashMap<>();
        cachedCounts.put(literal("T1", OUTGOING).with("k1", equalTo("v1")).with("k2", equalTo("v1")), 1);
        cachedCounts.put(literal("T1", OUTGOING).with("k1", equalTo("v2")).with("k2", equalTo("v1")), 1);
        cachedCounts.put(literal("T2", OUTGOING).with("k1", equalTo("v1")).with("k2", equalTo("v1")), 1);
        cachedCounts.put(literal("T2", OUTGOING).with("k1", equalTo("v2")).with("k2", equalTo("v1")), 1);
        cachedCounts.put(literal("T2", OUTGOING).with("k1", equalTo("v3")).with("k2", equalTo("v2")), 1);
        cachedCounts.put(literal("T2", OUTGOING).with("k1", equalTo("v4")).with("k2", equalTo("v2")), 1);

        DetachedRelationshipDescription result = new GeneralizeFrequentlyChanging().produceGeneralization(cachedCounts);
        assertTrue(literal("T2", OUTGOING).with("k1", any()).with("k2", equalTo("v1")).equals(result)
                || literal("T2", OUTGOING).with("k1", any()).with("k2", equalTo("v2")).equals(result));
    }

    @Test
    public void shouldPreferEliminationOfTypeWithMoreRelationships() {
        Map<DetachedRelationshipDescription, Integer> cachedCounts = new HashMap<>();
        cachedCounts.put(literal("T1", OUTGOING).with("k1", equalTo("v1")).with("k2", equalTo("v1")), 2);
        cachedCounts.put(literal("T1", OUTGOING).with("k1", equalTo("v2")).with("k2", equalTo("v1")), 2);
        cachedCounts.put(literal("T1", OUTGOING).with("k1", equalTo("v3")).with("k2", equalTo("v2")), 2);
        cachedCounts.put(literal("T1", OUTGOING).with("k1", equalTo("v4")).with("k2", equalTo("v2")), 2);
        cachedCounts.put(literal("T2", OUTGOING).with("k1", equalTo("v1")).with("k2", equalTo("v1")), 3);
        cachedCounts.put(literal("T2", OUTGOING).with("k1", equalTo("v2")).with("k2", equalTo("v1")), 3);
        cachedCounts.put(literal("T2", OUTGOING).with("k1", equalTo("v3")).with("k2", equalTo("v2")), 3);
        cachedCounts.put(literal("T2", OUTGOING).with("k1", equalTo("v4")).with("k2", equalTo("v2")), 3);
        cachedCounts.put(literal("T3", OUTGOING).with("k1", equalTo("v1")).with("k2", equalTo("v1")), 1);
        cachedCounts.put(literal("T3", OUTGOING).with("k1", equalTo("v2")).with("k2", equalTo("v1")), 1);
        cachedCounts.put(literal("T3", OUTGOING).with("k1", equalTo("v3")).with("k2", equalTo("v2")), 1);
        cachedCounts.put(literal("T3", OUTGOING).with("k1", equalTo("v4")).with("k2", equalTo("v2")), 1);

        DetachedRelationshipDescription result = new GeneralizeFrequentlyChanging().produceGeneralization(cachedCounts);
        assertTrue(literal("T3", OUTGOING).with("k1", any()).with("k2", equalTo("v1")).equals(result)
                || literal("T3", OUTGOING).with("k1", any()).with("k2", equalTo("v2")).equals(result));
    }

    @Test
    public void shouldPreferEliminationOfAlreadyEliminatedProperties() {
        Map<DetachedRelationshipDescription, Integer> cachedCounts = new HashMap<>();
        cachedCounts.put(literal("T1", OUTGOING).with("k1", any()).with("k2", equalTo("v1")), 2);
        cachedCounts.put(literal("T1", OUTGOING).with("k1", equalTo("v3")).with("k2", equalTo("v2")), 1);
        cachedCounts.put(literal("T1", OUTGOING).with("k1", equalTo("v4")).with("k2", equalTo("v2")), 1);

        DetachedRelationshipDescription result = new GeneralizeFrequentlyChanging().produceGeneralization(cachedCounts);
        assertEquals(literal("T1", OUTGOING).with("k1", any()).with("k2", equalTo("v2")), result);
    }

    @Test
    public void shouldTreatMissingAsUndefined() {
        Map<DetachedRelationshipDescription, Integer> cachedCounts = new HashMap<>();
        cachedCounts.put(literal("T1", INCOMING).with("k2", equalTo("v1")), 1);
        cachedCounts.put(literal("T1", INCOMING).with("k1", equalTo("v2")).with("k2", equalTo("v1")), 1);
        cachedCounts.put(literal("T1", OUTGOING).with("k1", equalTo("v3")).with("k2", equalTo("v2")).with("k3", equalTo("v3")), 1);
        cachedCounts.put(literal("T1", OUTGOING).with("k1", equalTo("v4")).with("k2", equalTo("v2")), 1);

        DetachedRelationshipDescription result = new GeneralizeFrequentlyChanging().produceGeneralization(cachedCounts);
        assertTrue(result.equals(literal("T1", INCOMING).with("k1", any()).with("k2", equalTo("v1"))));
    }
}
