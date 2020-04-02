/*
 * Copyright (c) 2013-2020 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation, either
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
 * {@link InstanceRolePolicy} with which any {@link InstanceRole} that is writable (i.e., {@link InstanceRole#isWritable()} returns <code>true</code>)
 * complies. Singleton.
 * <p>
 * A module with this policy will run only on:
 * - {@link InstanceRole#MASTER} in case of HA.
 * - {@link InstanceRole#LEADER} in case of CC.
 * - {@link InstanceRole#SINGLE} in case of no clustering.
 * <p>
 * Note that slaves in HA are technically writable but not for the purposes of this class and related framework mechanisms.
 */
public final class WritableRole implements InstanceRolePolicy {

    private static final WritableRole INSTANCE = new WritableRole();

    public static WritableRole getInstance() {
        return INSTANCE;
    }

    private WritableRole() {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean comply(InstanceRole role) {
        return role.isWritable();
    }
}
