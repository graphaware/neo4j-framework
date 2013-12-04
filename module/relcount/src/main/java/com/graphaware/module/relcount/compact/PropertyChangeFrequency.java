package com.graphaware.module.relcount.compact;

/**
 * Encapsulates relationship type, property key and its change frequency.
 */
class PropertyChangeFrequency implements Comparable<PropertyChangeFrequency> {
    private final String type;
    private final String property;
    private final double frequency;

    PropertyChangeFrequency(String type, String property, double frequency) {
        this.type = type;
        this.property = property;
        this.frequency = frequency;
    }

    public String getType() {
        return type;
    }

    public String getProperty() {
        return property;
    }

    @Override
    public int compareTo(PropertyChangeFrequency o) {
        int result = new Double(o.frequency).compareTo(frequency);

        if (result != 0) {
            return result;
        }

        result = type.compareTo(o.type);

        if (result != 0) {
            return result;
        }

        return property.compareTo(o.property);
    }
}
