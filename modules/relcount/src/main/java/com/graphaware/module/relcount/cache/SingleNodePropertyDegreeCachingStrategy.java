package com.graphaware.module.relcount.cache;

import com.graphaware.common.description.relationship.DetachedRelationshipDescription;
import org.neo4j.graphdb.Node;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.graphaware.common.description.serialize.Serializer.fromByteArray;
import static com.graphaware.common.description.serialize.Serializer.toByteArray;

/**
 * {@link DegreeCachingStrategy} that caches degrees as a single node property on the node that the degrees are for.
 * The key of the property is the prefix (runtime identifier + module prefix) and the value is the entire map of
 * degrees serialized to a byte array.
 */
public class SingleNodePropertyDegreeCachingStrategy implements DegreeCachingStrategy {

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeDegrees(Node node, String prefix, Map<DetachedRelationshipDescription, Integer> cachedDegrees, Set<DetachedRelationshipDescription> updatedDegrees, Set<DetachedRelationshipDescription> removedDegrees) {
        node.setProperty(prefix, toByteArray(cachedDegrees));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<DetachedRelationshipDescription, Integer> readDegrees(Node node, String prefix) {
        if (!node.hasProperty(prefix)) {
            return new HashMap<>();
        }

        //noinspection unchecked
        return fromByteArray((byte[]) node.getProperty(prefix), HashMap.class);
    }
}
