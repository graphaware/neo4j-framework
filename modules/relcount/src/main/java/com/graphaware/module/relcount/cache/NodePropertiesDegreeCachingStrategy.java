package com.graphaware.module.relcount.cache;

import com.graphaware.common.description.relationship.DetachedRelationshipDescription;
import com.graphaware.common.description.relationship.DetachedRelationshipDescriptionImpl;
import com.graphaware.common.description.serialize.Serializer;
import org.neo4j.graphdb.Node;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * {@link DegreeCachingStrategy} that caches degrees as node properties on the node that the degree is for. For each
 * degree with respect to a {@link DetachedRelationshipDescription}, one property is created. The key of the property
 * is the {@link DetachedRelationshipDescription} serialized to string and the value is the actual degree.
 */
public class NodePropertiesDegreeCachingStrategy implements DegreeCachingStrategy {

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeDegrees(Node node, String prefix, Map<DetachedRelationshipDescription, Integer> cachedDegrees, Set<DetachedRelationshipDescription> updatedDegrees, Set<DetachedRelationshipDescription> removedDegrees) {
        for (DetachedRelationshipDescription updated : updatedDegrees) {
            node.setProperty(Serializer.toString(updated, prefix), cachedDegrees.get(updated));
        }

        for (DetachedRelationshipDescription removed : removedDegrees) {
            node.removeProperty(Serializer.toString(removed, prefix));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<DetachedRelationshipDescription, Integer> readDegrees(Node node, String prefix) {
        Map<DetachedRelationshipDescription, Integer> cachedCounts = new HashMap<>();

        for (String key : node.getPropertyKeys()) {
            if (key.startsWith(prefix)) {
                cachedCounts.put(Serializer.fromString(key, DetachedRelationshipDescriptionImpl.class, prefix), (Integer) node.getProperty(key));
            }
        }

        return cachedCounts;
    }
}
