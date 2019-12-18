/*
 * Copyright (c) 2013-2019 GraphAware
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

package com.graphaware.common.ping;

import com.graphaware.common.log.LoggerFactory;
import com.graphaware.common.util.VersionReader;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.neo4j.ext.udc.UdcSettings;
import org.neo4j.graphdb.DependencyResolver.SelectionStrategy;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.io.os.OsBeanUtil;
import org.neo4j.kernel.configuration.Config;
import org.neo4j.kernel.impl.factory.Edition;
import org.neo4j.kernel.impl.factory.GraphDatabaseFacade;
import org.neo4j.kernel.impl.factory.OperationalMode;
import org.neo4j.kernel.impl.store.id.IdGeneratorFactory;
import org.neo4j.kernel.impl.store.id.IdType;
import org.neo4j.kernel.internal.GraphDatabaseAPI;
import org.neo4j.logging.Log;
import org.neo4j.udc.UsageData;
import org.neo4j.udc.UsageDataKeys;
import org.neo4j.util.concurrent.DecayingFlags;
import org.neo4j.util.concurrent.RecentK;

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
    private final String version;
    private UsageData usageData;
    private IdGeneratorFactory idGeneratorFactory;
    private Config config;

    public GoogleAnalyticsStatsCollector(GraphDatabaseService database) {
        if (database instanceof GraphDatabaseFacade) {
            GraphDatabaseFacade graphDatabaseFacade = (GraphDatabaseFacade) database;
            usageData = graphDatabaseFacade.getDependencyResolver().resolveDependency(UsageData.class, SelectionStrategy.FIRST);
            idGeneratorFactory = graphDatabaseFacade.getDependencyResolver().resolveDependency(IdGeneratorFactory.class, SelectionStrategy.FIRST);
        }

        this.database = database;
        this.version = VersionReader.getVersion();
    }

    public GoogleAnalyticsStatsCollector(GraphDatabaseService database, Config config) {
        this(database);
        this.config = config;
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
        String usageDataInfo = constructUsageDataInfo();
        String otherInfo = collectOtherInfo();
        return "v=1&tid=" + TID + "&cid=" + storeId + "&t=event&ea=" + appId + "&ec=Run&el=" + version
              + usageDataInfo + otherInfo;

    }

    private String constructUsageDataInfo() {
        if (usageData != null) {
            Edition edition = usageData.get(UsageDataKeys.edition);
            RecentK<String> clientNames = usageData.get(UsageDataKeys.clientNames);
            List<String> clientNamesList = Collections.emptyList();
            if (clientNames != null) {
                 clientNamesList = StreamSupport
                      .stream(clientNames.spliterator(), false)
                      .collect(Collectors.toList());
            }
            DecayingFlags features = usageData.get(UsageDataKeys.features);
            OperationalMode operationalMode = usageData.get(UsageDataKeys.operationalMode);
            String revision = usageData.get(UsageDataKeys.revision);
            String serverId = usageData.get(UsageDataKeys.serverId);
            String neo4jVersion = usageData.get(UsageDataKeys.version);

            return "&cd1=" + edition.name()
                  + "&cd2=" + operationalMode.name()
                  + "&cd3=" + revision
                  + "&cd4=" + serverId
                  + "&cd5=" + neo4jVersion
                  + "&cd6=" + clientNamesList.toString()
                  + "&cd7=" + (features == null ? "" : features.asHex());
        } else {
            return "";
        }
    }

    private String collectOtherInfo() {
        String udcSource = config.get(UdcSettings.udc_source);
        String udcRegistrationKey = config.get(UdcSettings.udc_registration_key);
        String os = System.getProperties().getProperty( "os.name");
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        long totalMemory = OsBeanUtil.getTotalPhysicalMemory();
        long heapSize = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
        long nodeIdsInUse = getNumberOfIdsInUse(IdType.NODE);
        long propertyIdsInUse = getNumberOfIdsInUse(IdType.PROPERTY);
        long relationshipIdsInUse = getNumberOfIdsInUse(IdType.RELATIONSHIP);
        long labelIdsInUse = getNumberOfIdsInUse(IdType.LABEL_TOKEN);

        return "&cd8=" + udcSource
              + "&cd9=" + udcRegistrationKey
              + "&cd10=" + os
              + "&cd11=" + availableProcessors
              + "&cd12=" + totalMemory
              + "&cd13=" + heapSize
              + "&cd14=" + nodeIdsInUse
              + "&cd15=" + propertyIdsInUse
              + "&cd16=" + relationshipIdsInUse
              + "&cd17=" + labelIdsInUse;
    }

    private long getNumberOfIdsInUse(IdType type) {
        return idGeneratorFactory.get(type).getNumberOfIdsInUse();
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
