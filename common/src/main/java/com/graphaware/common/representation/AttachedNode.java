package com.graphaware.common.representation;

import com.graphaware.common.expression.AttachedNodeExpressions;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;

import java.util.LinkedList;
import java.util.List;

import static org.neo4j.graphdb.Direction.valueOf;
import static org.neo4j.graphdb.RelationshipType.withName;

public class AttachedNode extends AttachedEntity<Node> implements AttachedNodeExpressions {

    public AttachedNode(Node node) {
        super(node);
    }

    public int getDegree() {
        return entity.getDegree();
    }

    @Override
    public int getDegree(String typeOrDirection) {
        try {
            return entity.getDegree(valueOf(typeOrDirection.toUpperCase()));
        } catch (IllegalArgumentException e) {
            return entity.getDegree(withName(typeOrDirection));
        }
    }

    @Override
    public int getDegree(String type, String direction) {
        return entity.getDegree(withName(type), valueOf(direction.toUpperCase()));
    }

    @Override
    public boolean hasLabel(String label) {
        return entity.hasLabel(Label.label(label));
    }

    @Override
    public String[] getLabels() {
        List<String> labels = new LinkedList<>();
        for (Label label : entity.getLabels()) {
            labels.add(label.name());
        }
        return labels.toArray(new String[labels.size()]);
    }


}
