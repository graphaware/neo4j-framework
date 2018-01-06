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

package com.graphaware.lifecycle.config;

import java.util.ArrayList;
import java.util.List;

import com.graphaware.common.policy.inclusion.InclusionPolicies;
import com.graphaware.common.policy.role.InstanceRolePolicy;
import com.graphaware.common.policy.role.WritableRole;
import com.graphaware.lifecycle.LifecyleModule;
import com.graphaware.lifecycle.event.commit.CommitEvent;
import com.graphaware.lifecycle.event.commit.RevivalEvent;
import com.graphaware.lifecycle.strategy.CompositeStrategy;
import com.graphaware.lifecycle.strategy.DeleteOrphanedNodeOnly;
import com.graphaware.lifecycle.strategy.LifecycleEventStrategy;
import com.graphaware.lifecycle.event.scheduled.ExpiryEvent;
import com.graphaware.lifecycle.event.scheduled.ScheduledEvent;
import com.graphaware.lifecycle.strategy.DeleteRelationship;
import com.graphaware.runtime.config.BaseTxAndTimerDrivenModuleConfiguration;
import com.graphaware.runtime.policy.InclusionPoliciesFactory;
import org.apache.commons.lang.StringUtils;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;


/**
 * {@link BaseTxAndTimerDrivenModuleConfiguration} for {@link LifecyleModule}.
 */
public class LifecycleConfiguration extends BaseTxAndTimerDrivenModuleConfiguration<LifecycleConfiguration> {

	private static final String DEFAULT_NODE_EXPIRATION_INDEX = "nodeExpirationIndex";
	private static final String DEFAULT_RELATIONSHIP_EXPIRATION_INDEX = "relationshipExpirationIndex";
	private static final String DEFAULT_NODE_EXPIRATION_PROPERTY = null;
	private static final String DEFAULT_RELATIONSHIP_EXPIRATION_PROPERTY = null;
	private static final String DEFAULT_NODE_TTL_PROPERTY = null;
	private static final String DEFAULT_RELATIONSHIP_TTL_PROPERTY = null;
	private static final String DEFAULT_NODE_REVIVAL_PROPERTY = null;
	private static final String DEFAULT_RELATIONSHIP_REVIVAL_PROPERTY = null;

	private static final LifecycleEventStrategy<Node> DEFAULT_NODE_EXPIRATION_STRATEGY = DeleteOrphanedNodeOnly.getInstance();
	private static final LifecycleEventStrategy<Relationship> DEFAULT_RELATIONSHIP_EXPIRATION_STRATEGY = DeleteRelationship.getInstance();
	private static final LifecycleEventStrategy<Node> DEFAULT_NODE_REVIVAL_STRATEGY = null;
	private static final LifecycleEventStrategy<Relationship> DEFAULT_RELATIONSHIP_REVIVAL_STRATEGY = null;
	private static final int DEFAULT_MAX_NO_EXPIRATIONS = 1000;
	private static final long DEFAULT_EXPIRY_OFFSET = 0;
	private static final long DEFAULT_REVIVAL_OFFSET = 0;

	private String nodeExpirationIndex;
	private String relationshipExpirationIndex;
	//TODO: Make configurable
	private String nodeRevivalIndex = "nodeRevivalIndex";
	private String relationshipRevivalIndex = "relationshipRevivalIndex";
	private String nodeExpirationProperty;
	private String relationshipExpirationProperty;
	private String nodeTtlProperty;
	private String relationshipTtlProperty;
	private String nodeRevivalProperty;
	private String relationshipRevivalProperty;
	private LifecycleEventStrategy<Node> nodeExpirationStrategy;
	private LifecycleEventStrategy<Relationship> relationshipExpirationStrategy;
	private LifecycleEventStrategy<Node> nodeRevivalStrategy;
	private LifecycleEventStrategy<Relationship> relationshipRevivalStrategy;
	private int maxNoExpirations;
	private long expiryOffset;
	private long revivalOffset;

