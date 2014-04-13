package com.graphaware.api.library.algo.timetree;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.test.TestGraphDatabaseFactory;
import org.neo4j.tooling.GlobalGraphOperations;

import java.util.Iterator;
import java.util.TimeZone;

import static com.graphaware.api.library.algo.timetree.TimeTreeImpl.VALUE_PROPERTY;
import static com.graphaware.api.library.algo.timetree.TimeTreeRelationshipTypes.*;
import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static org.neo4j.graphdb.Direction.INCOMING;
import static org.neo4j.graphdb.Direction.OUTGOING;
import static org.neo4j.helpers.collection.Iterables.count;

/**
 * Unit test for {@link TimeTreeImpl}.
 */
public class TimeTreeImplTest {

    private GraphDatabaseService database;
    private TimeTree timeTree; //class under test

    private static final DateTimeZone UTC = DateTimeZone.forTimeZone(TimeZone.getTimeZone("UTC"));

    @Before
    public void setUp() {
        database = new TestGraphDatabaseFactory().newImpermanentDatabase();
        timeTree = new TimeTreeImpl(database);
    }

    @After
    public void tearDown() {
        database.shutdown();
    }

    @Test
    public void trivialTreeShouldBeCreatedWhenFirstDayIsRequested() {
        //Given
        long dateInMillis = dateToMillis(2013, 5, 4);

        //When
        Node dayNode;
        try (Transaction tx = database.beginTx()) {
            dayNode = timeTree.getInstant(dateInMillis, Resolution.DAY);
            tx.success();
        }

        //Then
        try (Transaction tx = database.beginTx()) {
            assertEquals(4, count(GlobalGraphOperations.at(database).getAllNodes()));

            assertTrue(dayNode.hasLabel(TimeTreeLabels.DAY));
            assertEquals(4, dayNode.getProperty(VALUE_PROPERTY));

            Node monthNode = dayNode.getSingleRelationship(CHILD, INCOMING).getStartNode();
            assertEquals(monthNode, dayNode.getSingleRelationship(FIRST, INCOMING).getStartNode());
            assertEquals(monthNode, dayNode.getSingleRelationship(LAST, INCOMING).getStartNode());
            assertTrue(monthNode.hasLabel(TimeTreeLabels.MONTH));
            assertEquals(5, monthNode.getProperty(VALUE_PROPERTY));
            assertNull(dayNode.getSingleRelationship(NEXT, INCOMING));
            assertNull(dayNode.getSingleRelationship(NEXT, OUTGOING));

            Node yearNode = monthNode.getSingleRelationship(CHILD, INCOMING).getStartNode();
            assertEquals(yearNode, monthNode.getSingleRelationship(FIRST, INCOMING).getStartNode());
            assertEquals(yearNode, monthNode.getSingleRelationship(LAST, INCOMING).getStartNode());
            assertTrue(yearNode.hasLabel(TimeTreeLabels.YEAR));
            assertEquals(2013, yearNode.getProperty(VALUE_PROPERTY));
            assertNull(yearNode.getSingleRelationship(NEXT, INCOMING));
            assertNull(yearNode.getSingleRelationship(NEXT, OUTGOING));

            Node rootNode = yearNode.getSingleRelationship(CHILD, INCOMING).getStartNode();
            assertEquals(rootNode, yearNode.getSingleRelationship(FIRST, INCOMING).getStartNode());
            assertEquals(rootNode, yearNode.getSingleRelationship(LAST, INCOMING).getStartNode());
            assertTrue(rootNode.hasLabel(TimeTreeLabels.ROOT));
            assertNull(rootNode.getSingleRelationship(NEXT, INCOMING));
            assertNull(rootNode.getSingleRelationship(NEXT, OUTGOING));
            assertNull(rootNode.getSingleRelationship(FIRST, INCOMING));
            assertNull(rootNode.getSingleRelationship(LAST, INCOMING));
        }
    }

