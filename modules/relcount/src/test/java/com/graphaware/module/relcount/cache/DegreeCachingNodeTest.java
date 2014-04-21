package com.graphaware.module.relcount.cache;

import com.graphaware.common.description.predicate.Predicates;
import com.graphaware.common.description.relationship.DetachedRelationshipDescription;
import com.graphaware.module.relcount.RelationshipCountConfiguration;
import com.graphaware.module.relcount.compact.CompactionStrategy;
import com.graphaware.runtime.NeedsInitializationException;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.Node;

import java.util.*;

import static com.graphaware.common.description.relationship.RelationshipDescriptionFactory.literal;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.neo4j.graphdb.Direction.INCOMING;
import static org.neo4j.graphdb.Direction.OUTGOING;

/**
 * Unit test for {@link DegreeCachingNode}.
 */
public class DegreeCachingNodeTest {

    private static final String TEST_PREFIX = "test_prefix";

    private Node mockNode;
    private RelationshipCountConfiguration mockStrategies;
    private DegreeCachingStrategy mockDegreeCachingStrategy;
    private CompactionStrategy mockCompactionStrategy;

    @Before
    public void setUp() {
        mockNode = mock(Node.class);
        mockStrategies = mock(RelationshipCountConfiguration.class);

        mockDegreeCachingStrategy = mock(DegreeCachingStrategy.class);
        mockCompactionStrategy = mock(CompactionStrategy.class);

        when(mockStrategies.getDegreeCachingStrategy()).thenReturn(mockDegreeCachingStrategy);
        when(mockStrategies.getCompactionStrategy()).thenReturn(mockCompactionStrategy);
    }

    @Test
    public void correctCachedCountsShouldBeReturned() {
        Map<DetachedRelationshipDescription, Integer> cachedCounts = Collections.singletonMap(literal("TEST", OUTGOING), 1);
        when(mockDegreeCachingStrategy.readDegrees(mockNode, TEST_PREFIX)).thenReturn(cachedCounts);

        DegreeCachingNode cachingNode = new DegreeCachingNode(mockNode, TEST_PREFIX, mockStrategies);

        Map<DetachedRelationshipDescription, Integer> result = cachingNode.getCachedDegrees();

        assertEquals(1, result.size());
        assertEquals(1, (int) result.get(literal("TEST", OUTGOING)));

        verify(mockDegreeCachingStrategy).readDegrees(mockNode, TEST_PREFIX);
        verify(mockStrategies).getDegreeCachingStrategy();
        verifyNoMoreInteractions(mockDegreeCachingStrategy, mockCompactionStrategy, mockNode, mockStrategies);
    }

    @Test
    public void correctNodeIdShouldBeReturned() {
        when(mockDegreeCachingStrategy.readDegrees(mockNode, TEST_PREFIX)).thenReturn(Collections.<DetachedRelationshipDescription, Integer>emptyMap());
        when(mockNode.getId()).thenReturn(123L);

        DegreeCachingNode cachingNode = new DegreeCachingNode(mockNode, TEST_PREFIX, mockStrategies);

        assertEquals(123L, cachingNode.getId());

        verify(mockDegreeCachingStrategy).readDegrees(mockNode, TEST_PREFIX);
        verify(mockStrategies).getDegreeCachingStrategy();
        verify(mockNode).getId();
        verifyNoMoreInteractions(mockDegreeCachingStrategy, mockCompactionStrategy, mockNode, mockStrategies);
    }

    @Test
    public void incrementingNonExistingCountShouldTriggerCompaction() {
        Map<DetachedRelationshipDescription, Integer> cachedCounts = Collections.singletonMap(literal("TEST", OUTGOING), 1);
        when(mockDegreeCachingStrategy.readDegrees(mockNode, TEST_PREFIX)).thenReturn(cachedCounts);

        DegreeCachingNode cachingNode = new DegreeCachingNode(mockNode, TEST_PREFIX, mockStrategies);

        cachingNode.incrementDegree(literal("TEST", INCOMING), 1);

        verify(mockDegreeCachingStrategy).readDegrees(mockNode, TEST_PREFIX);
        verify(mockCompactionStrategy).compactRelationshipCounts(cachingNode);
        verify(mockStrategies).getDegreeCachingStrategy();
        verify(mockStrategies).getCompactionStrategy();
        verifyNoMoreInteractions(mockDegreeCachingStrategy, mockCompactionStrategy, mockNode, mockStrategies);
    }

