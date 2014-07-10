package com.graphaware.example.pagerank.parser;

import java.util.Map;
import java.util.Map.Entry;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Node;

import com.graphaware.common.strategy.InclusionStrategy;

public class LabelAndPropertyDrivenNodeInclusionStrategy implements InclusionStrategy<Node> {

	private final Map<String, Object> propertiesToMatch;
	private final String label;

	LabelAndPropertyDrivenNodeInclusionStrategy(String label, Map<String, Object> propertiesToMatch) {
		this.propertiesToMatch = propertiesToMatch;
		this.label = label;
	}

	@Override
	public boolean include(Node node) {
		for (Entry<String, ?> entry : propertiesToMatch.entrySet()) {
			Object property = node.getProperty(entry.getKey(), null);
			if (property == null || !property.equals(entry.getValue())) {
				return false;
			}
		}
		return (label.isEmpty() && node.getLabels() == null) || node.hasLabel(DynamicLabel.label(label));
	}

}
