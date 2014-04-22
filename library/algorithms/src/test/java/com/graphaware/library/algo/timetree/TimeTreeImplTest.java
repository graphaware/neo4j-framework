package com.graphaware.library.algo.timetree;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphalgo.impl.path.AllSimplePaths;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.*;
import org.neo4j.kernel.StandardExpander;
import org.neo4j.kernel.Traversal;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.util.TimeZone;

import static com.graphaware.graphunit.GraphUnit.assertSameGraph;
import static com.graphaware.library.algo.timetree.TimeTreeImpl.VALUE_PROPERTY;
import static com.graphaware.library.algo.timetree.TimeTreeRelationshipTypes.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
            dayNode = timeTree.getInstant(dateInMillis);
            tx.success();
        }

        //Then
        assertSameGraph(database, "CREATE" +
                "(root:TimeTreeRoot)," +
                "(root)-[:FIRST]->(year:Year {value:2013})," +
                "(root)-[:CHILD]->(year)," +
                "(root)-[:LAST]->(year)," +
                "(year)-[:FIRST]->(month:Month {value:5})," +
                "(year)-[:CHILD]->(month)," +
                "(year)-[:LAST]->(month)," +
                "(month)-[:FIRST]->(day:Day {value:4})," +
                "(month)-[:CHILD]->(day)," +
                "(month)-[:LAST]->(day)");

        try (Transaction tx = database.beginTx()) {
            assertTrue(dayNode.hasLabel(TimeTreeLabels.Day));
            assertEquals(4, dayNode.getProperty(VALUE_PROPERTY));
        }
    }

    @Test
    public void trivialTreeShouldBeCreatedWhenFirstMilliInstantIsRequested() {
        //Given
        long dateInMillis = new DateTime(2014, 4, 5, 13, 56, 22, 123, UTC).getMillis();
        timeTree = new TimeTreeImpl(database, DateTimeZone.forTimeZone(TimeZone.getTimeZone("GMT+1")), Resolution.MILLISECOND);

        //When
        Node instantNode;
        try (Transaction tx = database.beginTx()) {
            instantNode = timeTree.getInstant(dateInMillis);
            tx.success();
        }

        //Then
        assertSameGraph(database, "CREATE" +
                "(root:TimeTreeRoot)," +
                "(root)-[:FIRST]->(year:Year {value:2014})," +
                "(root)-[:CHILD]->(year)," +
                "(root)-[:LAST]->(year)," +
                "(year)-[:FIRST]->(month:Month {value:4})," +
                "(year)-[:CHILD]->(month)," +
                "(year)-[:LAST]->(month)," +
                "(month)-[:FIRST]->(day:Day {value:5})," +
                "(month)-[:CHILD]->(day)," +
                "(month)-[:LAST]->(day),"+
                "(day)-[:FIRST]->(hour:Hour {value:14})," + //1 hour more!
                "(day)-[:CHILD]->(hour)," +
                "(day)-[:LAST]->(hour),"+
                "(hour)-[:FIRST]->(minute:Minute {value:56})," +
                "(hour)-[:CHILD]->(minute)," +
                "(hour)-[:LAST]->(minute),"+
                "(minute)-[:FIRST]->(second:Second {value:22})," +
                "(minute)-[:CHILD]->(second)," +
                "(minute)-[:LAST]->(second),"+
                "(second)-[:FIRST]->(milli:Millisecond {value:123})," +
                "(second)-[:CHILD]->(milli)," +
                "(second)-[:LAST]->(milli)");

        try (Transaction tx = database.beginTx()) {
            assertTrue(instantNode.hasLabel(TimeTreeLabels.Millisecond));
            assertEquals(123, instantNode.getProperty(VALUE_PROPERTY));
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
        assertSameGraph(database, "CREATE" +
                "(root:TimeTreeRoot)," +
                "(root)-[:FIRST]->(year:Year {value:" + now.getYear() + "})," +
                "(root)-[:CHILD]->(year)," +
                "(root)-[:LAST]->(year)," +
                "(year)-[:FIRST]->(month:Month {value:" + now.getMonthOfYear() + "})," +
                "(year)-[:CHILD]->(month)," +
                "(year)-[:LAST]->(month)," +
                "(month)-[:FIRST]->(day:Day {value:" + now.getDayOfMonth() + "})," +
                "(month)-[:CHILD]->(day)," +
                "(month)-[:LAST]->(day)");

        try (Transaction tx = database.beginTx()) {
            assertTrue(dayNode.hasLabel(TimeTreeLabels.Day));
            assertEquals(now.getDayOfMonth(), dayNode.getProperty(VALUE_PROPERTY));
        }
    }

    @Test
    public void graphShouldNotBeMutatedWhenExistingDayIsRequested() {
        //Given
        DateTime now = DateTime.now(UTC);

        //When
        try (Transaction tx = database.beginTx()) {
            timeTree.getInstant(now.getMillis(), Resolution.DAY);
            tx.success();
        }

        Node dayNode;
        try (Transaction tx = database.beginTx()) {
            dayNode = timeTree.getInstant(now.getMillis(), Resolution.DAY);
            tx.success();
        }

        //Then
        assertSameGraph(database, "CREATE" +
                "(root:TimeTreeRoot)," +
                "(root)-[:FIRST]->(year:Year {value:" + now.getYear() + "})," +
                "(root)-[:CHILD]->(year)," +
                "(root)-[:LAST]->(year)," +
                "(year)-[:FIRST]->(month:Month {value:" + now.getMonthOfYear() + "})," +
                "(year)-[:CHILD]->(month)," +
                "(year)-[:LAST]->(month)," +
                "(month)-[:FIRST]->(day:Day {value:" + now.getDayOfMonth() + "})," +
                "(month)-[:CHILD]->(day)," +
                "(month)-[:LAST]->(day)");

        try (Transaction tx = database.beginTx()) {
            assertTrue(dayNode.hasLabel(TimeTreeLabels.Day));
            assertEquals(now.getDayOfMonth(), dayNode.getProperty(VALUE_PROPERTY));
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
        assertSameGraph(database, "CREATE" +
                "(root:TimeTreeRoot)," +
                "(root)-[:FIRST]->(year2012:Year {value:2012})," +
                "(root)-[:LAST]->(year2013:Year {value:2013})," +
                "(root)-[:CHILD]->(year2012)," +
                "(root)-[:CHILD]->(year2013)," +
                "(year2012)-[:NEXT]->(year2013)," +

                "(year2012)-[:FIRST]->(month112012:Month {value:11})," +
                "(year2012)-[:LAST]->(month112012)," +
                "(year2012)-[:CHILD]->(month112012)," +

                "(year2013)-[:CHILD]->(month012013:Month {value:1})," +
                "(year2013)-[:CHILD]->(month022013:Month {value:2})," +
                "(year2013)-[:CHILD]->(month032013:Month {value:3})," +
                "(year2013)-[:FIRST]->(month012013)," +
                "(year2013)-[:LAST]->(month032013)," +
                "(month112012)-[:NEXT]->(month012013)-[:NEXT]->(month022013)-[:NEXT]->(month032013), " +

                "(month112012)-[:CHILD]->(day01112012:Day {value:1})," +
                "(month112012)-[:CHILD]->(day10112012:Day {value:10})," +
                "(month112012)-[:FIRST]->(day01112012)," +
                "(month112012)-[:LAST]->(day10112012)," +
                "(day01112012)-[:NEXT]->(day10112012)-[:NEXT]->(day01012013:Day {value:1})-[:NEXT]->(day02012013:Day {value:2})-[:NEXT]->(day04012013:Day {value:4})-[:NEXT]->(day01022013:Day {value:1})-[:NEXT]->(day10032013:Day {value:10})," +

                "(month012013)-[:FIRST]->(day01012013)," +
                "(month012013)-[:CHILD]->(day01012013)," +
                "(month012013)-[:LAST]->(day04012013)," +
                "(month012013)-[:CHILD]->(day04012013)," +
                "(month012013)-[:CHILD]->(day02012013)," +

                "(month022013)-[:FIRST]->(day01022013)," +
                "(month022013)-[:LAST]->(day01022013)," +
                "(month022013)-[:CHILD]->(day01022013)," +

                "(month032013)-[:CHILD]->(day10032013)," +
                "(month032013)-[:FIRST]->(day10032013)," +
                "(month032013)-[:LAST]->(day10032013)");
    }

    private long dateToMillis(int year, int month, int day) {
        return dateToDateTime(year, month, day).getMillis();
    }

    private DateTime dateToDateTime(int year, int month, int day) {
        return new DateTime(year, month, day, 0, 0, UTC);
    }
}
