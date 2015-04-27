/*
 * Copyright (c) 2015 GraphAware
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

package com.graphaware.server;

import com.graphaware.runtime.GraphAwareRuntimeFactory;
import com.graphaware.runtime.config.FluentRuntimeConfiguration;
import com.graphaware.runtime.write.WritingConfig;
import com.graphaware.service.LifecycleTestService;
import com.graphaware.test.integration.NeoTestServer;
import com.graphaware.writer.DatabaseWriter;
import com.graphaware.writer.LifecycleTestWriter;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.GraphDatabaseAPI;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BeanLifecycleTest {

    @Before
    public void setUp() {
        LifecycleTestService.initCalled = false;
        LifecycleTestWriter.initCalled = false;
        LifecycleTestService.destroyCalled = false;
        LifecycleTestWriter.destroyCalled = false;
    }

    @Test
    public void lifecycleAnnotationsShouldBeHonouredInWebContext() throws IOException, InterruptedException {
        assertFalse(LifecycleTestService.initCalled);
        assertFalse(LifecycleTestService.destroyCalled);

        NeoTestServer testServer = new NeoTestServer();
        testServer.start();

        assertTrue(LifecycleTestService.initCalled);
        assertFalse(LifecycleTestService.destroyCalled);

        testServer.stop();

        assertTrue(LifecycleTestService.initCalled);
        assertTrue(LifecycleTestService.destroyCalled);
    }

    @Test
    public void lifecycleAnnotationsShouldBeHonouredInRootContext() throws IOException, InterruptedException {
        assertFalse(LifecycleTestWriter.initCalled);
        assertFalse(LifecycleTestWriter.destroyCalled);

        GraphDatabaseService database = new TestGraphDatabaseFactory().newImpermanentDatabase();
        GraphAwareRuntimeFactory.createRuntime(database, FluentRuntimeConfiguration.defaultConfiguration().withWritingConfig(new WritingConfig() {
            @Override
            public DatabaseWriter produceWriter(GraphDatabaseService database) {
                return new LifecycleTestWriter(database);
            }
        })).start();

        GraphAwareWrappingNeoServer server = new GraphAwareWrappingNeoServer((GraphDatabaseAPI) database);
        server.start();

        assertTrue(LifecycleTestWriter.initCalled);
        assertFalse(LifecycleTestWriter.destroyCalled);

        server.stop();
        database.shutdown();

        assertTrue(LifecycleTestWriter.initCalled);
        assertTrue(LifecycleTestWriter.destroyCalled);
    }
}
