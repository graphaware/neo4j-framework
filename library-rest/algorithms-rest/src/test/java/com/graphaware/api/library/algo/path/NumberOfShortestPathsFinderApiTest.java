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

package com.graphaware.api.library.algo.path;

import com.graphaware.api.common.GraphAwareApiTest;
import com.graphaware.common.test.TestUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Test;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;

import static com.graphaware.common.test.TestUtils.assertJsonEquals;

/**
 * Integration test for {@link com.graphaware.api.library.algo.path.NumberOfShortestPathsFinderApi}.
 */
public class NumberOfShortestPathsFinderApiTest extends GraphAwareApiTest {

    public static final String POST_URL = "http://localhost:" + PORT + "/graphaware/api/library/algorithm/path/increasinglyLongerShortestPath";

    private static final String NAME = "name";
    private static final String COST = "cost";

    private enum RelTypes implements RelationshipType {
        R1, R2
    }

    private enum Labels implements Label {
        L1, L2
    }

    @Test
    public void minimalOutputShouldBeProducedWithLegalMinimalInput() {
        assertJsonEquals(post(jsonAsString("minimalInput")), jsonAsString("minimalOutput"));
    }

    @Test
    public void nodePropertiesShouldBeIncludedWhenRequested() {
        assertJsonEquals(post(jsonAsString("requestNodePropsInput")), jsonAsString("requestNodePropsOutput"));
    }

    @Test
    public void nonExistingNodePropertiesShouldNotBeIncluded() {
        assertJsonEquals(post(jsonAsString("requestNonExistingNodePropsInput")), jsonAsString("minimalOutput"));
    }

    @Test
    public void relationshipPropertiesShouldBeIncludedWhenRequested() {
        assertJsonEquals(post(jsonAsString("requestRelationshipPropsInput")), jsonAsString("requestRelationshipPropsOutput"));
    }

    @Test
    public void nonExistingRelationshipPropertiesShouldNotBeIncluded() {
        assertJsonEquals(post(jsonAsString("requestNonExistingRelationshipPropsInput")), jsonAsString("minimalOutput"));
    }

    @Test
    public void nodeLabelsShouldOnlyBeIncludedWhenRequested() {
        assertJsonEquals(post(jsonAsString("requestNoNodeLabelsInput")), jsonAsString("minimalOutput"));
        assertJsonEquals(post(jsonAsString("requestNodeLabelsInput")), jsonAsString("requestNodeLabelsOutput"));
    }

    @Test
    public void maxDepthAndMaxResultsShouldBeRespected() {
        assertJsonEquals(post(jsonAsString("maxDepthInput")), jsonAsString("maxDepthOutput"));
        assertJsonEquals(post(jsonAsString("maxResultsInput")), jsonAsString("maxResultsOutput"));
    }

    @Test
    public void nonExistingCostPropertyShouldNotChangeOrder() {
        assertJsonEquals(post(jsonAsString("nonExistingCostPropertyInput")), jsonAsString("nonExistingCostPropertyOutput"));
    }

    @Test
    public void costPropertyShouldBeTakenIntoAccount() {
        assertJsonEquals(post(jsonAsString("costPropertyInput")), jsonAsString("costPropertyOutput"));
    }

    @Test
    public void directionShouldBeTakenIntoAccount() {
        assertJsonEquals(post(jsonAsString("singleDirectionInput")), jsonAsString("singleDirectionOutput"));
    }

    @Test
    public void relationshipTypesAndDirectionsShouldBeTakenIntoAccount() {
        assertJsonEquals(post(jsonAsString("typesAndDirectionsInput")), jsonAsString("typesAndDirectionsOutput"));
    }

    @Test
    public void inputWithTypesAndGlobalDirectionShouldBeInvalid() {
        post(jsonAsString("invalidInput1"), HttpStatus.BAD_REQUEST_400);
    }

    @Test
    public void invalidInputShouldCause400Error() {
        post(jsonAsString("invalidInput2"), HttpStatus.BAD_REQUEST_400);
    }

    @Test
    public void invalidInputShouldCause400Error2() {
        post(jsonAsString("invalidInput3"), HttpStatus.BAD_REQUEST_400);
    }

    @Test
    public void nonExistingNodeShouldResultIn404() {
        post(jsonAsString("invalidInput4"), HttpStatus.NOT_FOUND_404);
    }

    protected void populateDatabase() {
        try (Transaction tx = database.beginTx()) {
            Node one = database.createNode();
            Node two = database.createNode();
            Node three = database.createNode();
            Node four = database.createNode();
            Node five = database.createNode();
            Node six = database.createNode();
            Node seven = database.createNode();

            one.setProperty(NAME, "one");
            one.addLabel(Labels.L1);
            one.addLabel(Labels.L2);

            two.setProperty(NAME, "two");
            two.addLabel(Labels.L2);

            three.setProperty(NAME, "three");
            three.addLabel(Labels.L1);
            three.addLabel(Labels.L2);

            four.setProperty(NAME, "four");
            four.addLabel(Labels.L2);

            five.setProperty(NAME, "five");
            five.addLabel(Labels.L1);

            six.setProperty(NAME, "six");
            six.addLabel(Labels.L1);

            seven.setProperty(NAME, "seven");
            seven.addLabel(Labels.L1);

            one.createRelationshipTo(two, RelTypes.R1).setProperty(COST, 5);
            two.createRelationshipTo(three, RelTypes.R2).setProperty(COST, 1);
            one.createRelationshipTo(four, RelTypes.R2).setProperty(COST, 1);
            four.createRelationshipTo(five, RelTypes.R1).setProperty(COST, 2);
            five.createRelationshipTo(three, RelTypes.R1).setProperty(COST, 1);
            two.createRelationshipTo(four, RelTypes.R2).setProperty(COST, 1);
            one.createRelationshipTo(six, RelTypes.R1).setProperty(COST, 1);
            six.createRelationshipTo(seven, RelTypes.R1);
            three.createRelationshipTo(seven, RelTypes.R1).setProperty(COST, 1);

            tx.success();
        }
    }

    protected final String post(String json) {
        return post(json, HttpStatus.OK_200);
    }

    protected final String post(String json, int expectedStatus) {
        return TestUtils.post(POST_URL, json, expectedStatus);
    }

}
