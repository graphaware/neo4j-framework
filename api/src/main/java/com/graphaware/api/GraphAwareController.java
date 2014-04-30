package com.graphaware.api;

import org.springframework.stereotype.Controller;

import java.lang.annotation.*;

/**
 * Annotation that will cause a class to become a Spring {@link Controller} that will have a {@link org.neo4j.graphdb.GraphDatabaseService}
 * autowired. When using {@link org.springframework.web.bind.annotation.RequestMapping} annotations, the URLs provided
 * will be relative to http://your-app:port/graphaware/.
 * <p/>
 * In order for this mechanism to work, GraphAware Framework must be on the classpath (embedded mode) or in the Neo4j
 * server "plugins" directory (server mode).
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Controller
public @interface GraphAwareController {
}
