/*
 * Copyright (c) 2013-2016 GraphAware
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

package com.graphaware.lifecycle.event.strategy;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LifecycleEventStrategyFactory {


	public static LifecycleEventStrategy strategy(String name, Map<String, String> config) {

		if (name == null) {
			return null;
		}

		LifecycleEventStrategy instance;
		if (DeleteNodeAndRelationships.CONFIG_KEY.equals(name)) {
			instance = new DeleteNodeAndRelationships();
		} else if (DeleteOrphanedNodeOnly.CONFIG_KEY.endsWith(name)) {
			instance = new DeleteOrphanedNodeOnly();
		} else if (AddRemoveLabels.EXPIRY_CONFIG_KEY.endsWith(name)) {
			List<String> labelsToAdd = toList(config.get("nodeExpirationStrategy.labelsToAdd"));
			List<String> labelsToRemove = toList(config.get("nodeExpirationStrategy.labelsToRemove"));
			instance = new AddRemoveLabels(labelsToAdd, labelsToRemove);
		}
		else if (AddRemoveLabels.REVIVAL_CONFIG_KEY.endsWith(name)) {
			List<String> labelsToAdd = toList(config.get("nodeRevivalStrategy.labelsToAdd"));
			List<String> labelsToRemove = toList(config.get("nodeRevivalStrategy.labelsToRemove"));
			instance = new AddRemoveLabels(labelsToAdd, labelsToRemove);
		}
		else if (DeleteRelationship.CONFIG_KEY.equals(name)) {
			instance = new DeleteRelationship();
		} else {
			throw new IllegalArgumentException(String.format("No strategy for name %s", name));
		}
		return instance;
	}

	private static List<String> toList(String commaSeparated) {
		if (commaSeparated != null) {
			commaSeparated = commaSeparated.replaceAll("^\\[|]$", "");
			return Arrays.stream(commaSeparated.split(",")).map(String::trim).collect(Collectors.toList());
		}
		return Collections.emptyList();
	}


}
