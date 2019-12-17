package com.graphaware.runtime.bootstrap.cc;

import com.graphaware.common.log.LoggerFactory;
import com.graphaware.runtime.GraphAwareRuntime;
import org.neo4j.causalclustering.core.consensus.LeaderInfo;
import org.neo4j.causalclustering.core.consensus.LeaderListener;
import org.neo4j.causalclustering.core.consensus.RaftMachine;
import org.neo4j.causalclustering.discovery.TopologyService;
import org.neo4j.causalclustering.identity.MemberId;
import org.neo4j.graphdb.DependencyResolver;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.internal.GraphDatabaseAPI;
import org.neo4j.logging.Log;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClusterBootstrap implements LeaderListener {

    private static final Log LOG = LoggerFactory.getLogger(ClusterBootstrap.class);

    private final GraphDatabaseService database;
    private final GraphAwareRuntime runtime;
    private final ExecutorService initExecutor = Executors.newSingleThreadExecutor();

    public ClusterBootstrap(GraphDatabaseService database, GraphAwareRuntime runtime) {
        this.database = database;
        this.runtime = runtime;

        LOG.info("Registering LeaderListener");
        raftMachine().registerListener(this);
    }

    private RaftMachine raftMachine() {
        DependencyResolver dependencyResolver = ((GraphDatabaseAPI) database).getDependencyResolver();
        return dependencyResolver.resolveDependency(RaftMachine.class);
    }

    private TopologyService topologyService() {
        DependencyResolver dependencyResolver = ((GraphDatabaseAPI) database).getDependencyResolver();
        return dependencyResolver.resolveDependency(TopologyService.class);
    }

    @Override
    public void onLeaderSwitch(LeaderInfo leaderInfo) {
        final MemberId leaderId = leaderInfo.memberId();
        final MemberId thisMemberId = topologyService().myself();
        final boolean isSteppingDown = leaderInfo.isSteppingDown();
        LOG.info("onLeaderSwitch, leader = %s, isSteppingDown = %b this member = %s", leaderId, isSteppingDown, thisMemberId);

        if (Objects.equals(leaderId, thisMemberId) && !isSteppingDown) {
            LOG.info("Scheduling Runtime start");
            initExecutor.submit(runtime::start);
        }
    }

}
