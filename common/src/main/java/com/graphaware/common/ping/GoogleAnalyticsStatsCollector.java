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

package com.graphaware.common.ping;

import com.graphaware.common.log.LoggerFactory;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.internal.GraphDatabaseAPI;
import org.neo4j.logging.Log;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * {@link StatsCollector} that collects stats using Google Analytics.
 */
public class GoogleAnalyticsStatsCollector implements StatsCollector {

    private static final Log LOG = LoggerFactory.getLogger(GoogleAnalyticsStatsCollector.class);
    private static final String TID = "UA-1428985-11";
    private static final String UNKNOWN = "unknown";

    private final GraphDatabaseService database;
    private static ScheduledExecutorService executor;
    private String storeId = UNKNOWN;

    public GoogleAnalyticsStatsCollector(GraphDatabaseService database) {
        this.database = database;
    }

    private String findStoreIdIfNeeded() {
        if (UNKNOWN.equals(storeId)) {
            try {
                storeId = findStoreId();
            } catch (Exception e) {
                //do nothing
            }
        }

        return storeId;
    }

    private String findStoreId() {
        if (database == null) {
            return UNKNOWN;
        }

        if (((GraphDatabaseAPI) database).storeId() == null) {
            return UNKNOWN;
        }

        return String.valueOf(((GraphDatabaseAPI) database).storeId().getRandomId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void frameworkStart(String edition) {
        post("framework-" + edition);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void runtimeStart() {
        post("runtime");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void moduleStart(String moduleClassName) {
        post(moduleClassName);
    }

    private void post(final String appId) {
        initializeExecutorIfNeeded();

        executor.scheduleAtFixedRate(() -> {
            HttpPost httpPost = new HttpPost("http://www.google-analytics.com/collect");
            httpPost.setEntity(new StringEntity(constructBody(findStoreIdIfNeeded(), appId), ContentType.TEXT_PLAIN));

            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                httpClient.execute(httpPost);
            } catch (IOException e1) {
                //LOG.warn("Unable to collect stats", e1);
            }
        }, 5, 5, TimeUnit.MINUTES);
    }

    private void initializeExecutorIfNeeded() {
        if (executor == null) {
            synchronized (this) {
                if (executor == null) {
                    executor = Executors.newSingleThreadScheduledExecutor();

                    Runtime.getRuntime().addShutdownHook(new Thread() {
                        @Override
                        public void run() {
                            executor.shutdownNow();
                        }
                    });
                }
            }
        }
    }

    private String constructBody(String storeId, String appId) {
        return "v=1&tid=" + TID + "&cid=" + storeId + "&t=event&ea=" + appId + "&ec=Run&el=" + VERSION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        GoogleAnalyticsStatsCollector that = (GoogleAnalyticsStatsCollector) o;

        return !(storeId != null ? !storeId.equals(that.storeId) : that.storeId != null);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return storeId != null ? storeId.hashCode() : 0;
    }
}