	/**
	 * Construct a new configuration.
	 *
	 * @param inclusionPolicies policies for inclusion of nodes, relationships, and properties for processing by the module. Must not be <code>null</code>.
	 * @param initializeUntil until what time in ms since epoch it is ok to re(initialize) the entire module in case the configuration
	 * has changed since the last time the module was started, or if it is the first time the module was registered.
	 * {@link #NEVER} for never, {@link #ALWAYS} for always.
	 * @param instanceRolePolicy specifies which role a machine must have in order to run the module with this configuration. Must not be <code>null</code>.
	 * @param nodeExpirationIndex name of the legacy index where node expiry dates are stored. Can be <code>null</code>.
	 * @param relationshipExpirationIndex name of the legacy index where relationship expiry dates are stored. Can be <code>null</code>.
	 * @param nodeExpirationProperty name of the node property that specifies the expiration date in ms since epoch. Can be <code>null</code>.
	 * @param relationshipExpirationProperty name of the relationship property that specifies the expiration date in ms since epoch. Can be <code>null</code>.
	 * @param nodeTtlProperty name of the node property that specifies the TTL in ms. Can be <code>null</code>.
	 * @param relationshipTtlProperty name of the relationship property that specifies the TTL in ms. Can be <code>null</code>.
	 * @param nodeRevivalProperty name of the node property that specifies when revival occurs.
	 * @param relationshipRevivalProperty name of the relationship property that specifies when revival occurs.
	 * @param maxNoExpirations maximum number of expired nodes or relationships in one go.
	 * @param nodeExpirationStrategy expiration strategy for nodes. Must not be <code>null</code>.
	 * @param relationshipExpirationStrategy expiration strategy for relationships. Must not be <code>null</code>.
	 * @param nodeExpirationStrategy revival strategy for nodes.
	 * @param relationshipExpirationStrategy revival strategy for relationships.
	 * @param expiryOffset expiration is calculated as expiry date plus offset in ms.
	 * @param revivalOffset revival is calculated as revival date plus offset in ms.
	 */
	private LifecycleConfiguration(InclusionPolicies inclusionPolicies, long initializeUntil,
	                               InstanceRolePolicy instanceRolePolicy,
	                               String nodeExpirationIndex,
	                               String relationshipExpirationIndex,
	                               String nodeExpirationProperty,
	                               String relationshipExpirationProperty,
	                               String nodeTtlProperty,
	                               String relationshipTtlProperty,
	                               String nodeRevivalProperty,
	                               String relationshipRevivalProperty,
	                               int maxNoExpirations,
	                               LifecycleEventStrategy<Node> nodeExpirationStrategy,
	                               LifecycleEventStrategy<Relationship> relationshipExpirationStrategy,
	                               LifecycleEventStrategy<Node> nodeRevivalStrategy,
	                               LifecycleEventStrategy<Relationship> relationshipRevivalStrategy,
	                               long expiryOffset,
	                               long revivalOffset) {

		super(inclusionPolicies, initializeUntil, instanceRolePolicy);
		this.nodeExpirationIndex = nodeExpirationIndex;
		this.relationshipExpirationIndex = relationshipExpirationIndex;
		this.nodeExpirationProperty = nodeExpirationProperty;
		this.relationshipExpirationProperty = relationshipExpirationProperty;
		this.nodeTtlProperty = nodeTtlProperty;
		this.relationshipTtlProperty = relationshipTtlProperty;
		this.nodeRevivalProperty = nodeRevivalProperty;
		this.relationshipRevivalProperty = relationshipRevivalProperty;
		this.maxNoExpirations = maxNoExpirations;
		this.nodeExpirationStrategy = nodeExpirationStrategy;
		this.relationshipExpirationStrategy = relationshipExpirationStrategy;
		this.nodeRevivalStrategy = nodeRevivalStrategy;
		this.relationshipRevivalStrategy = relationshipRevivalStrategy;
		this.expiryOffset = expiryOffset;
		this.revivalOffset = revivalOffset;
	}


