/*
 * Copyright (c) 2013-2015 GraphAware
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

package com.graphaware.runtime.module.thirdparty;

import com.graphaware.common.representation.NodeRepresentation;
import com.graphaware.common.representation.RelationshipRepresentation;
import com.graphaware.common.util.Change;
import com.graphaware.runtime.module.BaseTxDrivenModule;
import com.graphaware.runtime.module.DeliberateTransactionRollbackException;
import com.graphaware.tx.event.improved.api.ImprovedTransactionData;
import com.graphaware.writer.thirdparty.*;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import java.util.Collection;
import java.util.HashSet;

/**
 * Base-class for GraphAware Runtime Modules that wish to implement integrations with third-party systems.
 * <p/>
 * Before committing, a collection of {@link WriteOperation}s is built, representing all the about-to-be-committed changes
 * in a way that allows it to be sent over the wire (no dependency on Neo4j APIs).
 * <p/>
 * The collection of {@link WriteOperation}s is passed into the {@link #afterCommit(Object)} method after the transaction
 * has successfully committed. The {@link #afterCommit(Object)} should be overridden by sub-classes.
 */
public abstract class ThirdPartyIntegrationModule extends BaseTxDrivenModule<Collection<WriteOperation<?>>> {

    /**
     * Construct a new module.
     *
     * @param moduleId ID of this module. Must not be <code>null</code> or empty.
     */
    protected ThirdPartyIntegrationModule(String moduleId) {
        super(moduleId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<WriteOperation<?>> beforeCommit(ImprovedTransactionData transactionData) throws DeliberateTransactionRollbackException {
        Collection<WriteOperation<?>> result = new HashSet<>();

        for (Node createdNode : transactionData.getAllCreatedNodes()) {
            result.add(new NodeCreated(new NodeRepresentation(createdNode)));
        }

        for (Change<Node> updatedNode : transactionData.getAllChangedNodes()) {
            result.add(new NodeUpdated(new NodeRepresentation(updatedNode.getPrevious()), new NodeRepresentation(updatedNode.getCurrent())));
        }

        for (Node deletedNode : transactionData.getAllDeletedNodes()) {
            result.add(new NodeDeleted(new NodeRepresentation(deletedNode)));
        }

        for (Relationship createdRelationship : transactionData.getAllCreatedRelationships()) {
            result.add(new RelationshipCreated(new RelationshipRepresentation(createdRelationship)));
        }

        for (Change<Relationship> updatedRelationship : transactionData.getAllChangedRelationships()) {
            result.add(new RelationshipUpdated(new RelationshipRepresentation(updatedRelationship.getPrevious()), new RelationshipRepresentation(updatedRelationship.getCurrent())));
        }

        for (Relationship deletedRelationship : transactionData.getAllDeletedRelationships()) {
            result.add(new RelationshipDeleted(new RelationshipRepresentation(deletedRelationship)));
        }

        return result;
    }
}
