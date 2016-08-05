package com.graphaware.common.representation;

public class AttachedRelationshipProperty extends RelationshipProperty<AttachedRelationship> {

    public AttachedRelationshipProperty(String key, AttachedRelationship propertyContainer) {
        super(key, propertyContainer);
    }

    @Override
    public AttachedRelationship getRelationship() {
        return propertyContainer;
    }
}
