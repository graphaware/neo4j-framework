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

package com.graphaware.common.ping;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * {@link StatsCollector} that collects stats using Google Analytics.
 */
public class GoogleAnalyticsStatsCollector implements StatsCollector {

    private static final Logger LOG = LoggerFactory.getLogger(GoogleAnalyticsStatsCollector.class);
    private static final UUID uuid = UUID.randomUUID();
    private static final String TID = "UA-1428985-8";
    private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private static final GoogleAnalyticsStatsCollector INSTANCE = new GoogleAnalyticsStatsCollector();

    public static GoogleAnalyticsStatsCollector getInstance() {
        return INSTANCE;
    }

    private GoogleAnalyticsStatsCollector() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                executor.shutdownNow();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void frameworkStart(String edition) {
        post(constructBody("framework-" + edition));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void runtimeStart() {
        post(constructBody("runtime"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void moduleStart(String moduleClassName) {
        post(constructBody(moduleClassName));
    }

    private void post(final String body) {
        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                HttpPost httpPost = new HttpPost("http://www.google-analytics.com/collect");
                httpPost.setEntity(new StringEntity(body, ContentType.TEXT_PLAIN));

                try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                    httpClient.execute(httpPost);
                } catch (IOException e1) {
                    LOG.warn("Unable to collect stats", e1);
                }
            }
        }, 5, 5, TimeUnit.MINUTES);
    }

    private String constructBody(String appId) {
        return "v=1&tid=" + TID + "&cid=" + uuid + "&t=event&ea=" + appId + "&ec=Run&el=" + VERSION;
    }
}