    @Test
    public void incrementingExistingCountShouldNotTriggerCompaction() {
        Map<DetachedRelationshipDescription, Integer> cachedCounts = Collections.singletonMap(literal("TEST", OUTGOING), 1);
        when(mockDegreeCachingStrategy.readDegrees(mockNode, TEST_PREFIX)).thenReturn(cachedCounts);

        DegreeCachingNode cachingNode = new DegreeCachingNode(mockNode, TEST_PREFIX, mockStrategies);

        cachingNode.incrementDegree(literal("TEST", OUTGOING), 1);

        verify(mockDegreeCachingStrategy).readDegrees(mockNode, TEST_PREFIX);
        verify(mockStrategies).getDegreeCachingStrategy();
        verifyNoMoreInteractions(mockDegreeCachingStrategy, mockCompactionStrategy, mockNode, mockStrategies);
    }

    @Test
    public void incrementingNonExistingCountShouldTriggerCompactionWhenNotExplicitlyPrevented() {
        Map<DetachedRelationshipDescription, Integer> cachedCounts = Collections.singletonMap(literal("TEST", OUTGOING), 1);
        when(mockDegreeCachingStrategy.readDegrees(mockNode, TEST_PREFIX)).thenReturn(cachedCounts);

        DegreeCachingNode cachingNode = new DegreeCachingNode(mockNode, TEST_PREFIX, mockStrategies);

        cachingNode.incrementDegree(literal("TEST", INCOMING), 1, false);

        verify(mockDegreeCachingStrategy).readDegrees(mockNode, TEST_PREFIX);
        verify(mockCompactionStrategy).compactRelationshipCounts(cachingNode);
        verify(mockStrategies).getDegreeCachingStrategy();
        verify(mockStrategies).getCompactionStrategy();
        verifyNoMoreInteractions(mockDegreeCachingStrategy, mockCompactionStrategy, mockNode, mockStrategies);
    }

    @Test
    public void incrementingNonExistingCountShouldNotTriggerCompactionWhenExplicitlyPrevented() {
        Map<DetachedRelationshipDescription, Integer> cachedCounts = Collections.singletonMap(literal("TEST", OUTGOING), 1);
        when(mockDegreeCachingStrategy.readDegrees(mockNode, TEST_PREFIX)).thenReturn(cachedCounts);

        DegreeCachingNode cachingNode = new DegreeCachingNode(mockNode, TEST_PREFIX, mockStrategies);

        cachingNode.incrementDegree(literal("TEST", INCOMING), 1, true);

        verify(mockDegreeCachingStrategy).readDegrees(mockNode, TEST_PREFIX);
        verify(mockStrategies).getDegreeCachingStrategy();
        verifyNoMoreInteractions(mockDegreeCachingStrategy, mockCompactionStrategy, mockNode, mockStrategies);
    }

    @Test
    public void flushShouldResultInASingleExtraCall() {
        Map<DetachedRelationshipDescription, Integer> cachedCounts = Collections.singletonMap(literal("TEST", OUTGOING), 1);
        when(mockDegreeCachingStrategy.readDegrees(mockNode, TEST_PREFIX)).thenReturn(cachedCounts);

        DegreeCachingNode cachingNode = new DegreeCachingNode(mockNode, TEST_PREFIX, mockStrategies);

        cachingNode.incrementDegree(literal("TEST", INCOMING), 1);
        cachingNode.flush();

        verify(mockDegreeCachingStrategy).readDegrees(mockNode, TEST_PREFIX);
        verify(mockDegreeCachingStrategy).writeDegrees(eq(mockNode), eq(TEST_PREFIX), anyMap(), anySet(), anySet());
        verify(mockCompactionStrategy).compactRelationshipCounts(cachingNode);
        verify(mockStrategies, times(2)).getDegreeCachingStrategy();
        verify(mockStrategies).getCompactionStrategy();
        verifyNoMoreInteractions(mockDegreeCachingStrategy, mockCompactionStrategy, mockNode, mockStrategies);
    }

