package com.graphaware.module.relcount.compact;

import com.graphaware.common.description.relationship.DetachedRelationshipDescription;
import com.graphaware.module.relcount.cache.DegreeCachingNode;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.graphaware.common.description.predicate.Predicates.any;
import static com.graphaware.common.description.predicate.Predicates.equalTo;
import static com.graphaware.common.description.relationship.RelationshipDescriptionFactory.literal;
import static org.mockito.Mockito.*;
import static org.neo4j.graphdb.Direction.OUTGOING;

/**
 * Unit test for {@link ThresholdBasedCompactionStrategy}.
 */
public class ThresholdBasedCompactionStrategyTest {

    private DegreeCachingNode mockCachingNode;
    private GeneralizationStrategy mockGeneralizationStrategy;

    private Map<DetachedRelationshipDescription, Integer> cachedDegrees;
    private Map<DetachedRelationshipDescription, Integer> generalizedOnce;
    private Map<DetachedRelationshipDescription, Integer> generalizedTwice;

    @Before
    public void setUp() {
        mockCachingNode = mock(DegreeCachingNode.class);

        cachedDegrees = new HashMap<>();
        cachedDegrees.put(literal("TEST", OUTGOING).with("k1", equalTo("v1")), 1);
        cachedDegrees.put(literal("TEST", OUTGOING).with("k1", equalTo("v2")), 1);
        cachedDegrees.put(literal("TEST", OUTGOING).with("k2", equalTo("v1")), 1);

        generalizedOnce = new HashMap<>();
        generalizedOnce.put(literal("TEST", OUTGOING).with("k1", any()), 2);
        generalizedOnce.put(literal("TEST", OUTGOING).with("k2", equalTo("v1")), 1);

        generalizedTwice = new HashMap<>();
        generalizedTwice.put(literal("TEST", OUTGOING).with("k1", any()).with("k2", any()), 3);

        when(mockCachingNode.getCachedDegrees())
                .thenReturn(cachedDegrees)
                .thenReturn(generalizedOnce)
                .thenReturn(generalizedTwice);

        mockGeneralizationStrategy = mock(GeneralizationStrategy.class);

        when(mockGeneralizationStrategy.produceGeneralization(cachedDegrees)).thenReturn(literal("TEST", OUTGOING).with("k1", any()));

        when(mockGeneralizationStrategy.produceGeneralization(generalizedOnce)).thenReturn(literal("TEST", OUTGOING).with("k1", any()).with("k2", any()));

        when(mockGeneralizationStrategy.produceGeneralization(generalizedTwice)).thenReturn(null);
    }

    @Test
    public void noCompactionShouldOccurWhenThresholdHasNotBeenSurpassed() {
        CompactionStrategy strategy = new ThresholdBasedCompactionStrategy(3, mockGeneralizationStrategy);

        strategy.compactRelationshipCounts(mockCachingNode);

        verify(mockCachingNode).getCachedDegrees();
        verifyNoMoreInteractions(mockCachingNode, mockGeneralizationStrategy);
    }

    @Test
    public void smallestPossibleCompactionShouldOccurWhenThresholdSurpassed() {
        CompactionStrategy strategy = new ThresholdBasedCompactionStrategy(2, mockGeneralizationStrategy);

        strategy.compactRelationshipCounts(mockCachingNode);

        verify(mockCachingNode, times(2)).getCachedDegrees();
        verify(mockCachingNode).decrementDegree(literal("TEST", OUTGOING).with("k1", equalTo("v1")), 1);
        verify(mockCachingNode).decrementDegree(literal("TEST", OUTGOING).with("k1", equalTo("v2")), 1);
        verify(mockCachingNode).incrementDegree(literal("TEST", OUTGOING).with("k1", any()), 2, true);

        verify(mockGeneralizationStrategy).produceGeneralization(cachedDegrees);

        verifyNoMoreInteractions(mockCachingNode, mockGeneralizationStrategy);
    }

    @Test
    public void smallestPossibleCompactionShouldOccurWhenThresholdSurpassed2() {
        CompactionStrategy strategy = new ThresholdBasedCompactionStrategy(1, mockGeneralizationStrategy);

        strategy.compactRelationshipCounts(mockCachingNode);

        verify(mockCachingNode, times(3)).getCachedDegrees();
        verify(mockCachingNode).decrementDegree(literal("TEST", OUTGOING).with("k1", equalTo("v1")), 1);
        verify(mockCachingNode).decrementDegree(literal("TEST", OUTGOING).with("k1", equalTo("v2")), 1);
        verify(mockCachingNode).incrementDegree(literal("TEST", OUTGOING).with("k1", any()), 2, true);
        verify(mockCachingNode).decrementDegree(literal("TEST", OUTGOING).with("k1", any()), 2);
        verify(mockCachingNode).decrementDegree(literal("TEST", OUTGOING).with("k2", equalTo("v1")), 1);
        verify(mockCachingNode).incrementDegree(literal("TEST", OUTGOING).with("k1", any()).with("k2", any()), 3, true);

        verify(mockGeneralizationStrategy).produceGeneralization(cachedDegrees);
        verify(mockGeneralizationStrategy).produceGeneralization(generalizedOnce);

        verifyNoMoreInteractions(mockCachingNode, mockGeneralizationStrategy);
    }

    @Test
    public void shouldStopWhenNoFurtherCompactionPossible() {
        CompactionStrategy strategy = new ThresholdBasedCompactionStrategy(0, mockGeneralizationStrategy);

        strategy.compactRelationshipCounts(mockCachingNode);

        verify(mockCachingNode, times(3)).getCachedDegrees();

        verify(mockCachingNode).decrementDegree(literal("TEST", OUTGOING).with("k1", equalTo("v1")), 1);
        verify(mockCachingNode).decrementDegree(literal("TEST", OUTGOING).with("k1", equalTo("v2")), 1);
        verify(mockCachingNode).incrementDegree(literal("TEST", OUTGOING).with("k1", any()), 2, true);

        verify(mockCachingNode).decrementDegree(literal("TEST", OUTGOING).with("k1", any()), 2);
        verify(mockCachingNode).decrementDegree(literal("TEST", OUTGOING).with("k2", equalTo("v1")), 1);
        verify(mockCachingNode).incrementDegree(literal("TEST", OUTGOING).with("k1", any()).with("k2", any()), 3, true);

        verify(mockCachingNode).getId(); //logging

        verify(mockGeneralizationStrategy).produceGeneralization(cachedDegrees);
        verify(mockGeneralizationStrategy).produceGeneralization(generalizedOnce);
        verify(mockGeneralizationStrategy).produceGeneralization(generalizedTwice);

        verifyNoMoreInteractions(mockCachingNode, mockGeneralizationStrategy);
    }
}
