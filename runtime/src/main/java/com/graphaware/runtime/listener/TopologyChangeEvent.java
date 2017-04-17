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

package com.graphaware.runtime.listener;

import com.graphaware.common.policy.role.InstanceRole;

/**
 * Abstraction of a topology change event.
 * This event will be fired both with HA cluster and Causal cluster
 */
public interface TopologyChangeEvent {

    /**
     * The id that is related to the instance leaving, joining the cluster or elected
     *
     * @return instance id related to the instance leaving, joining the cluster or elected
     */
    String getInstanceId();

    /**
     * The id that is related to the instance that is receiving the event
     *
     * @return instance id for this instance
     */
    String getOwnInstanceId();

    /**
     * The role that is related to the instance that is receiving the event
     *
     * @return instance role for this instance {@link InstanceRole}
     */
    InstanceRole getOwnInstanceRole();

    /**
     * @return type of the event that is occurred {@link EventType}
     */
    EventType getEventType();

    /**
     * Type of events that are normalized respect to the different events that can occurs when running an HA or a Causal cluster
     */
    enum EventType {
        // When an instance leave the cluster
        CLUSTER_LEAVE,
        // When an instance join the cluster
        CLUSTER_JOIN,
        // When an election occurs
        ELECTION
    }
}
