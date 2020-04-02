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
 * {@link InstanceRolePolicy} with which only {@link InstanceRole#SLAVE} complies. I.e., a module with this policy will run only on slaves in HA setup. Singleton.
 */
public final class SlavesOnly implements InstanceRolePolicy {

    private static final SlavesOnly INSTANCE = new SlavesOnly();

    private SlavesOnly() {
    }

    public static SlavesOnly getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean comply(InstanceRole role) {
        return InstanceRole.SLAVE.equals(role);
    }
}
