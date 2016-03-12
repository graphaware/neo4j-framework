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
