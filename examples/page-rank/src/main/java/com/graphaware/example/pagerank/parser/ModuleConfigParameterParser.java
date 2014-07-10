package com.graphaware.example.pagerank.parser;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import com.graphaware.common.strategy.InclusionStrategy;

/**
 * Parses configuration parameters for this module and decodes them into a useful form.
 */
public interface ModuleConfigParameterParser {

	/**
	 * Parses the given Cypher node expression and returns an {@link InclusionStrategy} that will include {@link Node}s
	 * that share <em>at least</em> the same labels and properties as those expressed in the given argument.
	 * <p>
	 * A leading identifier, colon (:) or parentheses should <b>not</b> be included in the specified expression; only the label
	 * and any properties to match are currently supported.
	 * </p>
	 *
	 * @param nodeExpression The Cypher "MATCH" style expression that identifies nodes to include
	 * @return An {@link InclusionStrategy} based on the given expression or <code>null</code> if invoked with <code>null</code>
	 */
	InclusionStrategy<Node> parseForNodeInclusionStrategy(String nodeExpression);

	/**
	 * Parses the given Cypher expression and returns an {@link InclusionStrategy} that will include {@link Relationship}s that
	 * match the same types as those expressed in the given argument.
	 * <p>
	 * No square brackets are necessary and a leading identifier or colon (:) shouldn't be included.  A pipe (|) symbol can be
	 * used to include multiple types of relationship.
	 * </p>
	 *
	 * @param nodeExpression The Cypher "MATCH" style expression that identifies nodes to include
	 * @return An {@link InclusionStrategy} based on the given expression or <code>null</code> if invoked with <code>null</code>
	 */
	InclusionStrategy<Relationship> parseForRelationshipInclusionStrategy(String relationshipExpression);

}
