package com.graphaware.module.relcount.compact;

import com.graphaware.common.description.relationship.DetachedRelationshipDescription;
import org.junit.Test;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static com.graphaware.common.description.predicate.Predicates.any;
import static com.graphaware.common.description.predicate.Predicates.equalTo;
import static com.graphaware.common.description.relationship.RelationshipDescriptionFactory.literal;
import static org.junit.Assert.*;
import static org.neo4j.graphdb.Direction.INCOMING;
import static org.neo4j.graphdb.Direction.OUTGOING;

/**
 * Unit test for {@link GeneralizationGenerator}.
 */
public class GeneralizationGeneratorTest {

    @Test
    public void shouldCorrectlyGeneralize() {
        Set<DetachedRelationshipDescription> toGeneralize = new HashSet<>();
        toGeneralize.add(literal("T1", OUTGOING).with("k1", equalTo("v1")));
        toGeneralize.add(literal("T1", OUTGOING).with("k1", equalTo("v2")));
        toGeneralize.add(literal("T2", INCOMING).with("k1", equalTo("v1")).with("k2", equalTo("v2")));

        List<PropertyChangeFrequency> propertyChangeFrequencies = new LinkedList<>();
        propertyChangeFrequencies.add(new PropertyChangeFrequency("T1", "k1", 0.9));
        propertyChangeFrequencies.add(new PropertyChangeFrequency("T2", "k1", 0.5));
        propertyChangeFrequencies.add(new PropertyChangeFrequency("T2", "k2", 0.5));

        GeneralizationGenerator generalizer = new GeneralizationGenerator(toGeneralize, propertyChangeFrequencies);

        assertEquals(literal("T1", OUTGOING).with("k1", any()), generalizer.generate());
    }

    @Test
    public void shouldCorrectlyGeneralize2() {
        Set<DetachedRelationshipDescription> toGeneralize = new HashSet<>();
        toGeneralize.add(literal("T1", OUTGOING).with("k1", equalTo("v1")));
        toGeneralize.add(literal("T1", OUTGOING).with("k1", equalTo("v2")));
        toGeneralize.add(literal("T2", INCOMING).with("k1", equalTo("v1")).with("k2", equalTo("v2")));
        toGeneralize.add(literal("T2", OUTGOING).with("k1", equalTo("v2")));

        List<PropertyChangeFrequency> propertyChangeFrequencies = new LinkedList<>();
        propertyChangeFrequencies.add(new PropertyChangeFrequency("T1", "k1", 0.9));
        propertyChangeFrequencies.add(new PropertyChangeFrequency("T2", "k1", 0.9));
        propertyChangeFrequencies.add(new PropertyChangeFrequency("T2", "k2", 0.5));

        GeneralizationGenerator generalizer = new GeneralizationGenerator(toGeneralize, propertyChangeFrequencies);

        assertEquals(literal("T1", OUTGOING).with("k1", any()), generalizer.generate());
    }

    @Test
    public void shouldCorrectlyGeneralize3() {
        Set<DetachedRelationshipDescription> toGeneralize = new HashSet<>();
        toGeneralize.add(literal("T1", OUTGOING).with("k1", any()).with("k2", equalTo("v1")));
        toGeneralize.add(literal("T1", OUTGOING).with("k1", equalTo("v3")).with("k2", equalTo("v2")));
        toGeneralize.add(literal("T1", OUTGOING).with("k1", equalTo("v4")).with("k2", equalTo("v2")));

        List<PropertyChangeFrequency> propertyChangeFrequencies = new LinkedList<>();
        propertyChangeFrequencies.add(new PropertyChangeFrequency("T1", "k1", 0.9));
        propertyChangeFrequencies.add(new PropertyChangeFrequency("T1", "k2", 0.8));

        GeneralizationGenerator generalizer = new GeneralizationGenerator(toGeneralize, propertyChangeFrequencies);

        DetachedRelationshipDescription next = generalizer.generate();
        assertTrue(literal("T1", OUTGOING).with("k1", any()).with("k2", equalTo("v2")).equals(next));
    }

    @Test
    public void shouldCorrectlyGeneralize4() {
        Set<DetachedRelationshipDescription> toGeneralize = new HashSet<>();
        toGeneralize.add(literal("T1", OUTGOING).with("k1", equalTo("v1")).with("k2", equalTo("v1")));
        toGeneralize.add(literal("T1", INCOMING).with("k1", equalTo("v1")).with("k2", equalTo("v1")));

        List<PropertyChangeFrequency> propertyChangeFrequencies = new LinkedList<>();
        propertyChangeFrequencies.add(new PropertyChangeFrequency("T1", "k1", 0.9));
        propertyChangeFrequencies.add(new PropertyChangeFrequency("T1", "k2", 0.9));

        GeneralizationGenerator generalizer = new GeneralizationGenerator(toGeneralize, propertyChangeFrequencies);

        assertNull(generalizer.generate());
    }
}
