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

package com.graphaware.common.policy.role;

/**
 * The role of a database instance within a cluster.
 */
public enum InstanceRole {

    // CAUSAL CLUSTER
    FOLLOWER,
    CANDIDATE,
    LEADER,
    READ_REPLICA,

    // HA CLUSTER
    MASTER,
    SLAVE,

    // SINGLE NODE
    SINGLE;

    /**
     * Check if the instance has write permission.
     *
     * @return true if the instance can write into the database.
     */
    public boolean isWritable() {
        return MASTER.equals(this) || SINGLE.equals(this) || LEADER.equals(this);
    }

    /**
     * Check if the instance is read only.
     *
     * @return true if the instance cannot write into the database.
     */
    public boolean isReadOnly() {
        return !isWritable();
    }
}
