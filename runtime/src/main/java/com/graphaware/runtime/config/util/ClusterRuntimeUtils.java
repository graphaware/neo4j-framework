package com.graphaware.runtime.config.util;

import com.graphaware.common.log.LoggerFactory;
import org.neo4j.causalclustering.core.consensus.NoLeaderFoundException;
import org.neo4j.causalclustering.core.consensus.RaftMachine;
import org.neo4j.graphdb.DependencyResolver;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.internal.GraphDatabaseAPI;
import org.neo4j.logging.Log;

public class ClusterRuntimeUtils {

    private final GraphDatabaseService database;
    private static final Log LOG = LoggerFactory.getLogger(ClusterRuntimeUtils.class);

    public ClusterRuntimeUtils(GraphDatabaseService database) {
        this.database = database;
    }

    public boolean waitClusterIsFormed(long timeout) {
        boolean ready = getLeader();

        if (ready) {
            return true;
        }

        long end = System.currentTimeMillis() + timeout;
        do {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.interrupted();
                break;
            }

            ready = getLeader();
        } while (!ready && System.currentTimeMillis() < end);

        return ready;
    }

    private boolean getLeader() {
        try {
            getRaftMachine().getLeader();

            return true;
        } catch (NoLeaderFoundException e) {
            LOG.debug("No leader found");
        }

        return false;
    }

    private RaftMachine getRaftMachine() {
        DependencyResolver dependencyResolver = ((GraphDatabaseAPI) database).getDependencyResolver();

        return dependencyResolver.resolveDependency(RaftMachine.class);
    }


}
