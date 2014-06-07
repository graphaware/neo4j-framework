package com.graphaware.crawler.api;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

/**
 * Encapsulates contextual information about the ongoing graph crawling.
 */
public class Context {

	private final Node currentNode;
	private final Relationship howDidIGetHere;

	/**
	 * Constructs a new {@link Context} containing the given information about the current point in the graph crawl.
	 *
	 * @param currentNode The {@link Node} that's currently being visited
	 * @param howDidIGetHere The {@link Relationship} that was followed to reach the current node, which may be
	 *        <code>null</code> if it's the first node of all
	 */
	public Context(Node currentNode, Relationship howDidIGetHere) {
		this.currentNode = currentNode;
		this.howDidIGetHere = howDidIGetHere;
	}

	/**
	 * @return The {@link Node} that's currently being visited by the graph crawler
	 */
	public Node getCurrentNode() {
		return currentNode;
	}

	/**
	 * @return The {@link Relationship} that was followed to reach the current node, which will be <code>null</code> if the node
	 *         was visited directly
	 */
	public Relationship getHowDidIGetHere() {
		return howDidIGetHere;
	}

}
