package com.graphaware.server.foundation.context;

import com.graphaware.server.foundation.config.NeoMvcConfig;
import org.neo4j.kernel.configuration.Config;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

public class NeoWebContextCreator extends BaseWebContextCreator {

    @Override
    protected void registerConfigClasses(AnnotationConfigWebApplicationContext context, Config config) {
        context.register(NeoMvcConfig.class);
    }
}
