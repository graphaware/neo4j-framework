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

package com.graphaware.lifecycle.strategy;

import java.util.*;
import java.util.stream.Collectors;

import com.graphaware.common.serialize.Serializer;
import com.graphaware.common.serialize.SingletonSerializer;
import com.graphaware.lifecycle.event.LifecycleEvent;
import com.graphaware.lifecycle.event.commit.RevivalEvent;
import com.graphaware.lifecycle.event.scheduled.ExpiryEvent;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;


public final class AddRemoveLabels extends LifecycleEventStrategy<Node> {

	private Map<Class, List<String>> labelsToAdd = new HashMap<>();
	private Map<Class, List<String>> labelsToRemove = new HashMap<>();

	static {
		Serializer.register(AddRemoveLabels.class, new SingletonSerializer());
	}

	private static final AddRemoveLabels INSTANCE = new AddRemoveLabels();

	public static AddRemoveLabels getInstance() {
		return INSTANCE;
	}

	private AddRemoveLabels() {
	}


	@Override
	public void setConfig(Map<String, String> config) {
		super.setConfig(config);

		labelsToAdd.put(ExpiryEvent.class, toList(config, "nodeExpirationStrategy.labelsToAdd"));
		labelsToRemove.put(ExpiryEvent.class, toList(config, "nodeExpirationStrategy.labelsToRemove"));

		labelsToAdd.put(RevivalEvent.class, toList(config, "nodeRevivalStrategy.labelsToAdd"));
		labelsToRemove.put(RevivalEvent.class, toList(config, "nodeRevivalStrategy.labelsToRemove"));
	}

	@Override
	public boolean applyIfNeeded(Node node, LifecycleEvent event) {
		for (String label : this.labelsToRemove.get(event.getClass())) {
			node.removeLabel(Label.label(label));
		}
		for (String label : this.labelsToAdd.get(event.getClass())) {
			node.addLabel(Label.label(label));
		}
		return true;
	}

	private List<String> toList(Map<String, String> config, String propertyName) {
		String listOfLabels = config.get(propertyName);
		if (listOfLabels != null) {
			listOfLabels = listOfLabels.replaceAll("^\\[|]$", "");
			return Arrays.stream(listOfLabels.split(",")).map(String::trim).collect(Collectors.toList());
		}
		return Collections.emptyList();
	}
}