	/**
	 * Create a default configuration with inclusion policies = {@link InclusionPoliciesFactory#allBusiness()},
	 * initialize until = {@link #ALWAYS}, instance role policy = {@link WritableRole},
	 * and {@link #DEFAULT_NODE_EXPIRATION_INDEX}, {@link #DEFAULT_RELATIONSHIP_EXPIRATION_INDEX},{@link #DEFAULT_NODE_EXPIRATION_PROPERTY},
	 * {@link #DEFAULT_RELATIONSHIP_EXPIRATION_PROPERTY},{@link #DEFAULT_NODE_TTL_PROPERTY}, {@link #DEFAULT_RELATIONSHIP_TTL_PROPERTY},
	 * {@link #DEFAULT_NODE_EXPIRATION_STRATEGY}, and {@link #DEFAULT_RELATIONSHIP_EXPIRATION_STRATEGY}.
	 */
	public static LifecycleConfiguration defaultConfiguration() {
		return new LifecycleConfiguration(InclusionPolicies.all(), ALWAYS, WritableRole.getInstance(),
				DEFAULT_NODE_EXPIRATION_INDEX, DEFAULT_RELATIONSHIP_EXPIRATION_INDEX, DEFAULT_NODE_EXPIRATION_PROPERTY,
				DEFAULT_RELATIONSHIP_EXPIRATION_PROPERTY, DEFAULT_NODE_TTL_PROPERTY, DEFAULT_RELATIONSHIP_TTL_PROPERTY,
				DEFAULT_NODE_REVIVAL_PROPERTY, DEFAULT_RELATIONSHIP_REVIVAL_PROPERTY, DEFAULT_MAX_NO_EXPIRATIONS,
				DEFAULT_NODE_EXPIRATION_STRATEGY, DEFAULT_RELATIONSHIP_EXPIRATION_STRATEGY,
				DEFAULT_NODE_REVIVAL_STRATEGY, DEFAULT_RELATIONSHIP_REVIVAL_STRATEGY, DEFAULT_EXPIRY_OFFSET,
				DEFAULT_REVIVAL_OFFSET);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected LifecycleConfiguration newInstance(InclusionPolicies inclusionPolicies, long initializeUntil, InstanceRolePolicy instanceRolePolicy) {
		return new LifecycleConfiguration(inclusionPolicies, initializeUntil, instanceRolePolicy,
				getNodeExpirationIndex(), getRelationshipExpirationIndex(), getNodeExpirationProperty(),
				getRelationshipExpirationProperty(), getNodeTtlProperty(), getRelationshipTtlProperty(),
				getNodeRevivalProperty(), getRelationshipRevivalProperty(), getMaxNoExpirations(),
				getNodeExpirationStrategy(), getRelationshipExpirationStrategy(), getNodeRevivalStrategy(),
				getRelationshipRevivalStrategy(), getExpiryOffset(), getRevivalOffset());
	}

	public LifecycleConfiguration withNodeExpirationIndex(String nodeExpirationIndex) {
		return new LifecycleConfiguration(getInclusionPolicies(), initializeUntil(), getInstanceRolePolicy(), nodeExpirationIndex, getRelationshipExpirationIndex(), getNodeExpirationProperty(), getRelationshipExpirationProperty(), getNodeTtlProperty(), getRelationshipTtlProperty(), getNodeRevivalProperty(), getRelationshipRevivalProperty(), getMaxNoExpirations(), getNodeExpirationStrategy(), getRelationshipExpirationStrategy(), getNodeRevivalStrategy(), getRelationshipRevivalStrategy(), getExpiryOffset(), getRevivalOffset());
	}

	public LifecycleConfiguration withRelationshipExpirationIndex(String relationshipExpirationIndex) {
		return new LifecycleConfiguration(getInclusionPolicies(), initializeUntil(), getInstanceRolePolicy(), getNodeExpirationIndex(), relationshipExpirationIndex, getNodeExpirationProperty(), getRelationshipExpirationProperty(), getNodeTtlProperty(), getRelationshipTtlProperty(), getNodeRevivalProperty(), getRelationshipRevivalProperty(), getMaxNoExpirations(), getNodeExpirationStrategy(), getRelationshipExpirationStrategy(), getNodeRevivalStrategy(), getRelationshipRevivalStrategy(), getExpiryOffset(), getRevivalOffset());
	}

	public LifecycleConfiguration withNodeExpirationProperty(String nodeExpirationProperty) {
		return new LifecycleConfiguration(getInclusionPolicies(), initializeUntil(), getInstanceRolePolicy(), getNodeExpirationIndex(), getRelationshipExpirationIndex(), nodeExpirationProperty, getRelationshipExpirationProperty(), getNodeTtlProperty(), getRelationshipTtlProperty(), getNodeRevivalProperty(), getRelationshipRevivalProperty(), getMaxNoExpirations(), getNodeExpirationStrategy(), getRelationshipExpirationStrategy(), getNodeRevivalStrategy(), getRelationshipRevivalStrategy(), getExpiryOffset(), getRevivalOffset());
	}

	public LifecycleConfiguration withRelationshipExpirationProperty(String relationshipExpirationProperty) {
		return new LifecycleConfiguration(getInclusionPolicies(), initializeUntil(), getInstanceRolePolicy(), getNodeExpirationIndex(), getRelationshipExpirationIndex(), getNodeExpirationProperty(), relationshipExpirationProperty, getNodeTtlProperty(), getRelationshipTtlProperty(), getNodeRevivalProperty(), getRelationshipRevivalProperty(), getMaxNoExpirations(), getNodeExpirationStrategy(), getRelationshipExpirationStrategy(), getNodeRevivalStrategy(), getRelationshipRevivalStrategy(), getExpiryOffset(), getRevivalOffset());
	}

	public LifecycleConfiguration withNodeTtlProperty(String nodeTtlProperty) {
		return new LifecycleConfiguration(getInclusionPolicies(), initializeUntil(), getInstanceRolePolicy(), getNodeExpirationIndex(), getRelationshipExpirationIndex(), getNodeExpirationProperty(), getRelationshipExpirationProperty(), nodeTtlProperty, getRelationshipTtlProperty(), getNodeRevivalProperty(), getRelationshipRevivalProperty(), getMaxNoExpirations(), getNodeExpirationStrategy(), getRelationshipExpirationStrategy(), getNodeRevivalStrategy(), getRelationshipRevivalStrategy(), getExpiryOffset(), getRevivalOffset());
	}

	public LifecycleConfiguration withRelationshipTtlProperty(String relationshipTtlProperty) {
		return new LifecycleConfiguration(getInclusionPolicies(), initializeUntil(), getInstanceRolePolicy(), getNodeExpirationIndex(), getRelationshipExpirationIndex(), getNodeExpirationProperty(), getRelationshipExpirationProperty(), getNodeTtlProperty(), relationshipTtlProperty, getNodeRevivalProperty(), getRelationshipRevivalProperty(), getMaxNoExpirations(), getNodeExpirationStrategy(), getRelationshipExpirationStrategy(), getNodeRevivalStrategy(), getRelationshipRevivalStrategy(), getExpiryOffset(), getRevivalOffset());
	}

	public LifecycleConfiguration withNodeRevivalProperty(String nodeRevivalProperty) {
		return new LifecycleConfiguration(getInclusionPolicies(), initializeUntil(), getInstanceRolePolicy(), getNodeExpirationIndex(), getRelationshipExpirationIndex(), getNodeExpirationProperty(), getRelationshipExpirationProperty(), getNodeTtlProperty(), getRelationshipTtlProperty(), nodeRevivalProperty, getRelationshipRevivalProperty(), getMaxNoExpirations(), getNodeExpirationStrategy(), getRelationshipExpirationStrategy(), getNodeRevivalStrategy(), getRelationshipRevivalStrategy(), getExpiryOffset(), getRevivalOffset());
	}

	public LifecycleConfiguration withRelationshipRevivalProperty(String relationshipRevivalProperty) {
		return new LifecycleConfiguration(getInclusionPolicies(), initializeUntil(), getInstanceRolePolicy(), getNodeExpirationIndex(), getRelationshipExpirationIndex(), getNodeExpirationProperty(), getRelationshipExpirationProperty(), getNodeTtlProperty(), getRelationshipTtlProperty(), getNodeRevivalProperty(), relationshipRevivalProperty, getMaxNoExpirations(), getNodeExpirationStrategy(), getRelationshipExpirationStrategy(), getNodeRevivalStrategy(), getRelationshipRevivalStrategy(), getExpiryOffset(), getRevivalOffset());
	}

	public LifecycleConfiguration withNodeExpirationStrategy(LifecycleEventStrategy<Node> nodeExpirationStrategy) {
		return new LifecycleConfiguration(getInclusionPolicies(), initializeUntil(), getInstanceRolePolicy(), getNodeExpirationIndex(), getRelationshipExpirationIndex(), getNodeExpirationProperty(), getRelationshipExpirationProperty(), getNodeTtlProperty(), getRelationshipTtlProperty(), getNodeRevivalProperty(), getRelationshipRevivalProperty(), getMaxNoExpirations(), nodeExpirationStrategy, getRelationshipExpirationStrategy(), getNodeRevivalStrategy(), getRelationshipRevivalStrategy(), getExpiryOffset(), getRevivalOffset());
	}

	public LifecycleConfiguration withRelationshipExpirationStrategy(LifecycleEventStrategy<Relationship> relationshipExpirationStrategy) {
		return new LifecycleConfiguration(getInclusionPolicies(), initializeUntil(), getInstanceRolePolicy(), getNodeExpirationIndex(), getRelationshipExpirationIndex(), getNodeExpirationProperty(), getRelationshipExpirationProperty(), getNodeTtlProperty(), getRelationshipTtlProperty(), getNodeRevivalProperty(), getRelationshipRevivalProperty(), getMaxNoExpirations(), getNodeExpirationStrategy(), relationshipExpirationStrategy, getNodeRevivalStrategy(), getRelationshipRevivalStrategy(), getExpiryOffset(), getRevivalOffset());
	}

	public LifecycleConfiguration withNodeRevivalStrategy(CompositeStrategy<Node> strategy) {
		return new LifecycleConfiguration(getInclusionPolicies(), initializeUntil(), getInstanceRolePolicy(), getNodeExpirationIndex(), getRelationshipExpirationIndex(), getNodeExpirationProperty(), getRelationshipExpirationProperty(), getNodeTtlProperty(), getRelationshipTtlProperty(), getNodeRevivalProperty(), getRelationshipRevivalProperty(), getMaxNoExpirations(), getNodeExpirationStrategy(), getRelationshipExpirationStrategy(), strategy, getRelationshipRevivalStrategy(), getExpiryOffset(), getRevivalOffset());
	}

	public LifecycleConfiguration withRelationshipRevivalStrategy(CompositeStrategy<Relationship> strategy) {
		return new LifecycleConfiguration(getInclusionPolicies(), initializeUntil(), getInstanceRolePolicy(), getNodeExpirationIndex(), getRelationshipExpirationIndex(), getNodeExpirationProperty(), getRelationshipExpirationProperty(), getNodeTtlProperty(), getRelationshipTtlProperty(), getNodeRevivalProperty(), getRelationshipRevivalProperty(), getMaxNoExpirations(), getNodeExpirationStrategy(), getRelationshipExpirationStrategy(), getNodeRevivalStrategy(), strategy, getExpiryOffset(), getRevivalOffset());
	}

	public LifecycleConfiguration withMaxNoExpirations(int maxNoExpirations) {
		return new LifecycleConfiguration(getInclusionPolicies(), initializeUntil(), getInstanceRolePolicy(), getNodeExpirationIndex(), getRelationshipExpirationIndex(), getNodeExpirationProperty(), getRelationshipExpirationProperty(), getNodeTtlProperty(), getRelationshipTtlProperty(), getNodeRevivalProperty(), getRelationshipRevivalProperty(), maxNoExpirations, getNodeExpirationStrategy(), getRelationshipExpirationStrategy(), getNodeRevivalStrategy(), getRelationshipRevivalStrategy(), getExpiryOffset(), getRevivalOffset());
	}

	public LifecycleConfiguration withExpiryOffset(long expiryOffset) {
		return new LifecycleConfiguration(getInclusionPolicies(), initializeUntil(), getInstanceRolePolicy(), getNodeExpirationIndex(), getRelationshipExpirationIndex(), getNodeExpirationProperty(), getRelationshipExpirationProperty(), getNodeTtlProperty(), getRelationshipTtlProperty(), getNodeRevivalProperty(), getRelationshipRevivalProperty(), getMaxNoExpirations(), getNodeExpirationStrategy(), getRelationshipExpirationStrategy(), getNodeRevivalStrategy(), getRelationshipRevivalStrategy(), expiryOffset, getRevivalOffset());
	}

	public LifecycleConfiguration withRevivalOffset(long revivalOffset) {
		return new LifecycleConfiguration(getInclusionPolicies(), initializeUntil(), getInstanceRolePolicy(), getNodeExpirationIndex(), getRelationshipExpirationIndex(), getNodeExpirationProperty(), getRelationshipExpirationProperty(), getNodeTtlProperty(), getRelationshipTtlProperty(), getNodeRevivalProperty(), getRelationshipRevivalProperty(), getMaxNoExpirations(), getNodeExpirationStrategy(), getRelationshipExpirationStrategy(), getNodeRevivalStrategy(), getRelationshipRevivalStrategy(), getExpiryOffset(), revivalOffset);
	}

	public String getNodeExpirationIndex() {
		return nodeExpirationIndex;
	}

	public String getNodeRevivalIndex() {
		return nodeRevivalIndex;
	}

	public String getRelationshipRevivalIndex() {
		return relationshipRevivalIndex;
	}

	public String getRelationshipExpirationIndex() {
		return relationshipExpirationIndex;
	}

	public String getNodeExpirationProperty() {
		return nodeExpirationProperty;
	}

	public String getRelationshipExpirationProperty() {
		return relationshipExpirationProperty;
	}

	public String getNodeTtlProperty() {
		return nodeTtlProperty;
	}

	public String getRelationshipTtlProperty() {
		return relationshipTtlProperty;
	}

	public String getNodeRevivalProperty() {
		return nodeRevivalProperty;
	}

	public String getRelationshipRevivalProperty() {
		return relationshipRevivalProperty;
	}

	public LifecycleEventStrategy<Node> getNodeExpirationStrategy() {
		return nodeExpirationStrategy;
	}

	public LifecycleEventStrategy<Relationship> getRelationshipExpirationStrategy() {
		return relationshipExpirationStrategy;
	}

	public LifecycleEventStrategy<Node> getNodeRevivalStrategy() {
		return nodeRevivalStrategy;
	}

	public LifecycleEventStrategy<Relationship> getRelationshipRevivalStrategy() {
		return relationshipRevivalStrategy;
	}

	public long getExpiryOffset() {
		return expiryOffset;
	}

	public long getRevivalOffset() {
		return revivalOffset;
	}

	public int getMaxNoExpirations() {
		return maxNoExpirations;
	}

	public void validate() {
		if (maxNoExpirations < 0) {
			throw new IllegalStateException("Max number of expirations must be at least 0 (ideally > 0)!");
		}
	}

	/**
	 * Build events based on current config.
	 *
	 * @return
	 */
	public List<ScheduledEvent> scheduledEvents() {
		cleanup();

		List<ScheduledEvent> events = new ArrayList<>();

		if (nodeExpirationIndex != null || relationshipExpirationIndex != null) {
			events.add(new ExpiryEvent(getNodeExpirationProperty(), getNodeTtlProperty(), getRelationshipExpirationProperty(), getRelationshipTtlProperty(), getExpiryOffset(), getNodeExpirationIndex(), getRelationshipExpirationIndex(), getNodeExpirationStrategy(), getRelationshipExpirationStrategy()));
		}

		return events;
	}

	public List<CommitEvent> commitEvents() {
		cleanup();

		List<CommitEvent> events = new ArrayList<>();
		if (nodeRevivalIndex != null || relationshipRevivalIndex != null) {
			events.add(new RevivalEvent(getNodeRevivalProperty(), getRelationshipRevivalProperty(), getNodeRevivalStrategy(), getRelationshipRevivalStrategy(), getRevivalOffset()));
		}
		return events;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof LifecycleConfiguration)) return false;
		if (!super.equals(o)) return false;

