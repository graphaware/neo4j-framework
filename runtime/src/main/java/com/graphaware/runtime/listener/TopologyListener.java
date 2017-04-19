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

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by alberto.delazzari on 19/04/17.
 */
public interface TopologyListener {

    /**
     * Register this listener in order to intercept topology change events
     */
    void register();

    /**
     * Un-register this listener in order to ignore further topology change events
     */
    void unregister();

    // ----- Inner implementation for a TopologyChangeEvent ---- ///

    class TopologyChangeEventImpl implements TopologyChangeEvent {

        /**
         * The instance id (joining or leaving the cluster so that generating the event)
         */
        private final String instanceId;

        /**
         * The instance id receiving the event
         */
        private final String ownInstanceId;

        /**
         * The event type
         */
        private final EventType eventType;

        /**
         * The instance role receiving the event
         */
        private final InstanceRole ownInstanceRole;

        public TopologyChangeEventImpl(String instanceId, String ownInstanceId, InstanceRole ownInstanceRole, EventType eventType) {
            this.instanceId = instanceId;
            this.ownInstanceId = ownInstanceId;
            this.ownInstanceRole = ownInstanceRole;
            this.eventType = eventType;
        }

        @Override
        public String getInstanceId() {
            return instanceId;
        }

        @Override
        public String getOwnInstanceId() {
            return ownInstanceId;
        }

        @Override
        public InstanceRole getOwnInstanceRole() {
            return ownInstanceRole;
        }

        public EventType getEventType() {
            return eventType;
        }

        @Override
        public String toString() {
            return Stream.of("[instanceId=", getInstanceId(),
                    ",ownInstanceId=", getOwnInstanceId(),
                    ",ownInstanceRole=", getOwnInstanceRole().toString(),
                    ",eventType=", getEventType().toString() + "]")
                    .collect(Collectors.joining());
        }
    }
}
