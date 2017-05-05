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
package com.graphaware.test.integration.cluster;

import static com.graphaware.test.integration.ClassPathProcedureUtils.proceduresAndFunctionsOnClassPath;
import static com.graphaware.test.integration.ClassPathProcedureUtils.registerAllProceduresAndFunctions;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.AfterClass;
import org.junit.Before;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactoryState;
import org.neo4j.kernel.GraphDatabaseDependencies;
import org.neo4j.kernel.impl.factory.GraphDatabaseFacade;
import org.neo4j.kernel.impl.factory.GraphDatabaseFacadeFactory;
import org.neo4j.kernel.impl.proc.Procedures;
import org.neo4j.logging.Log;
import org.springframework.core.io.ClassPathResource;

import com.graphaware.common.log.LoggerFactory;
import com.graphaware.test.data.DatabasePopulator;

/**
 * Superclass for cluster testing. It runs multiple server instances.
 */
public abstract class ClusterDatabasesIntegrationTest {

    private static final String CONF_PATH = "/com/graphaware/test/integration/neo4j.conf";

    private static final String TMP_PATH_PREFIX = "neo4j-test-cluster-";

    protected final Log LOG = LoggerFactory.getLogger(ClusterDatabasesIntegrationTest.class);

    /**
     * All the instances that will be available for tests. We should consider 3
     * main use cases:
     * <ul>
     * <li>single instance</li>
     * <li>high availability topology (at least two instances: master + slave)
     * </li>
     * <li>causal cluster topology (at least three cores 1 leader + 2 followers
     * and one read replica)</li>
     * </ul>
     */
    private static List<GraphDatabaseService> databases;

    /**
     * Load the cluster's instances just one time
     *
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        if (databases == null) {

            databases = createDatabases();

            for (GraphDatabaseService database : databases) {

                if (shouldRegisterProceduresAndFunctions()) {
                    registerAllProceduresAndFunctions(database);
                }

                if (shouldRegisterModules()) {
                    registerModules(database);
                }
            }

            populateDatabases();
        }
    }

    @AfterClass
    public static void tearDown() throws Exception {
        databases.forEach(GraphDatabaseService::shutdown);
        databases = null;
    }


    protected abstract List<Class<?>> getTopology();


    private final GraphDatabaseService createDatabase(int i, Class<?> instanceClass) throws Exception {

        GraphDatabaseFacadeFactory.Dependencies dependencies = new GraphDatabaseFactoryState().databaseDependencies();
        dependencies = GraphDatabaseDependencies.newDependencies(dependencies);

        Properties properties = new Properties();
        properties.load(new ClassPathResource(configFile()).getInputStream());

        Map<String, String> params = new HashMap<>();
        for (final String propertyName : properties.stringPropertyNames()) {
            params.put(propertyName, properties.getProperty(propertyName));
        }

        params.putAll(addictionalParams(i, instanceClass));

        File storeDir = Files.createTempDirectory(TMP_PATH_PREFIX + i).toFile();
        storeDir.deleteOnExit(); // it doesn't work very well

        GraphDatabaseService database = (GraphDatabaseService) instanceClass
                .getConstructor(File.class, Map.class, GraphDatabaseFacadeFactory.Dependencies.class)
                .newInstance(storeDir, params, dependencies);

        LOG.info("An instance of class " + instanceClass.getSimpleName() + " has been created");
        // Too verbose, we leave it at debug level
        LOG.debug("Configuration parameters: " + params);

        return database;
    }

    /**
     * Build the initial/discovery addresses for the instance configuration
     *
     * @param startPort
     * @param clusterSize
     * @return
     */
    protected String buildDiscoveryAddresses(int startPort, int clusterSize) {
        return IntStream.range(startPort, startPort + clusterSize).mapToObj(p -> "localhost:" + p)
                .collect(Collectors.joining(","));
    }

    /**
     * Add the specific configuration for the cluster type
     *
     * @param i             index of instance in the cluster
     * @param instanceClass the type of instance
     * @param clusterSize   the number of instances in the cluster
     * @return
     */
    protected abstract Map<String, String> addictionalParams(int i, Class<?> instanceClass);

    /**
     * Create a database.
     *
     * @return database.
     * @throws Exception
     */
    protected List<GraphDatabaseService> createDatabases() throws Exception {
        List<Class<?>> topology = getTopology();
        assertNotNull(topology);
        assertTrue(topology.size() > 0);

        ExecutorService exec = Executors.newFixedThreadPool(topology.size());
        List<Future<GraphDatabaseService>> futures = new ArrayList<>(topology.size());

        int i = 0;
        for (Class<?> instanceClass : topology) {
            final int j = i;
            Future<GraphDatabaseService> f = exec.submit(() -> {
                return createDatabase(j, instanceClass);
            });
            futures.add(f);
            i++;
        }

        List<GraphDatabaseService> dbs = new ArrayList<>();

        for (Future<GraphDatabaseService> f : futures) {
            dbs.add(f.get());
        }

        return dbs;
    }

    /**
     * Returns the principal instance of cluster (writable)
     *
     * @return
     */
    protected GraphDatabaseService getMainDatabase() {
        return databases.get(0);
    }

    /**
     * Populate all the databases. Can be overridden. By default, it populates
     * all the databases using {@link #databasePopulator()}.
     *
     * @param database to populate.
     */
    protected void populateDatabases() {
        DatabasePopulator populator = databasePopulator();
        if (populator != null) {
            populator.populate(getMainDatabase());
        }
    }

    /**
     * Get the name of config file used to configure the database.
     *
     * @return config file, <code>null</code> for none.
     */
    protected String configFile() {
        return CONF_PATH;
    }

    /**
     * @return <code>true</code> iff all procedures and functions should be registered during {@link #setUp()}.
     */
    protected boolean shouldRegisterProceduresAndFunctions() {
        return false;
    }

    /**
     * @return <code>iff</code> the {@link #registerModules(GraphDatabaseService)}
     * method should be called during {@link #setUp()}.
     */
    protected boolean shouldRegisterModules() {
        return false;
    }

    /**
     * Register modules or what you want into database.
     *
     * @param db to register against.
     */
    protected void registerModules(GraphDatabaseService db) throws Exception {
        // no-op by default
    }

    /**
     * @return {@link com.graphaware.test.data.DatabasePopulator},
     * <code>null</code> (no population) by default.
     */
    protected DatabasePopulator databasePopulator() {
        return null;
    }

    /**
     * Get the database instantiated for this test.
     *
     * @return database.
     */
    protected List<GraphDatabaseService> getDatabases() {
        return databases;
    }

}
