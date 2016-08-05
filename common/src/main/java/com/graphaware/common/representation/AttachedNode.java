package com.graphaware.common.representation;

import com.graphaware.common.expression.AttachedNodeExpressions;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;

import java.util.LinkedList;
import java.util.List;

import static org.neo4j.graphdb.Direction.valueOf;
import static org.neo4j.graphdb.RelationshipType.withName;

public class AttachedNode extends AttachedPropertyContainer<Node> implements AttachedNodeExpressions {

    public AttachedNode(Node node) {
        super(node);
    }

    public int getDegree() {
        return propertyContainer.getDegree();
    }

    @Override
    public int getDegree(String typeOrDirection) {
        try {
            return propertyContainer.getDegree(valueOf(typeOrDirection.toUpperCase()));
        } catch (IllegalArgumentException e) {
            return propertyContainer.getDegree(withName(typeOrDirection));
        }
    }

    @Override
    public int getDegree(String type, String direction) {
        return propertyContainer.getDegree(withName(type), valueOf(direction.toUpperCase()));
    }

    @Override
    public boolean hasLabel(String label) {
        return propertyContainer.hasLabel(Label.label(label));
    }

    @Override
    public String[] getLabels() {
        List<String> labels = new LinkedList<>();
        for (Label label : propertyContainer.getLabels()) {
            labels.add(label.name());
        }
        return labels.toArray(new String[labels.size()]);
    }


}