    @Test
    public void incrementingNonExistingCountShouldResultInCorrectDataUponFlush() {
        Map<DetachedRelationshipDescription, Integer> cachedCounts = Collections.singletonMap(literal("TEST", OUTGOING).with("k1", Predicates.any()), 1);
        when(mockDegreeCachingStrategy.readDegrees(mockNode, TEST_PREFIX)).thenReturn(cachedCounts);

        DegreeCachingNode cachingNode = new DegreeCachingNode(mockNode, TEST_PREFIX, mockStrategies);

        cachingNode.incrementDegree(literal("TEST", INCOMING), 2);
        cachingNode.flush();

        Map<DetachedRelationshipDescription, Integer> expectedCachedCounts = new HashMap<>();
        expectedCachedCounts.put(literal("TEST", OUTGOING).with("k1", Predicates.any()), 1);
        expectedCachedCounts.put(literal("TEST", INCOMING), 2);

        Set<DetachedRelationshipDescription> expectedUpdatedCounts = new HashSet<>();
        expectedUpdatedCounts.add(literal("TEST", INCOMING));

        Set<DetachedRelationshipDescription> expectedRemovedCounts = new HashSet<>();

        verify(mockDegreeCachingStrategy).readDegrees(mockNode, TEST_PREFIX);
        verify(mockDegreeCachingStrategy).writeDegrees(mockNode, TEST_PREFIX, expectedCachedCounts, expectedUpdatedCounts, expectedRemovedCounts);
        verifyNoMoreInteractions(mockDegreeCachingStrategy);
    }

    @Test
    public void incrementingExistingCountShouldResultInCorrectDataUponFlush() {
        Map<DetachedRelationshipDescription, Integer> cachedCounts = Collections.singletonMap(literal("TEST", OUTGOING).with("k1", Predicates.any()), 1);
        when(mockDegreeCachingStrategy.readDegrees(mockNode, TEST_PREFIX)).thenReturn(cachedCounts);

        DegreeCachingNode cachingNode = new DegreeCachingNode(mockNode, TEST_PREFIX, mockStrategies);

        cachingNode.incrementDegree(literal("TEST", OUTGOING), 2);
        cachingNode.flush();

        Map<DetachedRelationshipDescription, Integer> expectedCachedCounts = new HashMap<>();
        expectedCachedCounts.put(literal("TEST", OUTGOING).with("k1", Predicates.any()), 3);

        Set<DetachedRelationshipDescription> expectedUpdatedCounts = new HashSet<>();
        expectedUpdatedCounts.add(literal("TEST", OUTGOING).with("k1", Predicates.any()));

        Set<DetachedRelationshipDescription> expectedRemovedCounts = new HashSet<>();

        verify(mockDegreeCachingStrategy).readDegrees(mockNode, TEST_PREFIX);
        verify(mockDegreeCachingStrategy).writeDegrees(mockNode, TEST_PREFIX, expectedCachedCounts, expectedUpdatedCounts, expectedRemovedCounts);
        verifyNoMoreInteractions(mockDegreeCachingStrategy);
    }

