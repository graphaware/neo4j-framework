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

package com.graphaware.common.stopwatch;

import java.util.HashMap;

public class Stopwatch {

    private HashMap<String, Event> events = new HashMap<>();

    public void start(String eventName) {
        startEvent(eventName);
    }

    public Event stop(String eventName) {
        return stopEvent(eventName);
    }

    public void lap(String eventName) {
        ensureEventExists(eventName);
        events.get(eventName).addPeriod();
    }

    private void startEvent(String eventName) {
        if (events.containsKey(eventName)) {
            throw new IllegalArgumentException("An event with name " + eventName + " was already started");
        }

        events.put(eventName, new Event());
    }

    private Event stopEvent(String eventName) {
        ensureEventExists(eventName);
        events.get(eventName).stop();

        return events.get(eventName);
    }

    private void ensureEventExists(String eventName) {
        if (!events.containsKey(eventName)) {
            throw new IllegalArgumentException("No event with name " + eventName + " has been started");
        }
    }

}
