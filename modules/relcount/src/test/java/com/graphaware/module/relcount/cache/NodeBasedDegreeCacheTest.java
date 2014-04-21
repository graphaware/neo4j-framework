package com.graphaware.module.relcount.cache;

import com.graphaware.common.description.serialize.Serializer;
import com.graphaware.common.wrapper.NodeWrapper;
import com.graphaware.module.relcount.RelationshipCountConfiguration;
import com.graphaware.module.relcount.RelationshipCountConfigurationImpl;
import com.graphaware.module.relcount.count.WeighingStrategy;
import com.graphaware.runtime.config.DefaultRuntimeConfiguration;
import com.graphaware.runtime.config.RuntimeConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import java.util.Collections;

import static com.graphaware.common.description.predicate.Predicates.equalTo;
import static com.graphaware.common.description.relationship.RelationshipDescriptionFactory.literal;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;
import static org.neo4j.graphdb.Direction.*;
import static org.neo4j.graphdb.DynamicRelationshipType.withName;

/**
 * Unit test for {@link NodeBasedDegreeCache}.
 */
public class NodeBasedDegreeCacheTest {

    private NodeBasedDegreeCache cache; //class under test
    private DegreeCachingNode mockDegreeCachingNode;

    @Before
    public void setUp() {
        cache = new TestNodeBasedDegreeCache("TEST_ID", RelationshipCountConfigurationImpl.defaultConfiguration());
        cache.configurationChanged(DefaultRuntimeConfiguration.getInstance());

        mockDegreeCachingNode = mock(DegreeCachingNode.class);
    }

