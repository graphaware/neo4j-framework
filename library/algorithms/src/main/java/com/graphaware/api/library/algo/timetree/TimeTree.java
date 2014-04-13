package com.graphaware.api.library.algo.timetree;

import org.joda.time.DateTimeZone;
import org.neo4j.graphdb.Node;

/**
 * API for representing time as a tree. Provides methods for creating and retrieving nodes that represent instances of
 * time, making sure that a tree of time is maintained with each created node. There is no support for changes or deletes.
 */
public interface TimeTree {

    /**
     * Get a node representing this time instant. If one doesn't exist, it will be created.
     * <p/>
     * The resolution of the time instant (i.e., whether it is a day, hour, minute, etc.) depends on the implementation,
     * which can choose a sensible default, require to be configured with a default when instantiated, or both.
     * <p/>
     * The time zone of the time instant depends on the implementation, which can choose a default, require to be
     * configured with a default when instantiated, or both.
     *
     * @return node representing the time instant when this method was called.
     */
    Node getNow();

    /**
     * Get a node representing this time instant. If one doesn't exist, it will be created.
     * <p/>
     * The resolution of the time instant (i.e., whether it is a day, hour, minute, etc.) depends on the implementation,
     * which can choose a sensible default, require to be configured with a default when instantiated, or both.
     *
     * @param timeZone specific time zone.
     * @return node representing the time instant when this method was called.
     */
    Node getNow(DateTimeZone timeZone);

    /**
     * Get a node representing this time instant. If one doesn't exist, it will be created.
     * <p/>
     * The time zone of the time instant depends on the implementation, which can choose a default, require to be
     * configured with a default when instantiated, or both.
     *
     * @param resolution specific resolution.
     * @return node representing the time instant when this method was called.
     */
    Node getNow(Resolution resolution);

    /**
     * Get a node representing this time instant. If one doesn't exist, it will be created.
     *
     * @param timeZone   specific time zone.
     * @param resolution specific resolution.
     * @return node representing the time instant when this method was called.
     */
    Node getNow(DateTimeZone timeZone, Resolution resolution);

    /**
     * Get a node representing a specific time instant. If one doesn't exist, it will be created.
     * <p/>
     * The resolution of the time instant (i.e., whether it is a day, hour, minute, etc.) depends on the implementation,
     * which can choose a sensible default, require to be configured with a default when instantiated, or both.
     * <p/>
     * The time zone of the time instant depends on the implementation, which can choose a default, require to be
     * configured with a default when instantiated, or both.
     *
     * @param time UTC time in ms from 1/1/1970.
     * @return node representing a specific time instant.
     */
    Node getInstant(long time);

    /**
     * Get a node representing a specific time instant. If one doesn't exist, it will be created.
     * <p/>
     * The resolution of the time instant (i.e., whether it is a day, hour, minute, etc.) depends on the implementation,
     * which can choose a sensible default, require to be configured with a default when instantiated, or both.
     *
     * @param time     UTC time in ms from 1/1/1970.
     * @param timeZone specific time zone.
     * @return node representing a specific time instant.
     */
    Node getInstant(long time, DateTimeZone timeZone);

    /**
     * Get a node representing a specific time instant. If one doesn't exist, it will be created.
     * <p/>
     * The time zone of the time instant depends on the implementation, which can choose a default, require to be
     * configured with a default when instantiated, or both.
     *
     * @param time       UTC time in ms from 1/1/1970.
     * @param resolution specific resolution.
     * @return node representing a specific time instant.
     */
    Node getInstant(long time, Resolution resolution);

    /**
     * Get a node representing a specific time instant. If one doesn't exist, it will be created.
     *
     * @param time       UTC time in ms from 1/1/1970.
     * @param timeZone   specific time zone.
     * @param resolution specific resolution.
     * @return node representing a specific time instant.
     */
    Node getInstant(long time, DateTimeZone timeZone, Resolution resolution);


}
