package com.graphaware.api.library.algo.timetree;

import org.apache.log4j.Logger;
import org.joda.time.DateTimeFieldType;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;

/**
 * Resolution of a {@link TimeTree}.
 */
public enum Resolution {

    YEAR(TimeTreeLabels.YEAR, DateTimeFieldType.year()),
    MONTH(TimeTreeLabels.MONTH, DateTimeFieldType.monthOfYear()),
    DAY(TimeTreeLabels.DAY, DateTimeFieldType.dayOfMonth()),
    HOUR(TimeTreeLabels.HOUR, DateTimeFieldType.hourOfDay()),
    MINUTE(TimeTreeLabels.MINUTE, DateTimeFieldType.minuteOfHour()),
    SECOND(TimeTreeLabels.SECOND, DateTimeFieldType.secondOfMinute()),
    MILLISECOND(TimeTreeLabels.MILLISECOND, DateTimeFieldType.millisOfSecond());

    private static final Logger LOG = Logger.getLogger(Resolution.class);

    private final Label label;
    private final DateTimeFieldType dateTimeFieldType;

    private Resolution(Label label, DateTimeFieldType dateTimeFieldType) {
        this.label = label;
        this.dateTimeFieldType = dateTimeFieldType;
    }

    /**
     * Get the label corresponding to this resolution level. Nodes representing the level will get this label.
     *
     * @return label.
     */
    public Label getLabel() {
        return label;
    }

    /**
     * Get the {@link DateTimeFieldType} corresponding to this resolution level.
     *
     * @return field type.
     */
    public DateTimeFieldType getDateTimeFieldType() {
        return dateTimeFieldType;
    }

    /**
     * Get the resolution one level below this resolution.
     *
     * @return child resolution.
     * @throws IllegalStateException if this resolution does not have children.
     */
    public Resolution getChild() {
        if (this.ordinal() >= values().length - 1) {
            LOG.error("Parent resolution " + this.toString() + " does not have children. This is a bug.");
            throw new IllegalStateException("Parent resolution " + this.toString() + " does not have children. This is a bug.");
        }

        return values()[this.ordinal() + 1];
    }

    /**
     * Find the resolution level that the given node corresponds to. The node must be from a GraphAware TimeTree and must
     * not be the root of the tree.
     *
     * @param node to find resolution for.
     * @return resolution.
     * @throws IllegalArgumentException in case the given node is not from GraphAware TimeTree or is the root.
     */
    public static Resolution findForNode(Node node) {
        for (Label label : node.getLabels()) {
            Resolution resolution = findForLabel(label);
            if (resolution != null) {
                return resolution;
            }
        }

        LOG.error("Node " + node.toString() + " does not have a corresponding resolution. This is a bug.");
        throw new IllegalArgumentException("Node " + node.toString() + " does not have a corresponding resolution. This is a bug.");
    }

    /**
     * Find the resolution corresponding to the given label.
     *
     * @param label to find the resolution for.
     * @return resolution for label, null if there is no corresponding resolution.
     */
    private static Resolution findForLabel(Label label) {
        for (Resolution resolution : values()) {
            if (resolution.getLabel().name().equals(label.name())) {
                return resolution;
            }
        }

        return null;
    }
}
