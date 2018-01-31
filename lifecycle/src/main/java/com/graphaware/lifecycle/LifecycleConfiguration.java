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

package com.graphaware.lifecycle;

import java.util.*;
import java.util.stream.Collectors;

import com.graphaware.common.log.LoggerFactory;
import com.graphaware.common.policy.inclusion.InclusionPolicies;
import com.graphaware.common.policy.role.InstanceRolePolicy;
import com.graphaware.common.policy.role.WritableRole;
import com.graphaware.lifecycle.event.CommitEvent;
import com.graphaware.lifecycle.event.LifecycleEvent;
import com.graphaware.lifecycle.event.ScheduledEvent;
import com.graphaware.runtime.config.BaseTxAndTimerDrivenModuleConfiguration;
import com.graphaware.runtime.policy.InclusionPoliciesFactory;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.logging.Log;


/**
 * {@link BaseTxAndTimerDrivenModuleConfiguration} for {@link LifecyleModule}.
 */
public class LifecycleConfiguration extends BaseTxAndTimerDrivenModuleConfiguration<LifecycleConfiguration> {

	private static final Log LOG = LoggerFactory.getLogger(LifecycleConfiguration.class);
	private static final int DEFAULT_BATCH_SIZE = 1000;
	private static final String PACKAGE_LIST_CONFIG_KEY = "packages";

	private int batchSize;
	private HashMap<String, String> config;
	private List<ScheduledEvent<Node>> nodeScheduledEvents;
	private List<ScheduledEvent<Relationship>> relationshipScheduledEvents;
	private List<CommitEvent<Node>> nodeCommitEvents;
	private List<CommitEvent<Relationship>> relationshipCommitEvents;
	private List<String> packages;

	/**
	 * Construct a new configuration.
	 *
	 * @param inclusionPolicies policies for inclusion of nodes, relationships, and properties for processing by the module. Must not be <code>null</code>.
	 * @param initializeUntil until what time in ms since epoch it is ok to re(initialize) the entire module in case the configuration
	 * has changed since the last time the module was started, or if it is the first time the module was registered.
	 * {@link #NEVER} for never, {@link #ALWAYS} for always.
	 * @param instanceRolePolicy specifies which role a machine must have in order to run the module with this configuration. Must not be <code>null</code>.
	 * @param batchSize maximum number of expired nodes or relationships actioned in one go.
	 */
	private LifecycleConfiguration(InclusionPolicies inclusionPolicies, long initializeUntil,
	                               InstanceRolePolicy instanceRolePolicy,
	                               int batchSize) {

		super(inclusionPolicies, initializeUntil, instanceRolePolicy);
		this.batchSize = batchSize;
		this.config = new HashMap<>();
		this.packages = new ArrayList<>();
		this.nodeScheduledEvents = new ArrayList<>();
		this.relationshipScheduledEvents = new ArrayList<>();
		this.nodeCommitEvents = new ArrayList<>();
		this.relationshipCommitEvents = new ArrayList<>();
	}


	/**
	 * Create a default configuration with inclusion policies = {@link InclusionPoliciesFactory#allBusiness()},
	 * initialize until = {@link #ALWAYS}, instance role policy = {@link WritableRole}
	 */
	public static LifecycleConfiguration defaultConfiguration() {
		return new LifecycleConfiguration(InclusionPolicies.all(), ALWAYS, WritableRole.getInstance(), DEFAULT_BATCH_SIZE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected LifecycleConfiguration newInstance(InclusionPolicies inclusionPolicies, long initializeUntil, InstanceRolePolicy instanceRolePolicy) {
		return new LifecycleConfiguration(inclusionPolicies, initializeUntil, instanceRolePolicy, getBatchSize());
	}

	public void putAll(Map<String, String> properties) {
		this.config.putAll(properties);
		this.packages = toList(config.get(PACKAGE_LIST_CONFIG_KEY));
	}

	public LifecycleConfiguration withConfig(String key, String value) {
		this.config.put(key, value);
		return this;
	}

	/**
	 * Add a package to scan the classpath for lifecycle events. If not packages are configured, then the entire
	 * classpath is scanned.
	 * @param packageName
	 * @return
	 */
	public LifecycleConfiguration withPackage(String packageName) {
		this.packages.add(packageName);
		return this;
	}

	/**
	 * Configures how many nodes or relationship to process in one run-loop. The default is 1000.
	 * @param batchSize
	 * @return
	 */
	public LifecycleConfiguration withBatchSize(int batchSize) {
		this.batchSize = batchSize;
		return this;
	}

	public int getBatchSize() {
		return batchSize;
	}

	public List<String> getPackages() {
		return packages;
	}

	public List<ScheduledEvent<Node>> getNodeScheduledEvents() {
		return nodeScheduledEvents;
	}

	public List<ScheduledEvent<Relationship>> getRelationshipScheduledEvents() {
		return relationshipScheduledEvents;
	}

	public List<CommitEvent<Node>> getNodeCommitEvents() {
		return nodeCommitEvents;
	}

	public List<CommitEvent<Relationship>> getRelationshipCommitEvents() {
		return relationshipCommitEvents;
	}

	public HashMap<String, String> getConfig() {
		return new HashMap<>(config);
	}

	/**
	 * Scans for events in specified packages. If no packages are specified then the entire classpath is scanned.
	 */
	public LifecycleConfiguration scanForEvents() {
		if (packages.size() == 0) {
			LOG.info("Scanning for lifecycle events in all packages");
		} else {
			LOG.info("Scanning for lifecycle events in packages: " + packages);
		}
		ArrayList<Class<? extends LifecycleEvent>> matches = new ArrayList<>();
		new FastClasspathScanner(packages.toArray(new String[0]))
				.matchClassesImplementing(LifecycleEvent.class, matches::add)
				.scan();
		for (Class<? extends LifecycleEvent> c : matches) {
			register(c);
		}
		return this;
	}

	/**
	 * Manually registers an event class with the module.
	 *
	 * @param c class of the event to register.
	 */
	public LifecycleConfiguration register(Class<? extends LifecycleEvent> c) {
		LOG.info("Registering event: " + c.getName());
		try {
			LifecycleEvent instance = c.newInstance();
			instance.configure(getConfig());
			if (instance instanceof ScheduledEvent) {
				registerScheduledEvent((ScheduledEvent) instance);
			} else if (instance instanceof CommitEvent) {
				registerCommitEvent((CommitEvent) instance);
			}
		} catch (InstantiationException | IllegalAccessException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
		return this;
	}

	private void registerScheduledEvent(ScheduledEvent event) {
		if (event.appliesTo().equals(Node.class)) {
			nodeScheduledEvents.add(event);
		} else if (event.appliesTo().equals(Relationship.class)) {
			relationshipScheduledEvents.add(event);
		}
	}

	private void registerCommitEvent(CommitEvent event) {
		if (event.appliesTo().equals(Node.class)) {
			nodeCommitEvents.add(event);
		} else if (event.appliesTo().equals(Relationship.class)) {
			relationshipCommitEvents.add(event);
		}
	}


	private List<String> toList(String commaSeparated) {
		if (commaSeparated != null) {
			commaSeparated = commaSeparated.replaceAll("^\\[|]$", "");
			return Arrays.stream(commaSeparated.split(",")).map(String::trim).collect(Collectors.toList());
		}
		return Collections.emptyList();
	}
}
