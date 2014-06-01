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
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.neo4j.server.Bootstrapper;
import org.neo4j.server.configuration.Configurator;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;

import static com.graphaware.test.util.TestUtils.*;

/**
 * Base class for server mode integration tests that are as close to real Neo4j server deployment as possible.
 * <p/>
 * The primary purpose of tests that extend this class should be to verify that given a certain Neo4j configuration,
 * a (possibly runtime) module is bootstrapped and started correctly when the Neo4j server starts.
 * <p/>
 * The configuration is provided using a constructor. It defaults to "neo4j.properties" and if no such file is present
 * on the classpath of the implementing class, one that ships with Neo4j is used.
 */
public abstract class ServerIntegrationTest {

    private final String neo4jProperties;
    private Bootstrapper bootstrapper;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    protected ServerIntegrationTest() {
        this("neo4j.properties");
    }

    protected ServerIntegrationTest(String neo4jProperties) {
        this.neo4jProperties = neo4jProperties;
    }

    @Before
    public void setUp() throws IOException, InterruptedException {
        String serverConfigContents = IOUtils.toString(new ClassPathResource("neo4j-server.properties").getInputStream());
        serverConfigContents = serverConfigContents.replaceAll("=conf/", "=" + temporaryFolder.getRoot().getAbsolutePath() + "/conf/");
        serverConfigContents = serverConfigContents.replaceAll("=data/", "=" + temporaryFolder.getRoot().getAbsolutePath() + "/data/");

        temporaryFolder.newFolder("conf");
        File serverConfig = temporaryFolder.newFile("conf/neo4j-server.properties");
        FileUtils.copyInputStreamToFile(IOUtils.toInputStream(serverConfigContents), serverConfig);
        FileUtils.copyInputStreamToFile(new ClassPathResource(neo4jProperties).getInputStream(), temporaryFolder.newFile("conf/neo4j.properties"));

        System.setProperty(Configurator.NEO_SERVER_CONFIG_FILE_KEY, serverConfig.getAbsolutePath());

        bootstrapper = Bootstrapper.loadMostDerivedBootstrapper();
        bootstrapper.start(new String[0]);
    }

    @After
    public void tearDown() throws IOException, InterruptedException {
        bootstrapper.stop();
    }

    protected String executeCypher(String... cypherStatements) {
        StringBuilder stringBuilder = new StringBuilder("{\"statements\" : [ {");
        for (String statement : cypherStatements) {
            stringBuilder.append("\"statement\" : \"").append(statement).append("\"");
        }
        stringBuilder.append("}]}");

        return post("http://localhost:7474/db/data/transaction/commit", stringBuilder.toString(), HttpStatus.SC_OK);
    }
}
