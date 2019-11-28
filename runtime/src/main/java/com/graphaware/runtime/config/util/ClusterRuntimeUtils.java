package com.graphaware.runtime.config.util;

import com.graphaware.common.log.LoggerFactory;
import com.graphaware.runtime.GraphAwareRuntime;
import org.neo4j.causalclustering.core.consensus.LeaderInfo;
import org.neo4j.causalclustering.core.consensus.LeaderListener;
import org.neo4j.causalclustering.core.consensus.NoLeaderFoundException;
import org.neo4j.causalclustering.core.consensus.RaftMachine;
import org.neo4j.graphdb.DependencyResolver;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.internal.GraphDatabaseAPI;
import org.neo4j.logging.Log;

public class ClusterRuntimeUtils implements LeaderListener {

    private static final Log LOG = LoggerFactory.getLogger(ClusterRuntimeUtils.class);
    private static final long CLUSTER_WAIT_TIME = 5 * 60 * 1000;

    private final GraphDatabaseService database;
    private final GraphAwareRuntime runtime;
    private final Runnable startAction;
    private final RaftMachine raftMachine;

    public ClusterRuntimeUtils(GraphDatabaseService database, GraphAwareRuntime runtime, Runnable startAction) {
        this.database = database;
        this.runtime = runtime;
        this.startAction = startAction;
        this.raftMachine = getRaftMachine();

        LOG.info("Register leader listener");
        raftMachine.registerListener(this);
        runtime.setClusterRuntimeUtils(this);
    }

    /**
     * In the case of a causal cluster, we have to wait the LEADER Member becomes writable. The states a LEADER Member will have during the start lifecycle are FOLLOWER, CANDIDATE, LEADER.
     *
     */
    public boolean waitClusterIsFormed() {
        boolean ready = getLeader();

        if (ready) {
            return true;
        }

        long end = System.currentTimeMillis() + CLUSTER_WAIT_TIME;
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
            raftMachine.getLeader();
        } catch (NoLeaderFoundException e) {
            return false;
        }

        return true;
    }

    private RaftMachine getRaftMachine() {
        DependencyResolver dependencyResolver = ((GraphDatabaseAPI) database).getDependencyResolver();

        return dependencyResolver.resolveDependency(RaftMachine.class);
    }


/*
    @Override
    public void onLeaderEvent(Outcome outcome) {
        LOG.info("Leader change event");
        waitClusterIsFormed()
    }
*/

    @Override
    public void onLeaderSwitch(LeaderInfo leaderInfo) {
        LOG.info("onLeaderSwitch");

        runtime.restart();

        new Thread(() -> {
            LOG.info("Waiting for leader to become available");
            boolean formed = waitClusterIsFormed();
            if (formed) {
                LOG.info("Leader available, starting the Runtime");
                startAction.run();
                LOG.info("GraphAware Runtime automatically started.");
            } else {
                LOG.error("Could not start GraphAware Runtime because the database didn't get to a usable state within 5 minutes.");
            }
        }).start();
    }

    @Override
    public void onLeaderStepDown(long stepDownTerm) {

    }

}
