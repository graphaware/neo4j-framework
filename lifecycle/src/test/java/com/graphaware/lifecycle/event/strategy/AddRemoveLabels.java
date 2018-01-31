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

import java.util.*;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;


public final class AddRemoveLabels implements LifecycleEventStrategy<Node> {

	public static final String EXPIRY_CONFIG_KEY = "labels-expire";
	public static final String REVIVAL_CONFIG_KEY = "labels-revive";

	private List<String> labelsToAdd;
	private List<String> labelsToRemove;

	public AddRemoveLabels(List<String> labelsToAdd, List<String> labelsToRemove) {
		this.labelsToAdd = labelsToAdd;
		this.labelsToRemove = labelsToRemove;
	}

	@Override
	public boolean applyIfNeeded(Node entity) {
		for (String label : this.labelsToRemove) {
			entity.removeLabel(Label.label(label));
		}
		for (String label : this.labelsToAdd) {
			entity.addLabel(Label.label(label));
		}
		return true;
	}

}
