/*
 * Copyright (c) 2014 GraphAware
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
package ${package};

import com.graphaware.test.api.ApiTest;
import org.apache.http.HttpStatus;
import org.junit.Test;
import org.neo4j.graphdb.Transaction;

import static com.graphaware.test.util.TestUtils.get;
import static org.junit.Assert.assertEquals;

/**
 * Integration test for {@link NodeCountApi}.
 */
public class NodeCountApiTest extends ApiTest {

    @Test
    public void nodesShouldBeCorrectlyCounted() {
        try (Transaction tx = getDatabase().beginTx()) {
            getDatabase().createNode();
            tx.success();
        }

        String result = get("http://localhost:" + getPort() + "/graphaware/count/", HttpStatus.SC_OK);

        assertEquals("1", result);
    }
}