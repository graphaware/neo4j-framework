/*
 * Copyright (c) 2013 GraphAware
 *
 * This file is part of GraphAware.
 *
 * GraphAware is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.api.library.algo.timetree;

import com.graphaware.api.common.GraphAwareApiTest;
import com.graphaware.common.test.TestUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

import static com.graphaware.graphunit.GraphUnit.assertSameGraph;
import static org.junit.Assert.assertEquals;

/**
 * Integration test for {@link TimeTreeApi}.
 */
public class TimeTreeApiTest extends GraphAwareApiTest {

    public static final String URL = "http://localhost:" + PORT + "/graphaware/api/library/algorithm/timetree/";

    @Test
    public void trivialTreeShouldBeCreatedWhenFirstDayIsRequested() {
        //Given
        long dateInMillis = dateToMillis(2013, 5, 4);

        //When
        String result = get(URL + dateInMillis, HttpStatus.OK_200);

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

        assertEquals("3", result);
    }

    @Test
    public void trivialTreeShouldBeCreatedWhenFirstMilliInstantIsRequested() {
        //Given
        long dateInMillis = new DateTime(2014, 4, 5, 13, 56, 22, 123, DateTimeZone.UTC).getMillis();

        //When
        String result = get(URL + dateInMillis + "?resolution=millisecond&timezone=GMT%2B1", HttpStatus.OK_200);

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

        assertEquals("7", result);
    }

    @Test
    public void trivialTreeShouldBeCreatedWhenTodayIsRequested() {
        //Given
        DateTime now = DateTime.now(DateTimeZone.UTC);

        //When
        String result = get(URL + "now", HttpStatus.OK_200);

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

        assertEquals("3", result);
    }

    private String get(String url, int expectedStatus) {
        return TestUtils.get(url, expectedStatus);
    }

    private long dateToMillis(int year, int month, int day) {
        return dateToDateTime(year, month, day).getMillis();
    }

    private DateTime dateToDateTime(int year, int month, int day) {
        return new DateTime(year, month, day, 0, 0, DateTimeZone.UTC);
    }
}
