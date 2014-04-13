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

    public Label getLabel() {
        return label;
    }

    public DateTimeFieldType getDateTimeFieldType() {
        return dateTimeFieldType;
    }

    public Resolution getChild() {
        if (this.ordinal() >= values().length - 1) {
            LOG.error("Parent resolution " + this.toString() + " does not have children. This is a bug.");
            throw new IllegalArgumentException("Parent resolution " + this.toString() + " does not have children. This is a bug.");
        }

        return values()[this.ordinal() + 1];
    }

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

    private static Resolution findForLabel(Label label) {
        for (Resolution resolution : values()) {
            if (resolution.getLabel().name().equals(label.name())) {
                return resolution;
            }
        }

        return null;
    }
}