    @Test
    public void trivialTreeShouldBeCreatedWhenTodayIsRequested() {
        //Given
        DateTime now = DateTime.now(UTC);

        //When
        Node dayNode;
        try (Transaction tx = database.beginTx()) {
            dayNode = timeTree.getNow(Resolution.DAY);
            tx.success();
        }

        //Then
        try (Transaction tx = database.beginTx()) {
            assertEquals(4, count(GlobalGraphOperations.at(database).getAllNodes()));

            assertTrue(dayNode.hasLabel(TimeTreeLabels.DAY));
            assertEquals(now.getDayOfMonth(), dayNode.getProperty(VALUE_PROPERTY));

            Node monthNode = dayNode.getSingleRelationship(CHILD, INCOMING).getStartNode();
            assertEquals(monthNode, dayNode.getSingleRelationship(FIRST, INCOMING).getStartNode());
            assertEquals(monthNode, dayNode.getSingleRelationship(LAST, INCOMING).getStartNode());
            assertTrue(monthNode.hasLabel(TimeTreeLabels.MONTH));
            assertEquals(now.getMonthOfYear(), monthNode.getProperty(VALUE_PROPERTY));
            assertNull(dayNode.getSingleRelationship(NEXT, INCOMING));
            assertNull(dayNode.getSingleRelationship(NEXT, OUTGOING));

            Node yearNode = monthNode.getSingleRelationship(CHILD, INCOMING).getStartNode();
            assertEquals(yearNode, monthNode.getSingleRelationship(FIRST, INCOMING).getStartNode());
            assertEquals(yearNode, monthNode.getSingleRelationship(LAST, INCOMING).getStartNode());
            assertTrue(yearNode.hasLabel(TimeTreeLabels.YEAR));
            assertEquals(now.getYear(), yearNode.getProperty(VALUE_PROPERTY));
            assertNull(yearNode.getSingleRelationship(NEXT, INCOMING));
            assertNull(yearNode.getSingleRelationship(NEXT, OUTGOING));

            Node rootNode = yearNode.getSingleRelationship(CHILD, INCOMING).getStartNode();
            assertEquals(rootNode, yearNode.getSingleRelationship(FIRST, INCOMING).getStartNode());
            assertEquals(rootNode, yearNode.getSingleRelationship(LAST, INCOMING).getStartNode());
            assertTrue(rootNode.hasLabel(TimeTreeLabels.ROOT));
            assertEquals("GraphAware TimeTree Root", rootNode.getProperty(VALUE_PROPERTY));
            assertNull(rootNode.getSingleRelationship(NEXT, INCOMING));
            assertNull(rootNode.getSingleRelationship(NEXT, OUTGOING));
            assertNull(rootNode.getSingleRelationship(FIRST, INCOMING));
            assertNull(rootNode.getSingleRelationship(LAST, INCOMING));
        }
    }

    @Test
    public void graphShouldNotBeMutatedWhenExistingDayIsRequested() {
        //Given
        DateTime today = DateTime.now(UTC).withTimeAtStartOfDay();

        //When
        try (Transaction tx = database.beginTx()) {
            timeTree.getInstant(today.getMillis(), Resolution.DAY);
            tx.success();
        }

        Node dayNode;
        try (Transaction tx = database.beginTx()) {
            dayNode = timeTree.getInstant(today.getMillis(), Resolution.DAY);
            tx.success();
        }

        //Then
        try (Transaction tx = database.beginTx()) {
            assertEquals(4, count(GlobalGraphOperations.at(database).getAllNodes()));

            assertTrue(dayNode.hasLabel(TimeTreeLabels.DAY));
            assertEquals(today.getDayOfMonth(), dayNode.getProperty(VALUE_PROPERTY));

            Node monthNode = dayNode.getSingleRelationship(CHILD, INCOMING).getStartNode();
            assertTrue(monthNode.hasLabel(TimeTreeLabels.MONTH));
            assertEquals(today.getMonthOfYear(), monthNode.getProperty(VALUE_PROPERTY));

            Node yearNode = monthNode.getSingleRelationship(CHILD, INCOMING).getStartNode();
            assertTrue(yearNode.hasLabel(TimeTreeLabels.YEAR));
            assertEquals(today.getYear(), yearNode.getProperty(VALUE_PROPERTY));

            Node rootNode = yearNode.getSingleRelationship(CHILD, INCOMING).getStartNode();
            assertTrue(rootNode.hasLabel(TimeTreeLabels.ROOT));
            assertEquals("GraphAware TimeTree Root", rootNode.getProperty(VALUE_PROPERTY));
        }
    }

