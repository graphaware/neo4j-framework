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

package com.graphaware.test.integration;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.neo4j.server.Bootstrapper;
import org.neo4j.server.configuration.Configurator;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;

import static junit.framework.Assert.assertTrue;

/**
 *
 */
public abstract class IntegrationTest {

    private Bootstrapper bootstrapper;

    protected void setUp(String serverConfig) throws IOException, InterruptedException {
        deleteTempDir();

        ClassPathResource classPathResource = new ClassPathResource(serverConfig);

        assertTrue(classPathResource.exists());

        String path = classPathResource.getFile().getCanonicalPath();

        System.setProperty(Configurator.NEO_SERVER_CONFIG_FILE_KEY, path);

        bootstrapper = Bootstrapper.loadMostDerivedBootstrapper();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                bootstrapper.start(new String[0]);
            }
        });

        thread.run();

        Thread.sleep(1000);
    }

    @After
    public void tearDown() throws IOException, InterruptedException {
        bootstrapper.stop();
    }

    private void deleteTempDir() throws IOException {
        FileUtils.deleteDirectory(new File("/tmp/ga-int-test/"));
    }

    protected String jsonAsString(String fileName) {
        try {
            return IOUtils.toString(new ClassPathResource("com/graphaware/test/" + fileName + ".json").getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
