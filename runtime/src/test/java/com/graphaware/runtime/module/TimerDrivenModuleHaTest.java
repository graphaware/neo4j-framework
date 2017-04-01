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

package com.graphaware.runtime.module;

import com.graphaware.common.policy.role.AnyRole;
import com.graphaware.common.policy.role.InstanceRolePolicy;
import com.graphaware.common.policy.role.MasterOnly;
import com.graphaware.common.policy.role.SlavesOnly;
import com.graphaware.runtime.GraphAwareRuntime;
import com.graphaware.runtime.GraphAwareRuntimeFactory;
import com.graphaware.runtime.config.FluentRuntimeConfiguration;
import com.graphaware.runtime.config.FluentTimerDrivenModuleConfiguration;
import com.graphaware.runtime.config.NullTimerDrivenModuleConfiguration;
import com.graphaware.runtime.schedule.FixedDelayTimingStrategy;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.backup.OnlineBackupSettings;
import org.neo4j.cluster.ClusterSettings;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.HighlyAvailableGraphDatabaseFactory;
import org.neo4j.kernel.ha.HaSettings;
import org.neo4j.shell.ShellSettings;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.io.File;
import java.util.concurrent.*;

import static com.graphaware.common.util.DatabaseUtils.registerShutdownHook;
import static com.graphaware.test.util.TestUtils.waitFor;
import static org.junit.Assert.assertTrue;
import static org.neo4j.kernel.configuration.Settings.FALSE;

public class TimerDrivenModuleHaTest {

    private ExecutorService executor;

    @Before
    public void setUp() {
        executor = Executors.newFixedThreadPool(5);
    }

    @After
    public void tearDown() {
        executor.shutdownNow();
    }

    @Test
    public void shouldRespectSettingsOnMaster() throws InterruptedException, ExecutionException {
        int[] runMasterOnly = run(MasterOnly.getInstance());
        int[] runSlavesOnly = run(SlavesOnly.getInstance());
        int[] runAny = run(AnyRole.getInstance());

        assertTrue(runMasterOnly[0] > 10);
        assertTrue(runMasterOnly[1] == 0);
        assertTrue(runMasterOnly[2] == 0);

        assertTrue(runSlavesOnly[0] == 0);
        assertTrue(runSlavesOnly[1] > 10);
        assertTrue(runSlavesOnly[2] > 10);

        assertTrue(runAny[0] > 10);
        assertTrue(runAny[1] > 10);
        assertTrue(runAny[2] > 10);

    }

    @Test
    public void shouldRunInSingleMode() {
        GraphDatabaseService database = new TestGraphDatabaseFactory()
                .newImpermanentDatabaseBuilder()
                .setConfig(OnlineBackupSettings.online_backup_enabled, FALSE)
                .setConfig(ShellSettings.remote_shell_enabled, FALSE)
                .newGraphDatabase();

        registerShutdownHook(database);

        GraphAwareRuntime runtime = GraphAwareRuntimeFactory.createRuntime(database,
                FluentRuntimeConfiguration
                        .defaultConfiguration(database)
                        .withTimingStrategy(FixedDelayTimingStrategy.getInstance().withInitialDelay(0).withDelay(10)));

        RunCountingTimerDrivenModule module = new RunCountingTimerDrivenModule(NullTimerDrivenModuleConfiguration.getInstance());
        runtime.registerModule(module);
        runtime.start();

        waitFor(300);

        assertTrue(module.getRuns() > 10);

        database.shutdown();
    }

    private int[] run(InstanceRolePolicy instanceRolePolicy) throws InterruptedException, ExecutionException {
        Future<GraphDatabaseService> masterFuture = executor.submit(() -> haDb("1", false));
        Future<GraphDatabaseService> slave1Future = executor.submit(() -> haDb("2", true));
        Future<GraphDatabaseService> slave2Future = executor.submit(() -> haDb("3", true));

        GraphDatabaseService master = masterFuture.get();
        GraphDatabaseService slave1 = slave1Future.get();
        GraphDatabaseService slave2 = slave2Future.get();

        RunCountingTimerDrivenModule moduleMaster = startFramework(master, instanceRolePolicy);
        RunCountingTimerDrivenModule moduleSlave1 = startFramework(slave1, instanceRolePolicy);
        RunCountingTimerDrivenModule moduleSlave2 = startFramework(slave2, instanceRolePolicy);

        waitFor(300);

        master.shutdown();
        slave1.shutdown();
        slave2.shutdown();

        return new int[]{moduleMaster.getRuns(), moduleSlave1.getRuns(), moduleSlave2.getRuns()};
    }

    private RunCountingTimerDrivenModule startFramework(GraphDatabaseService database, InstanceRolePolicy instanceRolePolicy) {
        GraphAwareRuntime runtime = GraphAwareRuntimeFactory.createRuntime(database,
                FluentRuntimeConfiguration
                        .defaultConfiguration(database)
                        .withTimingStrategy(FixedDelayTimingStrategy.getInstance().withInitialDelay(0).withDelay(10)));

        RunCountingTimerDrivenModule module = new RunCountingTimerDrivenModule(FluentTimerDrivenModuleConfiguration.defaultConfiguration().with(instanceRolePolicy));
        runtime.registerModule(module);
        runtime.start();
        return module;
    }

    private GraphDatabaseService haDb(String id, boolean slave) throws InterruptedException {
        GraphDatabaseService database = new HighlyAvailableGraphDatabaseFactory()
                .newEmbeddedDatabaseBuilder(new File("target/data/" + id + "/" + System.currentTimeMillis()))
                .setConfig(OnlineBackupSettings.online_backup_enabled, FALSE)
                .setConfig(ShellSettings.remote_shell_enabled, FALSE)
                .setConfig(ClusterSettings.server_id, id)
                .setConfig(HaSettings.ha_server, "localhost:600" + id)
                .setConfig(HaSettings.slave_only, Boolean.toString(slave))
                .setConfig(ClusterSettings.cluster_server, "localhost:510" + id)
                .setConfig(ClusterSettings.initial_hosts, "localhost:5101,localhost:5102,localhost:5103")
                .newGraphDatabase();

        registerShutdownHook(database);

        return database;
    }
}