    @Test
    public void fullTreeShouldBeCreatedWhenAFewDaysAreRequested() {
        //Given
        try (Transaction tx = database.beginTx()) {
            timeTree.getInstant(dateToMillis(2012, 11, 1));
            tx.success();
        }
        try (Transaction tx = database.beginTx()) {
            timeTree.getInstant(dateToMillis(2012, 11, 10));
            tx.success();
        }
        try (Transaction tx = database.beginTx()) {
            timeTree.getInstant(dateToMillis(2013, 1, 2));
            timeTree.getInstant(dateToMillis(2013, 1, 1));
            timeTree.getInstant(dateToMillis(2013, 1, 4));
            tx.success();
        }
        try (Transaction tx = database.beginTx()) {
            timeTree.getInstant(dateToMillis(2013, 3, 10));
            timeTree.getInstant(dateToMillis(2013, 2, 1));
            tx.success();
        }

        //Then
        verifyFullTree();
    }

    @Test
    public void fullTreeShouldBeCreatedWhenAFewDaysAreRequested2() {
        //Given
        try (Transaction tx = database.beginTx()) {
            timeTree.getInstant(dateToMillis(2013, 1, 2));
            timeTree.getInstant(dateToMillis(2013, 1, 4));
            timeTree.getInstant(dateToMillis(2013, 1, 1));
            tx.success();
        }
        try (Transaction tx = database.beginTx()) {
            timeTree.getInstant(dateToMillis(2013, 2, 1));
            tx.success();
        }
        try (Transaction tx = database.beginTx()) {
            timeTree.getInstant(dateToMillis(2012, 11, 1));
            tx.success();
        }
        try (Transaction tx = database.beginTx()) {
            timeTree.getInstant(dateToMillis(2013, 3, 10));
            timeTree.getInstant(dateToMillis(2012, 11, 10));
            tx.success();
        }

        //Then
        verifyFullTree();
    }

