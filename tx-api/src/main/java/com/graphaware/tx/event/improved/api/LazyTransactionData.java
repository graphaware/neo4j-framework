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

package com.graphaware.tx.event.improved.api;

import com.graphaware.tx.event.improved.data.BaseImprovedTransactionData;
import com.graphaware.tx.event.improved.data.NodeTransactionData;
import com.graphaware.tx.event.improved.data.RelationshipTransactionData;
import com.graphaware.tx.event.improved.data.TransactionDataContainer;
import com.graphaware.tx.event.improved.data.lazy.LazyNodeTransactionData;
import com.graphaware.tx.event.improved.data.lazy.LazyRelationshipTransactionData;
import org.neo4j.graphdb.event.TransactionData;

/**
 * {@link ImprovedTransactionData} delegating all work to {@link com.graphaware.tx.event.improved.data.lazy.LazyNodeTransactionData}
 * and {@link com.graphaware.tx.event.improved.data.lazy.LazyRelationshipTransactionData}.
 */
public class LazyTransactionData extends BaseImprovedTransactionData implements ImprovedTransactionData, TransactionDataContainer {

    private final NodeTransactionData nodeTransactionData;
    private final RelationshipTransactionData relationshipTransactionData;

    /**
     * Create an instance from Neo4j {@link org.neo4j.graphdb.event.TransactionData}.
     *
     * @param transactionData data about the transaction.
     */
    public LazyTransactionData(TransactionData transactionData) {
        super(transactionData);
        nodeTransactionData = new LazyNodeTransactionData(transactionData, this);
        relationshipTransactionData = new LazyRelationshipTransactionData(transactionData, this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeTransactionData getNodeTransactionData() {
        return nodeTransactionData;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RelationshipTransactionData getRelationshipTransactionData() {
        return relationshipTransactionData;
    }
}
