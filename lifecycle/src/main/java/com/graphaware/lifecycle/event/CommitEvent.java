package com.graphaware.lifecycle.event;

import org.neo4j.graphdb.Entity;

public interface CommitEvent<E extends Entity> extends LifecycleEvent {

	/**
	 * {@inheritDoc}
	 */
	Class<E> appliesTo();

	/**
	 * Evaluate necessity of, and execute expiry of an Entity.
	 *
	 * @param entity to expire
	 */
	boolean applyIfNeeded(E entity);

}
