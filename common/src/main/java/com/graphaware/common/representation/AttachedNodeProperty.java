package com.graphaware.common.representation;

public class AttachedNodeProperty extends NodeProperty<AttachedNode> {

    public AttachedNodeProperty(String key, AttachedNode propertyContainer) {
        super(key, propertyContainer);
    }

    @Override
    public AttachedNode getNode() {
        return propertyContainer;
    }
}
