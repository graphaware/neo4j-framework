package com.graphaware.api.library.algo.timetree;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.tooling.GlobalGraphOperations;

import java.util.Iterator;
import java.util.TimeZone;

import static com.graphaware.api.library.algo.timetree.Resolution.*;
import static com.graphaware.api.library.algo.timetree.TimeTreeLabels.TimeTreeRoot;
import static com.graphaware.api.library.algo.timetree.TimeTreeRelationshipTypes.*;
import static org.neo4j.graphdb.Direction.INCOMING;
import static org.neo4j.graphdb.Direction.OUTGOING;

/**
 * Default implementation of {@link TimeTree}. The default {@link Resolution}, if one is not explicitly provided using
 * the constructor or one of the public methods, is {@link Resolution#DAY}. The default {@link DateTimeZone}, if one
 * is not explicitly provided, is UTC.
 */
public class TimeTreeImpl implements TimeTree {
    private static final Logger LOG = Logger.getLogger(TimeTreeImpl.class);

    private static final Resolution DEFAULT_RESOLUTION = DAY;
    private static final DateTimeZone DEFAULT_TIME_ZONE = DateTimeZone.forTimeZone(TimeZone.getTimeZone("UTC"));
    protected static final String VALUE_PROPERTY = "value";

    private final GraphDatabaseService database;
    private final DateTimeZone timeZone;
    private final Resolution resolution;

    /**
     * Constructor for time tree with default {@link Resolution#DAY} resolution and default UTC timezone.
     *
     * @param database to talk to.
     */
    public TimeTreeImpl(GraphDatabaseService database) {
        this(database, DEFAULT_RESOLUTION);
    }

    /**
     * Constructor for time tree with default UTC timezone.
     *
     * @param database   to talk to.
     * @param resolution default resolution.
     */
    public TimeTreeImpl(GraphDatabaseService database, Resolution resolution) {
        this(database, DEFAULT_TIME_ZONE, resolution);
    }

    /**
     * Constructor for time tree with default {@link Resolution#DAY} resolution.
     *
     * @param database to talk to.
     * @param timeZone default time zone.
     */
    public TimeTreeImpl(GraphDatabaseService database, DateTimeZone timeZone) {
        this(database, timeZone, DAY);
    }