    @Test
    public void decrementingExistingCountShouldResultInCorrectDataUponFlush() {
        Map<DetachedRelationshipDescription, Integer> cachedCounts = Collections.singletonMap(literal("TEST", OUTGOING).with("k1", Predicates.any()), 3);
        when(mockDegreeCachingStrategy.readDegrees(mockNode, TEST_PREFIX)).thenReturn(cachedCounts);

        DegreeCachingNode cachingNode = new DegreeCachingNode(mockNode, TEST_PREFIX, mockStrategies);

        cachingNode.decrementDegree(literal("TEST", OUTGOING), 2);
        cachingNode.flush();

        Map<DetachedRelationshipDescription, Integer> expectedCachedCounts = new HashMap<>();
        expectedCachedCounts.put(literal("TEST", OUTGOING).with("k1", Predicates.any()), 1);

        Set<DetachedRelationshipDescription> expectedUpdatedCounts = new HashSet<>();
        expectedUpdatedCounts.add(literal("TEST", OUTGOING).with("k1", Predicates.any()));

        Set<DetachedRelationshipDescription> expectedRemovedCounts = new HashSet<>();

        verify(mockDegreeCachingStrategy).readDegrees(mockNode, TEST_PREFIX);
        verify(mockDegreeCachingStrategy).writeDegrees(mockNode, TEST_PREFIX, expectedCachedCounts, expectedUpdatedCounts, expectedRemovedCounts);
        verifyNoMoreInteractions(mockDegreeCachingStrategy);
    }

    @Test
    public void decrementingExistingCountToZeroShouldResultInCorrectDataUponFlush() {
        Map<DetachedRelationshipDescription, Integer> cachedCounts = Collections.singletonMap(literal("TEST", OUTGOING).with("k1", Predicates.any()), 3);
        when(mockDegreeCachingStrategy.readDegrees(mockNode, TEST_PREFIX)).thenReturn(cachedCounts);

        DegreeCachingNode cachingNode = new DegreeCachingNode(mockNode, TEST_PREFIX, mockStrategies);

        cachingNode.decrementDegree(literal("TEST", OUTGOING), 3);
        cachingNode.flush();

        Map<DetachedRelationshipDescription, Integer> expectedCachedCounts = new HashMap<>();

        Set<DetachedRelationshipDescription> expectedUpdatedCounts = new HashSet<>();

        Set<DetachedRelationshipDescription> expectedRemovedCounts = new HashSet<>();
        expectedRemovedCounts.add(literal("TEST", OUTGOING).with("k1", Predicates.any()));

        verify(mockDegreeCachingStrategy).readDegrees(mockNode, TEST_PREFIX);
        verify(mockDegreeCachingStrategy).writeDegrees(mockNode, TEST_PREFIX, expectedCachedCounts, expectedUpdatedCounts, expectedRemovedCounts);
        verifyNoMoreInteractions(mockDegreeCachingStrategy);
    }

    @Test(expected = NeedsInitializationException.class)
    public void decrementingExistingCountBelowZeroShouldResultInExceptionUponFlush() {
        Map<DetachedRelationshipDescription, Integer> cachedCounts = Collections.singletonMap(literal("TEST", OUTGOING).with("k1", Predicates.any()), 3);
        when(mockDegreeCachingStrategy.readDegrees(mockNode, TEST_PREFIX)).thenReturn(cachedCounts);

        DegreeCachingNode cachingNode = new DegreeCachingNode(mockNode, TEST_PREFIX, mockStrategies);

        cachingNode.decrementDegree(literal("TEST", OUTGOING), 4);
        cachingNode.flush();
    }

    @Test(expected = NeedsInitializationException.class)
    public void decrementingNonExistingCountShouldResultInExceptionUponFlush() {
        Map<DetachedRelationshipDescription, Integer> cachedCounts = Collections.singletonMap(literal("TEST", OUTGOING).with("k1", Predicates.any()), 3);
        when(mockDegreeCachingStrategy.readDegrees(mockNode, TEST_PREFIX)).thenReturn(cachedCounts);

        DegreeCachingNode cachingNode = new DegreeCachingNode(mockNode, TEST_PREFIX, mockStrategies);

        cachingNode.decrementDegree(literal("TEST", INCOMING), 1);
        cachingNode.flush();
    }
}
