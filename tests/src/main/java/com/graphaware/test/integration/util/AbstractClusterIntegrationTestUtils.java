/*
 * Copyright (c) 2013-2016 GraphAware
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
package com.graphaware.test.integration.util;

import java.io.File;
import java.io.IOException;
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

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactoryState;
import org.neo4j.kernel.GraphDatabaseDependencies;
import org.neo4j.kernel.impl.factory.GraphDatabaseFacadeFactory;
import org.springframework.core.io.ClassPathResource;

/**
 * Utility for testing in cluster mode
 */
public abstract class AbstractClusterIntegrationTestUtils {
	/**
	 * Default enterprise configuration file
	 */
	private static final String CONF_PATH = "/com/graphaware/runtime/config/neo4j.conf";

	/**
	 * The database instances of the cluster
	 */
	private List<GraphDatabaseService> databases;

	/**
	 * Create the right type of GraphDatabaseService
	 * @param storeDir
	 * @param params
	 * @param dependencies
	 * @return
	 */
	protected abstract GraphDatabaseService newDatabaseInstance(File storeDir, Map<String, String> params,
			GraphDatabaseFacadeFactory.Dependencies dependencies);

	/**
	 * Add the specific configuration for the cluster type
	 * @param i index of instance in the cluster
	 * @param clusterSize the number of instances in the cluster
	 * @return
	 */
	protected abstract Map<String, String> addictionalParams(int i, int clusterSize);

	/**
	 * Start the "i" instance of database
	 * @param i
	 * @param clusterSize
	 * @return
	 * @throws IOException
	 */
	protected GraphDatabaseService startDatabase(int i, int clusterSize) throws IOException {

		GraphDatabaseFacadeFactory.Dependencies dependencies = new GraphDatabaseFactoryState().databaseDependencies();

		dependencies = GraphDatabaseDependencies.newDependencies(dependencies);

		Properties properties = new Properties();
		properties.load(new ClassPathResource(CONF_PATH).getInputStream());

		Map<String, String> params = new HashMap<>();
		for (final String propertyName : properties.stringPropertyNames()) {
			params.put(propertyName, properties.getProperty(propertyName));
		}

		params.putAll(addictionalParams(i, clusterSize));

		File storeDir = Files.createTempDirectory("neo4j-test-cluster-").toFile();
		storeDir.deleteOnExit(); // it doesn't work very well
		GraphDatabaseService graphDb = newDatabaseInstance(storeDir, params, dependencies);

		return graphDb;
	}

	/**
	 * Start all the instances of the cluster
	 * @param clusterSize number of instances in the cluster
	 * @return
	 * @throws Exception
	 */
	public List<GraphDatabaseService> setUpDatabases(int clusterSize) throws Exception {

		ExecutorService exec = Executors.newFixedThreadPool(clusterSize);
		List<Future<GraphDatabaseService>> futures = new ArrayList<>(clusterSize);

		// load server instances
		IntStream.range(0, clusterSize).forEach(i -> {

			Future<GraphDatabaseService> f = exec.submit(() -> {
				return startDatabase(i, clusterSize);
			});

			futures.add(f);
		});

		databases = new ArrayList<>();

		for (Future<GraphDatabaseService> f : futures) {
			databases.add(f.get());
		}

		return databases;
	}

	/**
	 * ES: localhost:5000,localhost:5001
	 * @param startPort 5000
	 * @param clusterSize 
	 * @return
	 */
	protected String buildDiscoveryAddresses(int startPort, int clusterSize){
		return IntStream.range(startPort, startPort+clusterSize).mapToObj(p -> "localhost:"+p).collect(Collectors.joining(","));
	}
	
	/**
	 * shutdown all the instances in the cluster
	 */
	public void shutdownDatabases() {
		shutdownDatabases(databases);
	}

	/**
	 * Shutdown the input instances 
	 * @param dbs
	 */
	public void shutdownDatabases(List<GraphDatabaseService> dbs) {
		dbs.forEach(GraphDatabaseService::shutdown);
	}

	/**
	 * Get the first instance started in the cluster
	 * @return
	 */
	public GraphDatabaseService getMainDatabase() {
		return databases.get(0);
	}

	/**
	 * Get the other (not main) instances of database in the cluster
	 * @return
	 */
	public List<GraphDatabaseService> getSecondaryDatabases() {
		return databases.subList(1, databases.size());
	}

	/**
	 * All the instances of the cluster
	 * @return
	 */
	public List<GraphDatabaseService> getAllDatabases() {
		return databases;
	}
}
