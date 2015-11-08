package com.graphaware.server.foundation.context;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.neo4j.kernel.configuration.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

public class GraphAwareWebContextCreator extends BaseWebContextCreator {

    private static final Logger LOG = LoggerFactory.getLogger(GraphAwareWebContextCreator.class);

    private static final String GA_API_PACKAGE_SCAN_SETTING = "com.graphaware.server.api.scan";
    private static final String[] GA_API_PACKAGE_SCAN_DEFAULT = new String[]{"com.**.graphaware.**", "org.**.graphaware.**", "net.**.graphaware.**"};

    @Override
    protected void registerConfigClasses(AnnotationConfigWebApplicationContext context, Config config) {
        context.scan(getPackagesToScan(config));
    }

    private String[] getPackagesToScan(Config config) {
        if (config.getParams().containsKey(GA_API_PACKAGE_SCAN_SETTING)) {
            String packageExpression = config.getParams().get(GA_API_PACKAGE_SCAN_SETTING);
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
