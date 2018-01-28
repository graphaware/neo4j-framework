package com.graphaware.common.representation;

public class AttachedRelationshipProperty extends RelationshipProperty<AttachedRelationship> {

    public AttachedRelationshipProperty(String key, AttachedRelationship entity) {
        super(key, entity);
    }

    @Override
    public AttachedRelationship getRelationship() {
        return entity;
    }
}
