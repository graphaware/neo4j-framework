/*
 * Copyright (c) 2013-2015 GraphAware
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

package com.graphaware.test.integration;

import org.apache.commons.io.IOUtils;
import org.junit.rules.TemporaryFolder;
import org.neo4j.server.Bootstrapper;
import org.neo4j.server.configuration.ServerSettings;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Real Neo4j server, started from an instance of this class using the {@link #start()} method and stopped using the
 * {@link #stop()} method.
 * <p/>
 * The Neo4j and server configuration file names are provided using a constructor. They defaults to "neo4j.properties"
 * and "neo4j-server.properties" and if no such files are present on the classpath, the ones that ships with Neo4j are used.
 */
public abstract class NeoTestServer {

    private Bootstrapper bootstrapper;
    private TemporaryFolder temporaryFolder = new TemporaryFolder();
    private final String neo4jConfigFile;
    private final String neo4jServerConfigFile;

    public NeoTestServer() {
        this("neo4j.properties", "neo4j-server.properties");
    }

    public NeoTestServer(String neo4jConfigFile, String neo4jServerConfigFile) {
        this.neo4jConfigFile = neo4jConfigFile;
        this.neo4jServerConfigFile = neo4jServerConfigFile;
    }

    public final void start() throws IOException, InterruptedException {
        temporaryFolder.create();
        temporaryFolder.getRoot().deleteOnExit();

        temporaryFolder.newFolder("config");

        File serverConfig = serverConfigToConfDir();

        copyToConfDir(neo4jConfigFile, "neo4j.properties");

        for (String otherConfig : otherConfResources()) {
            copyToConfDir(otherConfig, otherConfig);
        }

        bootstrapper = createBootstrapper();
        bootstrapper.start(serverConfig.getAbsoluteFile());
    }

    private File serverConfigToConfDir() throws IOException {
        String serverConfigContents = IOUtils.toString(new ClassPathResource(neo4jServerConfigFile).getInputStream());
        serverConfigContents = serverConfigContents.replaceAll("=config/", "=" + temporaryFolder.getRoot().getAbsolutePath() + "/config/");
        serverConfigContents = serverConfigContents.replaceAll("=data/", "=" + temporaryFolder.getRoot().getAbsolutePath() + "/data/");

        File serverConfig = temporaryFolder.newFile("config/neo4j-server.properties");
        IOUtils.copy(IOUtils.toInputStream(serverConfigContents), new FileOutputStream(serverConfig));
        System.setProperty(ServerSettings.SERVER_CONFIG_FILE_KEY, serverConfig.getAbsolutePath());
        return serverConfig;
    }

    protected abstract Bootstrapper createBootstrapper();

    protected String[] otherConfResources() {
        return new String[0];
    }

    protected File copyToConfDir(String classPathResource, String newName) throws IOException {
        File result = temporaryFolder.newFile("config/" + newName);
        IOUtils.copy(new ClassPathResource(classPathResource).getInputStream(), new FileOutputStream(result));
        return result;
    }

    public void stop() throws IOException, InterruptedException {
        bootstrapper.stop();
        temporaryFolder.delete();
    }
}
