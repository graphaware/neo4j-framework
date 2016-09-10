package com.graphaware.runtime.module.thirdparty;

import com.graphaware.common.representation.DetachedNode;
import com.graphaware.common.representation.DetachedRelationship;
import com.graphaware.common.representation.GraphDetachedNode;
import com.graphaware.common.representation.GraphDetachedRelationship;
import com.graphaware.writer.thirdparty.ThirdPartyWriter;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

public class DefaultThirdPartyIntegrationModule extends WriterBasedThirdPartyIntegrationModule<Long> {

    public DefaultThirdPartyIntegrationModule(String moduleId, ThirdPartyWriter writer) {
        super(moduleId, writer);
    }

    @Override
    protected DetachedRelationship<Long, ? extends DetachedNode<Long>> relationshipRepresentation(Relationship relationship) {
        return new GraphDetachedRelationship(relationship);
    }

    @Override
    protected DetachedNode<Long> nodeRepresentation(Node node) {
        return new GraphDetachedNode(node);
    }
}

