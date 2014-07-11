package com.graphaware.example.pagerank.parser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.shell.ShellException;
import org.neo4j.shell.util.json.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.graphaware.common.strategy.InclusionStrategy;
import com.graphaware.common.strategy.RelationshipInclusionStrategy;

/**
 * Implementation of {@link ModuleConfigParameterParser} that uses simple regular expressions to parse the configuration
 * parameter values.
 */
public class RegexModuleConfigParameterParser implements ModuleConfigParameterParser {

	private static final Logger LOG = LoggerFactory.getLogger(RegexModuleConfigParameterParser.class);

	private static final Pattern NODE_EXPRESSION_PATTERN = Pattern.compile("(\\w*)\\s*\\{?(.*)\\}?");

	@SuppressWarnings("unchecked")
	@Override
	public InclusionStrategy<Node> parseForNodeInclusionStrategy(String nodeExpression) {
		if (nodeExpression == null) {
			LOG.info("Null node expression specified so no inclusion strategy will be returned");
			return null;
		}

		Matcher m = NODE_EXPRESSION_PATTERN.matcher(nodeExpression);
		if (!m.matches()) {
			LOG.warn("Node expression \"{}\" is invalid or unsupported.  No inclusion strategy can be determined", nodeExpression);
			return null; // TODO: should raise an exception here, probably
		}

		final Map<String, Object> propertiesToMatch = new HashMap<>();
		try {
			propertiesToMatch.putAll((Map<String, Object>) JSONParser.parse('{' + m.group(2) + '}'));
		} catch (ShellException e) {
			LOG.warn("Parameter part of node expression: \"" + nodeExpression + "\" couldn't be understood.  "
					+ "As a result, the inclusion strategy will not be based on node properties.", e);
		}

		return new LabelAndPropertyDrivenNodeInclusionStrategy(m.group(1), propertiesToMatch);
	}

	@Override
	public InclusionStrategy<Relationship> parseForRelationshipInclusionStrategy(String relExpression) {
		if (relExpression == null) {
			LOG.info("Null relationship expression specified so no inclusion strategy will be returned");
			return null;
		}

		final List<String> relationshipsToMatch = Arrays.asList(relExpression.split("\\|"));

		return new RelationshipInclusionStrategy() {
			@Override
			public boolean include(Relationship object) {
				return relationshipsToMatch.contains(object.getType().name());
			}
		};
	}

}
