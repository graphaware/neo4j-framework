package com.graphaware.example.pagerank.parser;

import java.util.Map;
import java.util.Map.Entry;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Node;

import com.graphaware.common.strategy.InclusionStrategy;

/**
 * Node-based {@link InclusionStrategy} that includes nodes that match a particular label and set of properties.
 */
public class LabelAndPropertyDrivenNodeInclusionStrategy implements InclusionStrategy<Node> {

	private final Map<String, Object> propertiesToMatch;
	private final String labelToMatch;

	LabelAndPropertyDrivenNodeInclusionStrategy(String label, Map<String, Object> propertiesToMatch) {
		this.propertiesToMatch = propertiesToMatch;
		this.labelToMatch = label;
	}

	@Override
	public boolean include(Node node) {
		for (Entry<String, Object> entry : this.propertiesToMatch.entrySet()) {
			Object property = node.getProperty(entry.getKey(), null);
			if (property == null || !property.equals(entry.getValue())) {
				return false;
			}
		}
		return this.labelToMatch.isEmpty() || node.hasLabel(DynamicLabel.label(this.labelToMatch));
	}

}
