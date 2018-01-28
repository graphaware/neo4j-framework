package com.graphaware.common.representation;

public class AttachedNodeProperty extends NodeProperty<AttachedNode> {

    public AttachedNodeProperty(String key, AttachedNode entity) {
        super(key, entity);
    }

    @Override
    public AttachedNode getNode() {
        return entity;
    }
}
