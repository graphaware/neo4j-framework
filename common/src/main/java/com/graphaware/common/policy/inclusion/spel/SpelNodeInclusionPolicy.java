/*
 * Copyright (c) 2013-2017 GraphAware
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

package com.graphaware.common.policy.inclusion.spel;

import com.graphaware.common.expression.AttachedNodeExpressions;
import com.graphaware.common.policy.inclusion.NodeInclusionPolicy;
import com.graphaware.common.representation.AttachedNode;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.helpers.collection.FilteringIterable;

/**
 * {@link NodeInclusionPolicy} based on a SPEL expression. The expression can use methods defined in {@link AttachedNodeExpressions}.
 */
public class SpelNodeInclusionPolicy extends SpelInclusionPolicy implements NodeInclusionPolicy {

    public SpelNodeInclusionPolicy(String expression) {
        super(expression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean include(Node node) {
        return (Boolean) exp.getValue(new AttachedNode(node));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<Node> getAll(GraphDatabaseService database) {
        //In simple cases, we can fetch nodes using more efficient native syntax
        if(expressionNode.toStringAST().startsWith("hasLabel")) {
            String labelName = stripWrappingQuotes(expressionNode.getChild(0).toStringAST());
            return () -> database.findNodes(Label.label(labelName));
        }


        return new FilteringIterable<>(database.getAllNodes(), this::include);
    }

    private String stripWrappingQuotes(String s) {
        if(s.startsWith("\"") || s.startsWith("\'")) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }
}
