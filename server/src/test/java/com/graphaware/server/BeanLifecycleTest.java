/*
 * Copyright (c) 2013-2017 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.server;

import com.graphaware.service.LifecycleTestService;
import com.graphaware.test.integration.GraphAwareIntegrationTest;
import com.graphaware.writer.LifecycleTestWriter;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BeanLifecycleTest extends GraphAwareIntegrationTest {

    @Before
    public void setUp() {
        LifecycleTestService.initCalled = false;
        LifecycleTestWriter.initCalled = false;
        LifecycleTestService.destroyCalled = false;
        LifecycleTestWriter.destroyCalled = false;
    }

    @Override
    protected boolean autoStart() {
        return false;
    }

    @Test
    public void lifecycleAnnotationsShouldBeHonouredInWebContextCommunity() throws IOException, InterruptedException {
        assertFalse(LifecycleTestService.initCalled);
        assertFalse(LifecycleTestService.destroyCalled);

        startServer();

        assertTrue(LifecycleTestService.initCalled);
        assertFalse(LifecycleTestService.destroyCalled);

        stopServer();

        assertTrue(LifecycleTestService.initCalled);
        assertTrue(LifecycleTestService.destroyCalled);
    }

    @Test
    public void lifecycleAnnotationsShouldBeHonouredInWebContextEnterprise() throws IOException, InterruptedException {
        assertFalse(LifecycleTestService.initCalled);
        assertFalse(LifecycleTestService.destroyCalled);

        startServer();

        assertTrue(LifecycleTestService.initCalled);
        assertFalse(LifecycleTestService.destroyCalled);

        stopServer();

        assertTrue(LifecycleTestService.initCalled);
        assertTrue(LifecycleTestService.destroyCalled);
    }
}
