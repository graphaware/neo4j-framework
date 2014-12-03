/*
 * Copyright (c) 2013 GraphAware
 *
 * This file is part of GraphAware.
 *
 * GraphAware is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.server.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring transactions-related application config.
 */
@Configuration
public class TxConfig {

    @Autowired(required = false)
    private GraphDatabaseAPI databaseAPI;

    @Autowired (required = false)
    private GraphDatabaseService database;

    @Bean
    public PlatformTransactionManager transactionManager() {
        if (databaseAPI != null) {
            return new JtaTransactionManager(new UserTransactionImpl(databaseAPI), new SpringTransactionManager(databaseAPI));
        }

        if (database != null) {
            return new JtaTransactionManager(new UserTransactionImpl((GraphDatabaseAPI) database), new SpringTransactionManager((GraphDatabaseAPI) database));
        }

        throw new IllegalStateException("Neither GraphDatabaseAPI nor GraphDatabaseService are present");
    }
}
