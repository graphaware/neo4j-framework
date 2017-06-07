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

package com.graphaware.server.foundation.context;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.neo4j.configuration.ConfigValue;
import org.neo4j.kernel.configuration.Config;
import org.neo4j.logging.Log;
import com.graphaware.common.log.LoggerFactory;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

public class GraphAwareWebContextCreator extends BaseWebContextCreator {

    private static final Log LOG = LoggerFactory.getLogger(GraphAwareWebContextCreator.class);

    private static final String GA_API_PACKAGE_SCAN_SETTING = "com.graphaware.server.api.scan";
    private static final String GA_API_STATS_DISABLE_SETTING_LEGACY = "com.graphaware.server.stats.disable"; //see https://github.com/graphaware/neo4j-framework/issues/59
    private static final String GA_API_STATS_DISABLE_SETTING = "com.graphaware.server.stats.disabled";

    private static final String[] GA_API_PACKAGE_SCAN_DEFAULT = new String[]{"com.**.graphaware.**", "org.**.graphaware.**", "net.**.graphaware.**"};
    private static final String GA_STATS_PACKAGE = "com.graphaware.server.foundation.stats";

    @Override
    protected void registerConfigClasses(AnnotationConfigWebApplicationContext context, Config config) {
        configureStatsCollector(context, config);
        context.scan(addStatsPackage(getPackagesToScan(config)));
    }

    private String[] addStatsPackage(String[] packagesToScan) {
        packagesToScan = ArrayUtils.add(packagesToScan, GA_STATS_PACKAGE);
        return packagesToScan;
    }

    private void configureStatsCollector(AnnotationConfigWebApplicationContext context, Config config) {
        if (Boolean.valueOf(config.getRaw().getOrDefault(GA_API_STATS_DISABLE_SETTING_LEGACY, "false"))) {
            context.getEnvironment().addActiveProfile("stats-null");
        }
        else if (Boolean.valueOf(config.getRaw().getOrDefault(GA_API_STATS_DISABLE_SETTING, "false"))) {
            context.getEnvironment().addActiveProfile("stats-null");
        }
        else {
            context.getEnvironment().addActiveProfile("stats-google");
        }
    }

    private String[] getPackagesToScan(Config config) {
        if (config.getRaw().containsKey(GA_API_PACKAGE_SCAN_SETTING)) {
            String packageExpression = config.getRaw().get(GA_API_PACKAGE_SCAN_SETTING);
            if (StringUtils.isNotBlank(packageExpression)) {
                LOG.info("Will try to scan the following packages: " + packageExpression);
                return packageExpression.split(",");
            } else {
                LOG.warn("Invalid expression for packages to scan, will use default...");
            }
        }

        LOG.info("Will try to scan the following packages: " + ArrayUtils.toString(GA_API_PACKAGE_SCAN_DEFAULT));
        return GA_API_PACKAGE_SCAN_DEFAULT;
    }
}
