/*
 * Copyright (c) 2013-2020 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.example.module;

import com.graphaware.common.policy.inclusion.RelationshipInclusionPolicy;
import com.graphaware.runtime.config.FluentModuleConfiguration;
import com.graphaware.runtime.config.ModuleConfiguration;
import com.graphaware.runtime.module.BaseModule;
import com.graphaware.runtime.module.Module;
import com.graphaware.tx.event.improved.api.ImprovedTransactionData;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Relationship;

import static com.graphaware.example.module.Relationships.FRIEND_OF;

/**
 * {@link Module} that counts the total friendship strength in the database
 * and keeps it up to date.
 */
public class FriendshipStrengthModule extends BaseModule<Void> {

    private final ModuleConfiguration configuration;
    private final FriendshipStrengthCounter counter;

    public FriendshipStrengthModule(String moduleId, GraphDatabaseService database) {
        super(moduleId);
        this.counter = new FriendshipStrengthCounter();

        //only take into account relationships with FRIEND_OF type:
        configuration = FluentModuleConfiguration
                .defaultConfiguration()
                .with(
                        new RelationshipInclusionPolicy.Adapter() {
                            @Override
                            public boolean include(Relationship relationship) {
                                return relationship.isType(FRIEND_OF);
                            }
                        }
                );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ModuleConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Void beforeCommit(ImprovedTransactionData transactionData) {
        if (transactionData.mutationsOccurred()) {
            counter.handleCreatedFriendships(transactionData.getTransaction(), transactionData.getAllCreatedRelationships());
            counter.handleChangedFriendships(transactionData.getTransaction(), transactionData.getAllChangedRelationships());
            counter.handleDeletedFriendships(transactionData.getTransaction(), transactionData.getAllDeletedRelationships());
        }

        return null;
    }
}