		LifecycleConfiguration that = (LifecycleConfiguration) o;

		if (maxNoExpirations != that.maxNoExpirations) return false;
		if (expiryOffset != that.expiryOffset) return false;
		if (revivalOffset != that.revivalOffset) return false;
		if (nodeExpirationIndex != null ? !nodeExpirationIndex.equals(that.nodeExpirationIndex) : that.nodeExpirationIndex != null)
			return false;
		if (relationshipExpirationIndex != null ? !relationshipExpirationIndex.equals(that.relationshipExpirationIndex) : that.relationshipExpirationIndex != null)
			return false;
		if (nodeRevivalIndex != null ? !nodeRevivalIndex.equals(that.nodeRevivalIndex) : that.nodeRevivalIndex != null)
			return false;
		if (relationshipRevivalIndex != null ? !relationshipRevivalIndex.equals(that.relationshipRevivalIndex) : that.relationshipRevivalIndex != null)
			return false;
		if (nodeExpirationProperty != null ? !nodeExpirationProperty.equals(that.nodeExpirationProperty) : that.nodeExpirationProperty != null)
			return false;
		if (relationshipExpirationProperty != null ? !relationshipExpirationProperty.equals(that.relationshipExpirationProperty) : that.relationshipExpirationProperty != null)
			return false;
		if (nodeTtlProperty != null ? !nodeTtlProperty.equals(that.nodeTtlProperty) : that.nodeTtlProperty != null)
			return false;
		if (relationshipTtlProperty != null ? !relationshipTtlProperty.equals(that.relationshipTtlProperty) : that.relationshipTtlProperty != null)
			return false;
		if (nodeRevivalProperty != null ? !nodeRevivalProperty.equals(that.nodeRevivalProperty) : that.nodeRevivalProperty != null)
			return false;
		if (relationshipRevivalProperty != null ? !relationshipRevivalProperty.equals(that.relationshipRevivalProperty) : that.relationshipRevivalProperty != null)
			return false;
		if (nodeExpirationStrategy != null ? !nodeExpirationStrategy.equals(that.nodeExpirationStrategy) : that.nodeExpirationStrategy != null)
			return false;
		if (relationshipExpirationStrategy != null ? !relationshipExpirationStrategy.equals(that.relationshipExpirationStrategy) : that.relationshipExpirationStrategy != null)
			return false;
		if (nodeRevivalStrategy != null ? !nodeRevivalStrategy.equals(that.nodeRevivalStrategy) : that.nodeRevivalStrategy != null)
			return false;
		return relationshipRevivalStrategy != null ? relationshipRevivalStrategy.equals(that.relationshipRevivalStrategy) : that.relationshipRevivalStrategy == null;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (nodeExpirationIndex != null ? nodeExpirationIndex.hashCode() : 0);
		result = 31 * result + (relationshipExpirationIndex != null ? relationshipExpirationIndex.hashCode() : 0);
		result = 31 * result + (nodeRevivalIndex != null ? nodeRevivalIndex.hashCode() : 0);
		result = 31 * result + (relationshipRevivalIndex != null ? relationshipRevivalIndex.hashCode() : 0);
		result = 31 * result + (nodeExpirationProperty != null ? nodeExpirationProperty.hashCode() : 0);
		result = 31 * result + (relationshipExpirationProperty != null ? relationshipExpirationProperty.hashCode() : 0);
		result = 31 * result + (nodeTtlProperty != null ? nodeTtlProperty.hashCode() : 0);
		result = 31 * result + (relationshipTtlProperty != null ? relationshipTtlProperty.hashCode() : 0);
		result = 31 * result + (nodeRevivalProperty != null ? nodeRevivalProperty.hashCode() : 0);
		result = 31 * result + (relationshipRevivalProperty != null ? relationshipRevivalProperty.hashCode() : 0);
		result = 31 * result + (nodeExpirationStrategy != null ? nodeExpirationStrategy.hashCode() : 0);
		result = 31 * result + (relationshipExpirationStrategy != null ? relationshipExpirationStrategy.hashCode() : 0);
		result = 31 * result + (nodeRevivalStrategy != null ? nodeRevivalStrategy.hashCode() : 0);
		result = 31 * result + (relationshipRevivalStrategy != null ? relationshipRevivalStrategy.hashCode() : 0);
		result = 31 * result + maxNoExpirations;
		result = 31 * result + (int) (expiryOffset ^ (expiryOffset >>> 32));
		result = 31 * result + (int) (revivalOffset ^ (revivalOffset >>> 32));
		return result;
	}

	private void cleanup() {
		if (StringUtils.isBlank(nodeExpirationIndex)) {
			nodeExpirationProperty = null;
			nodeTtlProperty = null;
		}

		if (StringUtils.isBlank(relationshipExpirationIndex)) {
			relationshipExpirationProperty = null;
			relationshipTtlProperty = null;
		}

		if (StringUtils.isBlank(nodeExpirationProperty) && StringUtils.isBlank(nodeTtlProperty)) {
			nodeExpirationIndex = null;
		}

		if (StringUtils.isBlank(relationshipExpirationProperty) && StringUtils.isBlank(relationshipTtlProperty)) {
			relationshipExpirationIndex = null;
		}

		if (StringUtils.isBlank(nodeRevivalProperty)) {
			nodeRevivalIndex = null;
		}

		if (StringUtils.isBlank(relationshipRevivalProperty)) {
			relationshipRevivalIndex = null;
		}
	}
}
