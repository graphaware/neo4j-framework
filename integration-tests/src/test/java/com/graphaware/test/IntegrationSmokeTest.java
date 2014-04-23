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

import com.graphaware.common.test.TestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.After;
import org.junit.Test;
import org.neo4j.server.Bootstrapper;
import org.neo4j.server.configuration.Configurator;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;

import static com.graphaware.common.test.TestUtils.assertJsonEquals;
import static junit.framework.Assert.assertTrue;

/**
 *
 */
//@Ignore
public class IntegrationSmokeTest {

    private Bootstrapper bootstrapper;

    private void setUp(String serverConfig) throws IOException, InterruptedException {
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

    @Test
    public void graphAwareApisAreMountedWhenPresentOnClasspath() throws InterruptedException, IOException {
        setUp("neo4j-server-no-runtime.properties");

        TestUtils.post("http://localhost:7474/db/data/cypher",
                "{\"query\" : \"" +
                        "CREATE (one:L1:L2 { name:\\\"one\\\" }) " +
                        "CREATE (two:L2 { name:\\\"two\\\" }) " +
                        "CREATE (three:L1:L2 { name:\\\"three\\\" }) " +
                        "CREATE (four:L2 { name:\\\"four\\\" }) " +
                        "CREATE (five:L1 { name:\\\"five\\\" }) " +
                        "CREATE (six:L1 { name:\\\"six\\\" }) " +
                        "CREATE (seven:L1 { name:\\\"seven\\\" }) " +
                        "CREATE (one)-[:R1 {cost:5}]->(two)-[:R2 {cost:1}]->(three) " +
                        "CREATE (one)-[:R2 {cost:1}]->(four)-[:R1 {cost:2}]->(five)-[:R1 {cost:1}]->(three) " +
                        "CREATE (two)-[:R2 {cost:1}]->(four) " +
                        "CREATE (one)-[:R1 {cost:1}]->(six)-[:R1]->(seven)<-[:R1 {cost:1}]-(three)" +
                        "\"}",
                HttpStatus.OK_200);

        assertJsonEquals(TestUtils.post("http://localhost:7474/graphaware/api/library/algorithm/path/increasinglyLongerShortestPath",
                jsonAsString("minimalInput"), HttpStatus.OK_200),
                jsonAsString("minimalOutput"));
    }

    @Test
    public void graphAwareRuntimeWithModulesWorkWhenProperlyConfigured() throws IOException, InterruptedException {
        setUp("neo4j-server-runtime-and-relcount.properties");

        //todo test relcount API
    }

    private String jsonAsString(String fileName) {
        try {
            return IOUtils.toString(new ClassPathResource("com/graphaware/test/" + fileName + ".json").getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