    /**
     * Constructor for time tree.
     *
     * @param database   to talk to.
     * @param timeZone   default time zone.
     * @param resolution default resolution.
     */
    public TimeTreeImpl(GraphDatabaseService database, DateTimeZone timeZone, Resolution resolution) {
        this.database = database;
        this.timeZone = timeZone;
        this.resolution = resolution;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node getNow() {
        return getNow(timeZone, resolution);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node getNow(DateTimeZone timeZone) {
        return getNow(timeZone, resolution);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node getNow(Resolution resolution) {
        return getNow(timeZone, resolution);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node getNow(DateTimeZone timeZone, Resolution resolution) {
        return getInstant(DateTime.now().getMillis(), timeZone, resolution);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node getInstant(long time) {
        return getInstant(time, timeZone, resolution);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node getInstant(long time, DateTimeZone timeZone) {
        return getInstant(time, timeZone, resolution);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node getInstant(long time, Resolution resolution) {
        return getInstant(time, timeZone, resolution);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node getInstant(long time, DateTimeZone timeZone, Resolution resolution) {
        DateTime dateTime = new DateTime(time, timeZone);

        Node year = findOrCreateChild(getTimeRoot(), dateTime.get(YEAR.getDateTimeFieldType()));

        return getInstant(year, dateTime, resolution);
    }

    /**
     * Get a node representing a specific time instant. If one doesn't exist, it will be created as well as any missing
     * nodes on the way down from parent (recursively).
     *
     * @param parent           parent node on path to desired instant node.
     * @param dateTime         time instant.
     * @param targetResolution target child resolution. Recursion stops when at this level.
     * @return node representing the time instant at the desired resolution level.
     */
    private Node getInstant(Node parent, DateTime dateTime, Resolution targetResolution) {
        Resolution currentResolution = findForNode(parent);

        if (currentResolution.equals(targetResolution)) {
            return parent;
        }

        Node child = findOrCreateChild(parent, dateTime.get(currentResolution.getChild().getDateTimeFieldType()));

        //recursion
        return getInstant(child, dateTime, targetResolution);
    }

    /**
     * Get the root of the time tree. Create it if it does not exist.
     *
     * @return root of the time tree.
     */
    private Node getTimeRoot() {
        Node result;

        Iterator<Node> nodeIterator = GlobalGraphOperations.at(database).getAllNodesWithLabel(TimeTreeRoot).iterator();
        if (nodeIterator.hasNext()) {
            result = nodeIterator.next();
            if (nodeIterator.hasNext()) {
                LOG.error("There is more than one time tree root!");
                throw new IllegalStateException("There is more than one time tree root!");
            }
            return result;
        }

        LOG.info("Creating time tree root");

        return database.createNode(TimeTreeRoot);
    }

    /**
     * Find a child node with value equal to the given value. If no such child exists, create one.
     *
     * @param parent parent of the node to be found or created.
     * @param value  value of the node to be found or created.
     * @return child node.
     */
    private Node findOrCreateChild(Node parent, int value) {
        Relationship firstRelationship = parent.getSingleRelationship(FIRST, OUTGOING);
        if (firstRelationship == null) {
            return createFirstChildEver(parent, value);
        }

        Node existingChild = firstRelationship.getEndNode();
        boolean isFirst = true;
        while ((int) existingChild.getProperty(VALUE_PROPERTY) < value && parent(existingChild).getId() == parent.getId()) {
            isFirst = false;
            Relationship nextRelationship = existingChild.getSingleRelationship(NEXT, OUTGOING);

            if (nextRelationship == null || parent(nextRelationship.getEndNode()).getId() != parent.getId()) {
                return createLastChild(parent, existingChild, nextRelationship == null ? null : nextRelationship.getEndNode(), value);
            }

            existingChild = nextRelationship.getEndNode();
        }

        if (existingChild.getProperty(VALUE_PROPERTY).equals(value)) {
            return existingChild;
        }

        Relationship previousRelationship = existingChild.getSingleRelationship(NEXT, INCOMING);

        if (isFirst) {
            return createFirstChild(parent, previousRelationship == null ? null : previousRelationship.getStartNode(), existingChild, value);
        }

        return createChild(parent, previousRelationship.getStartNode(), existingChild, value);
    }

    /**
     * Create the first ever child of a parent.
     *
     * @param parent to create child for.
     * @param value  value of the node to be created.
     * @return child node.
     */
    private Node createFirstChildEver(Node parent, int value) {
        if (parent.getSingleRelationship(LAST, OUTGOING) != null) { //sanity check
            LOG.error("Node ID " + parent.toString() + " has no " + FIRST.name() + " relationship, but has a " + LAST.name() + " one!");
            throw new IllegalStateException("Node ID " + parent.toString() + " has no " + FIRST.name() + " relationship, but has a " + LAST.name() + " one!");
        }

        Node previousChild = null;
        Relationship previousParentRelationship = parent.getSingleRelationship(NEXT, INCOMING);
        if (previousParentRelationship != null) {
            Relationship previousParentLastChildRelationship = previousParentRelationship.getStartNode().getSingleRelationship(LAST, OUTGOING);
            if (previousParentLastChildRelationship != null) {
                previousChild = previousParentLastChildRelationship.getEndNode();
            }
        }

        Node nextChild = null;
        Relationship nextParentRelationship = parent.getSingleRelationship(NEXT, OUTGOING);
        if (nextParentRelationship != null) {
            Relationship nextParentFirstChildRelationship = nextParentRelationship.getEndNode().getSingleRelationship(FIRST, OUTGOING);
            if (nextParentFirstChildRelationship != null) {
                nextChild = nextParentFirstChildRelationship.getEndNode();
            }
        }

        Node child = createChild(parent, previousChild, nextChild, value);

        parent.createRelationshipTo(child, FIRST);
        parent.createRelationshipTo(child, LAST);

        return child;
    }

    /**
     * Create the first child node that belongs to a specific parent. "First" is with respect to ordering, not the
     * number of nodes. In other words, the node being created is not the first parent's child, but it is the child with
     * the lowest ordering.
     *
     * @param parent        to create child for.
     * @param previousChild previous child (has different parent), or null for no such child.
     * @param nextChild     next child (has same parent).
     * @param value         value of the node to be created.
     * @return child node.
     */
    private Node createFirstChild(Node parent, Node previousChild, Node nextChild, int value) {
        Relationship firstRelationship = parent.getSingleRelationship(FIRST, OUTGOING);

        if (nextChild.getId() != firstRelationship.getEndNode().getId()) { //sanity check
            LOG.error("Node " + nextChild.toString() + " seems to be the first child of node " + parent.toString() + ", but there is no " + FIRST.name() + " relationship between the two!");
            throw new IllegalStateException("Node " + nextChild.toString() + " seems to be the first child of node " + parent.toString() + ", but there is no " + FIRST.name() + " relationship between the two!");
        }

        firstRelationship.delete();

        Node child = createChild(parent, previousChild, nextChild, value);

        parent.createRelationshipTo(child, FIRST);

        return child;
    }

    /**
     * Create the last child node that belongs to a specific parent.
     *
     * @param parent        to create child for.
     * @param previousChild previous child (has same parent).
     * @param nextChild     next child (has different parent), or null for no such child.
     * @param value         value of the node to be created.
     * @return child node.
     */
    private Node createLastChild(Node parent, Node previousChild, Node nextChild, int value) {
        Relationship lastRelationship = parent.getSingleRelationship(LAST, OUTGOING);

        Node endNode = lastRelationship.getEndNode();
        if (previousChild.getId() != endNode.getId()) { //sanity check
            LOG.error("Node " + previousChild.toString() + " seems to be the last child of node " + parent.toString() + ", but there is no " + LAST.name() + " relationship between the two!");
            throw new IllegalStateException("Node " + previousChild.toString() + " seems to be the last child of node " + parent.toString() + ", but there is no " + LAST.name() + " relationship between the two!");
        }

        lastRelationship.delete();

        Node child = createChild(parent, previousChild, nextChild, value);

        parent.createRelationshipTo(child, LAST);

        return child;
    }

    /**
     * Create a child node.
     *
     * @param parent   parent node.
     * @param previous previous node on the same level, null if the child is the first one.
     * @param next     next node on the same level, null if the child is the last one.
     * @param value    value of the child.
     * @return the newly created child.
     */
    private Node createChild(Node parent, Node previous, Node next, int value) {
        if (previous != null && next != null && next.getId() != previous.getSingleRelationship(NEXT, OUTGOING).getEndNode().getId()) {
            LOG.error("Nodes " + previous.toString() + " and " + next.toString() + " are not connected with a " + NEXT.name() + " relationship!");
            throw new IllegalArgumentException("Nodes " + previous.toString() + " and " + next.toString() + " are not connected with a " + NEXT.name() + " relationship!");
        }

        Node child = database.createNode(TimeTreeLabels.getChild(parent));
        child.setProperty(VALUE_PROPERTY, value);
        parent.createRelationshipTo(child, CHILD);

        if (previous != null) {
            Relationship nextRelationship = previous.getSingleRelationship(NEXT, OUTGOING);
            if (nextRelationship != null) {
                nextRelationship.delete();
            }
            previous.createRelationshipTo(child, NEXT);
        }

        if (next != null) {
            child.createRelationshipTo(next, NEXT);
        }

        return child;
    }

    /**
     * Find the parent of a node.
     *
     * @param node to find a parent for.
     * @return parent.
     * @throws IllegalStateException in case the node has no parent.
     */
    private Node parent(Node node) {
        Relationship parentRelationship = node.getSingleRelationship(CHILD, INCOMING);

        if (parentRelationship == null) {
            LOG.error("Node ID " + node.toString() + " has no parent!");
            throw new IllegalStateException("Node ID " + node.toString() + " has no parent!");
        }

        return parentRelationship.getStartNode();
    }
}