    private void verifyFullTree() {
        try (Transaction tx = database.beginTx()) {
            assertEquals(14, count(database.getAllNodes()));

            Node root = database.findNodesByLabelAndProperty(TimeTreeLabels.ROOT, VALUE_PROPERTY, "GraphAware TimeTree Root").iterator().next();

            //year level
            Node year2012 = root.getSingleRelationship(FIRST, OUTGOING).getEndNode();
            Node year2013 = root.getSingleRelationship(LAST, OUTGOING).getEndNode();
            Iterator<Relationship> childRels = root.getRelationships(CHILD, OUTGOING).iterator();
            assertTrue(asList(year2012, year2013).contains(childRels.next().getEndNode()));
            assertTrue(asList(year2012, year2013).contains(childRels.next().getEndNode()));
            assertFalse(childRels.hasNext());
            assertEquals(year2013, year2012.getSingleRelationship(NEXT, OUTGOING).getEndNode());
            assertNull(year2013.getSingleRelationship(NEXT, OUTGOING));
            assertNull(year2012.getSingleRelationship(NEXT, INCOMING));

            //month level
            Node month112012 = year2012.getSingleRelationship(FIRST, OUTGOING).getEndNode();
            assertEquals(month112012, year2012.getSingleRelationship(LAST, OUTGOING).getEndNode());
            assertEquals(month112012, year2012.getSingleRelationship(CHILD, OUTGOING).getEndNode());

            Node month012013 = year2013.getSingleRelationship(FIRST, OUTGOING).getEndNode();
            assertEquals(month012013, month112012.getSingleRelationship(NEXT, OUTGOING).getEndNode());

            Node month022013 = month012013.getSingleRelationship(NEXT, OUTGOING).getEndNode();
            Node month032013 = month022013.getSingleRelationship(NEXT, OUTGOING).getEndNode();
            childRels = year2013.getRelationships(CHILD, OUTGOING).iterator();
            assertTrue(asList(month012013, month022013, month032013).contains(childRels.next().getEndNode()));
            assertTrue(asList(month012013, month022013, month032013).contains(childRels.next().getEndNode()));
            assertTrue(asList(month012013, month022013, month032013).contains(childRels.next().getEndNode()));
            assertFalse(childRels.hasNext());
            assertEquals(month032013, year2013.getSingleRelationship(LAST, OUTGOING).getEndNode());
            assertNull(month032013.getSingleRelationship(NEXT, OUTGOING));

            //day level
            Node day01112012 = month112012.getSingleRelationship(FIRST, OUTGOING).getEndNode();
            Node day10112012 = month112012.getSingleRelationship(LAST, OUTGOING).getEndNode();
            childRels = month112012.getRelationships(CHILD, OUTGOING).iterator();
            assertTrue(asList(day01112012, day10112012).contains(childRels.next().getEndNode()));
            assertTrue(asList(day01112012, day10112012).contains(childRels.next().getEndNode()));
            assertFalse(childRels.hasNext());

            Node day01012013 = day10112012.getSingleRelationship(NEXT, OUTGOING).getEndNode();
            Node day02012013 = day01012013.getSingleRelationship(NEXT, OUTGOING).getEndNode();
            Node day04012013 = day02012013.getSingleRelationship(NEXT, OUTGOING).getEndNode();
            Node day01022013 = day04012013.getSingleRelationship(NEXT, OUTGOING).getEndNode();
            Node day10032013 = day01022013.getSingleRelationship(NEXT, OUTGOING).getEndNode();

            assertEquals(day01012013, month012013.getSingleRelationship(FIRST, OUTGOING).getEndNode());
            assertEquals(day04012013, month012013.getSingleRelationship(LAST, OUTGOING).getEndNode());
            childRels = month012013.getRelationships(CHILD, OUTGOING).iterator();
            assertTrue(asList(day01012013, day02012013, day04012013).contains(childRels.next().getEndNode()));
            assertTrue(asList(day01012013, day02012013, day04012013).contains(childRels.next().getEndNode()));
            assertTrue(asList(day01012013, day02012013, day04012013).contains(childRels.next().getEndNode()));
            assertFalse(childRels.hasNext());

            assertEquals(day01022013, month022013.getSingleRelationship(FIRST, OUTGOING).getEndNode());
            assertEquals(day01022013, month022013.getSingleRelationship(LAST, OUTGOING).getEndNode());
            assertEquals(day01022013, month022013.getSingleRelationship(CHILD, OUTGOING).getEndNode());

            assertEquals(day10032013, month032013.getSingleRelationship(FIRST, OUTGOING).getEndNode());
            assertEquals(day10032013, month032013.getSingleRelationship(LAST, OUTGOING).getEndNode());
            assertEquals(day10032013, month032013.getSingleRelationship(CHILD, OUTGOING).getEndNode());

            assertNull(day01112012.getSingleRelationship(CHILD, OUTGOING));
            assertNull(day10112012.getSingleRelationship(CHILD, OUTGOING));
            assertNull(day01012013.getSingleRelationship(CHILD, OUTGOING));
            assertNull(day02012013.getSingleRelationship(CHILD, OUTGOING));
            assertNull(day04012013.getSingleRelationship(CHILD, OUTGOING));
            assertNull(day01022013.getSingleRelationship(CHILD, OUTGOING));
            assertNull(day10032013.getSingleRelationship(CHILD, OUTGOING));
        }
    }

    @Test
    @Ignore
    public void generateDatabaseWithTwoYears() {
        database.shutdown();
        database = new GraphDatabaseFactory().newEmbeddedDatabase("/tmp/rover");
        timeTree = new TimeTreeImpl(database);

        DateTime start = dateToDateTime(2012, 1, 1);
        DateTime end = dateToDateTime(2013, 12, 31);

        try (Transaction tx = database.beginTx()) {
            for (DateTime dateTime = start; dateTime.isBefore(end); dateTime = dateTime.plusDays(1)) {
                timeTree.getInstant(dateTime.getMillis());
            }
                tx.success();
        }
    }

    private long dateToMillis(int year, int month, int day) {
        return dateToDateTime(year, month, day).getMillis();
    }

    private DateTime dateToDateTime(int year, int month, int day) {
        return new DateTime(year, month, day, 0, 0, UTC);
    }
}
