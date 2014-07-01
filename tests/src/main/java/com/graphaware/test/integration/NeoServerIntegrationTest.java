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

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.neo4j.server.Bootstrapper;
import org.neo4j.server.configuration.Configurator;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Base class for server mode integration tests that are as close to real Neo4j server deployment as possible. As a consequence,
 * low-level access to {@link org.neo4j.graphdb.GraphDatabaseService} is not provided. Instead, use {@link com.graphaware.test.util.TestUtils#executeCypher(String, String...)}
 * to populate/query the database.
 * <p/>
 * The target audience of this class are developers of GraphAware Framework (Runtime) Modules.
 * The primary purpose of tests that extend this class should be to verify that given a certain Neo4j configuration,
 * a (possibly runtime) module is bootstrapped and started correctly when the Neo4j server starts.
 * <p/>
 * The configuration file name is provided using a constructor. It defaults to "neo4j.properties" and if no such file is present
 * on the classpath of the implementing class, the one that ships with Neo4j is used.
 */
public abstract class NeoServerIntegrationTest {

    private Bootstrapper bootstrapper;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    public void setUp() throws IOException, InterruptedException {
        String serverConfigContents = IOUtils.toString(new ClassPathResource(neo4jServerConfigFile()).getInputStream());
        serverConfigContents = serverConfigContents.replaceAll("=conf/", "=" + temporaryFolder.getRoot().getAbsolutePath() + "/conf/");
        serverConfigContents = serverConfigContents.replaceAll("=data/", "=" + temporaryFolder.getRoot().getAbsolutePath() + "/data/");

        temporaryFolder.newFolder("conf");
        File serverConfig = temporaryFolder.newFile("conf/neo4j-server.properties");
        IOUtils.copy(IOUtils.toInputStream(serverConfigContents), new FileOutputStream(serverConfig));
        IOUtils.copy(new ClassPathResource(neo4jConfigFile()).getInputStream(), new FileOutputStream(temporaryFolder.newFile("conf/neo4j.properties")));

        System.setProperty(Configurator.NEO_SERVER_CONFIG_FILE_KEY, serverConfig.getAbsolutePath());

        bootstrapper = Bootstrapper.loadMostDerivedBootstrapper();
        bootstrapper.start(new String[0]);
    }

    @After
    public void tearDown() throws IOException, InterruptedException {
        bootstrapper.stop();
    }

    protected String baseUrl() {
        return "http://localhost:7575";
    }

    /**
     * Get the name of the neo4j config file on the classpath.
     *
     * @return config file name.
     */
    protected String neo4jConfigFile() {
        return "neo4j.properties";
    }

    /**
     * Get the name of the neo4j server config file on the classpath.
     *
     * @return config file name.
     */
    protected String neo4jServerConfigFile() {
        return "neo4j-server.properties";
    }
}
