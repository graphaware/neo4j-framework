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

package com.graphaware.server.tx;

import org.neo4j.logging.Log;
import org.neo4j.server.rest.transactional.GraphAwareLongRunningTransaction;
import org.neo4j.server.rest.transactional.TransactionFacade;
import org.neo4j.server.rest.transactional.TransactionHandle;
import org.neo4j.server.rest.transactional.error.Neo4jError;
import org.neo4j.server.rest.transactional.error.TransactionLifecycleException;
import com.graphaware.common.log.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * A {@link javax.servlet.Filter} that will resume/suspend {@link com.graphaware.server.tx.LongRunningTransaction}s, so
 * that GraphAware Modules can participate in them. The transactions have to be started and committed/rolled back from
 * Cypher.
 * <p/>
 * Transaction ID has to be provided as a request header in order for Modules to be able to participate in the transactions.
 * The name of the header must be {@link #TX_HEADER} ("_GA_TX_ID").
 */
public class LongRunningTransactionFilter implements Filter {

    private static final Log LOG = LoggerFactory.getLogger(LongRunningTransactionFilter.class);
    private static final String TX_HEADER = "_GA_TX_ID";

    private final TransactionFacade transactionFacade;

    public LongRunningTransactionFilter(TransactionFacade transactionFacade) {
        this.transactionFacade = transactionFacade;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        LOG.debug("Initializing " + LongRunningTransaction.class.getName());
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        LongRunningTransaction longRunningTransaction = null;

        String txId = ((HttpServletRequest) request).getHeader(TX_HEADER);
        if (txId != null) {
            LOG.debug("Trying to participate in a long-running transaction ID: " + txId);
            try {
                long txIdAsLong = Long.parseLong(txId);
                TransactionHandle transactionHandle = transactionFacade.findTransactionHandle(txIdAsLong);
                longRunningTransaction = new GraphAwareLongRunningTransaction(transactionHandle);
                longRunningTransaction.resume();
                LOG.debug("Transaction " + txId + " resumed.");
            } catch (TransactionLifecycleException e) {
                LOG.warn("Transaction " + txId + " could not be resumed.", e);

                Neo4jError error = e.toNeo4jError();
                response.reset();
                ((HttpServletResponse) response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().append(error.getMessage());
                response.getWriter().append(error.getStackTraceAsString());
                return;
            } catch (RuntimeException e) {
                LOG.warn("Transaction " + txId + " could not be resumed.", e);

                response.reset();
                ((HttpServletResponse) response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().append(e.getMessage());
                return;
            }
        }

        chain.doFilter(request, response);

        if (longRunningTransaction != null) {
            LOG.debug("Suspending transaction " + txId);
            longRunningTransaction.suspend();
        }
    }

    @Override
    public void destroy() {
        LOG.debug("Destroying " + LongRunningTransaction.class.getName());
    }
}