    @After
    public void tearDown() {
        try {
            cache.endCaching();
        } catch (IllegalStateException e) {
            //OK
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotBeAbleToUseBothAsDefaultDirection() {
        NodeWrapper mockNodeWrapper = mock(NodeWrapper.class);
        Relationship mockRelationship = mock(Relationship.class);

        cache.handleCreatedRelationship(mockRelationship, mockNodeWrapper, BOTH);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldNotBeAbleToHandleRelationshipsBeforeCachingStarts() {
        Node mockStartNode = mock(Node.class);
        when(mockStartNode.getId()).thenReturn(123L);

        Node mockEndNode = mock(Node.class);
        when(mockEndNode.getId()).thenReturn(124L);

        NodeWrapper mockNodeWrapper = mock(NodeWrapper.class);
        when(mockNodeWrapper.getId()).thenReturn(123L);
        when(mockNodeWrapper.getWrapped()).thenReturn(mockStartNode);

        Relationship mockRelationship = mock(Relationship.class);
        when(mockRelationship.getType()).thenReturn(withName("TEST"));
        when(mockRelationship.getStartNode()).thenReturn(mockStartNode);
        when(mockRelationship.getEndNode()).thenReturn(mockEndNode);
        when(mockRelationship.getPropertyKeys()).thenReturn(Collections.singleton("k1"));
        when(mockRelationship.getProperty("k1")).thenReturn("v1");

        cache.handleCreatedRelationship(mockRelationship, mockNodeWrapper, OUTGOING);
    }

    @Test
    public void shouldNotBeAbleToStartCachingTwice() {
        cache.startCaching();

        try {
            cache.startCaching();
            fail();
        } catch (IllegalStateException e) {
            cache.endCaching();
        }
    }

    @Test(expected = IllegalStateException.class)
    public void shouldNotBeAbleToEndCachingBeforeStarting() {
        cache.endCaching();
    }

    @Test
    public void handlingCreatedRelationshipShouldResultInDegreeIncrement() {
        Node mockStartNode = mock(Node.class);
        when(mockStartNode.getId()).thenReturn(123L);

        Node mockEndNode = mock(Node.class);
        when(mockEndNode.getId()).thenReturn(124L);

        NodeWrapper mockNodeWrapper = mock(NodeWrapper.class);
        when(mockNodeWrapper.getId()).thenReturn(123L);
        when(mockNodeWrapper.getWrapped()).thenReturn(mockStartNode);

        Relationship mockRelationship = mock(Relationship.class);
        when(mockRelationship.getType()).thenReturn(withName("TEST"));
        when(mockRelationship.getStartNode()).thenReturn(mockStartNode);
        when(mockRelationship.getEndNode()).thenReturn(mockEndNode);
        when(mockRelationship.getPropertyKeys()).thenReturn(Collections.singleton("k1"));
        when(mockRelationship.getProperty("k1")).thenReturn("v1");

        cache.startCaching();

        cache.handleCreatedRelationship(mockRelationship, mockNodeWrapper, OUTGOING);
        verify(mockDegreeCachingNode).incrementDegree(literal("TEST", OUTGOING).with("k1", equalTo("v1")), 1);
        verifyNoMoreInteractions(mockDegreeCachingNode);

        cache.endCaching();

        verify(mockDegreeCachingNode).flush();
        verifyNoMoreInteractions(mockDegreeCachingNode);
    }

    @Test
    public void handlingCreatedRelationshipShouldResultInDegreeIncrementTakingIntoAccountWeights() {
        cache = new TestNodeBasedDegreeCache("TEST_ID", RelationshipCountConfigurationImpl.defaultConfiguration().with(
                new WeighingStrategy() {
                    @Override
                    public int getRelationshipWeight(Relationship relationship, Node pointOfView) {
                        return 100;
                    }
                }
        ), true);
        cache.configurationChanged(DefaultRuntimeConfiguration.getInstance());

        Node mockStartNode = mock(Node.class);
        when(mockStartNode.getId()).thenReturn(123L);

        Node mockEndNode = mock(Node.class);
        when(mockEndNode.getId()).thenReturn(124L);

        NodeWrapper mockNodeWrapper = mock(NodeWrapper.class);
        when(mockNodeWrapper.getId()).thenReturn(123L);
        when(mockNodeWrapper.getWrapped()).thenReturn(mockStartNode);

        Relationship mockRelationship = mock(Relationship.class);
        when(mockRelationship.getType()).thenReturn(withName("TEST"));
        when(mockRelationship.getStartNode()).thenReturn(mockStartNode);
        when(mockRelationship.getEndNode()).thenReturn(mockEndNode);
        when(mockRelationship.getPropertyKeys()).thenReturn(Collections.singleton("k1"));
        when(mockRelationship.getProperty("k1")).thenReturn("v1");

        cache.startCaching();

        cache.handleCreatedRelationship(mockRelationship, mockNodeWrapper, OUTGOING);
        verify(mockDegreeCachingNode).incrementDegree(literal("TEST", OUTGOING).with("k1", equalTo("v1")), 100);

        cache.endCaching();
    }

    @Test
    public void handlingDeletedRelationshipShouldResultInDegreeDecrement() {
        Node mockStartNode = mock(Node.class);
        when(mockStartNode.getId()).thenReturn(123L);

        Node mockEndNode = mock(Node.class);
        when(mockEndNode.getId()).thenReturn(124L);

        NodeWrapper mockNodeWrapper = mock(NodeWrapper.class);
        when(mockNodeWrapper.getId()).thenReturn(123L);
        when(mockNodeWrapper.getWrapped()).thenReturn(mockStartNode);

        Relationship mockRelationship = mock(Relationship.class);
        when(mockRelationship.getType()).thenReturn(withName("TEST"));
        when(mockRelationship.getStartNode()).thenReturn(mockStartNode);
        when(mockRelationship.getEndNode()).thenReturn(mockEndNode);
        when(mockRelationship.getPropertyKeys()).thenReturn(Collections.singleton("k1"));
        when(mockRelationship.getProperty("k1")).thenReturn("v1");

        cache.startCaching();

        cache.handleDeletedRelationship(mockRelationship, mockNodeWrapper, OUTGOING);
        verify(mockDegreeCachingNode).decrementDegree(literal("TEST", OUTGOING).with("k1", equalTo("v1")), 1);
        verifyNoMoreInteractions(mockDegreeCachingNode);

        cache.endCaching();

        verify(mockDegreeCachingNode).flush();
        verifyNoMoreInteractions(mockDegreeCachingNode);
    }

    @Test
    public void handlingDeletedRelationshipShouldResultInDegreeDecrementTakingIntoAccountWeights() {
        cache = new TestNodeBasedDegreeCache("TEST_ID", RelationshipCountConfigurationImpl.defaultConfiguration().with(
                new WeighingStrategy() {
                    @Override
                    public int getRelationshipWeight(Relationship relationship, Node pointOfView) {
                        return 100;
                    }
                }
        ), true);
        cache.configurationChanged(DefaultRuntimeConfiguration.getInstance());

        Node mockStartNode = mock(Node.class);
        when(mockStartNode.getId()).thenReturn(123L);

        Node mockEndNode = mock(Node.class);
        when(mockEndNode.getId()).thenReturn(124L);

        NodeWrapper mockNodeWrapper = mock(NodeWrapper.class);
        when(mockNodeWrapper.getId()).thenReturn(123L);
        when(mockNodeWrapper.getWrapped()).thenReturn(mockStartNode);

        Relationship mockRelationship = mock(Relationship.class);
        when(mockRelationship.getType()).thenReturn(withName("TEST"));
        when(mockRelationship.getStartNode()).thenReturn(mockStartNode);
        when(mockRelationship.getEndNode()).thenReturn(mockEndNode);
        when(mockRelationship.getPropertyKeys()).thenReturn(Collections.singleton("k1"));
        when(mockRelationship.getProperty("k1")).thenReturn("v1");

        cache.startCaching();

        cache.handleDeletedRelationship(mockRelationship, mockNodeWrapper, OUTGOING);
        verify(mockDegreeCachingNode).decrementDegree(literal("TEST", OUTGOING).with("k1", equalTo("v1")), 100);

        cache.endCaching();
    }

    @Test
    public void cachedDirectionShouldNeverBeBoth() {
        Node mockNode = mock(Node.class);
        when(mockNode.getId()).thenReturn(123L);

        NodeWrapper mockNodeWrapper = mock(NodeWrapper.class);
        when(mockNodeWrapper.getId()).thenReturn(123L);
        when(mockNodeWrapper.getWrapped()).thenReturn(mockNode);

        Relationship mockRelationship = mock(Relationship.class);
        when(mockRelationship.getType()).thenReturn(withName("TEST"));
        when(mockRelationship.getStartNode()).thenReturn(mockNode);
        when(mockRelationship.getEndNode()).thenReturn(mockNode);
        when(mockRelationship.getPropertyKeys()).thenReturn(Collections.singleton("k1"));
        when(mockRelationship.getProperty("k1")).thenReturn("v1");

        cache.startCaching();
        cache.handleCreatedRelationship(mockRelationship, mockNodeWrapper, OUTGOING);
        cache.handleDeletedRelationship(mockRelationship, mockNodeWrapper, INCOMING);
        cache.endCaching();

        verify(mockDegreeCachingNode).incrementDegree(literal("TEST", OUTGOING).with("k1", equalTo("v1")), 1);
        verify(mockDegreeCachingNode).decrementDegree(literal("TEST", INCOMING).with("k1", equalTo("v1")), 1);
        verify(mockDegreeCachingNode).flush();
        verifyNoMoreInteractions(mockDegreeCachingNode);
    }

    private class TestNodeBasedDegreeCache extends NodeBasedDegreeCache {

        private final boolean doNotCheckConfiguration;

        private TestNodeBasedDegreeCache(String id, RelationshipCountConfiguration relationshipCountConfiguration) {
            this(id, relationshipCountConfiguration, false);
        }

        private TestNodeBasedDegreeCache(String id, RelationshipCountConfiguration relationshipCountConfiguration, boolean doNotCheckConfiguration) {
            super(id, relationshipCountConfiguration);
            this.doNotCheckConfiguration = doNotCheckConfiguration;
        }

        @Override
        protected DegreeCachingNode newDegreeCachingNode(Node node, String prefix, RelationshipCountConfiguration configuration) {
            mockDegreeCachingNode = mock(DegreeCachingNode.class);
            long id = node.getId();
            when(mockDegreeCachingNode.getId()).thenReturn(id);

            assertEquals(prefix, RuntimeConfiguration.GA_PREFIX + "TEST_ID" + "_");
            if (!doNotCheckConfiguration) {
                assertEquals(Serializer.toString(configuration, "test"), Serializer.toString(RelationshipCountConfigurationImpl.defaultConfiguration(), "test"));
            }

            return mockDegreeCachingNode;
        }
    }
}
