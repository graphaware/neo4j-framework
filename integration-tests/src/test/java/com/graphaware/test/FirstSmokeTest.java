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

package com.graphaware.test;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.server.Bootstrapper;
import org.neo4j.server.configuration.Configurator;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

import static junit.framework.Assert.*;

/**
 *
 */
public class FirstSmokeTest {

    private Thread thread;

    @Before
    public void setUp() throws IOException {
        ClassPathResource classPathResource = new ClassPathResource("neo4j-server.properties");
        assertTrue(classPathResource.exists());

        String path = classPathResource.getFile().getCanonicalPath();

        System.setProperty(Configurator.NEO_SERVER_CONFIG_FILE_KEY, path);

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Bootstrapper.main(new String[0]);
            }
        });

        thread.run();
    }

    @Test
    public void testNothing() throws InterruptedException {
        Thread.sleep(1000000);
        thread.stop();
    }
}
