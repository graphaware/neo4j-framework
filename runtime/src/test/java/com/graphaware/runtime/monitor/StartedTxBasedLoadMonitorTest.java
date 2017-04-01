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

package com.graphaware.runtime.monitor;

import com.graphaware.runtime.schedule.TimingStrategy;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.backup.OnlineBackupSettings;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.shell.ShellSettings;
import org.neo4j.test.TestGraphDatabaseFactory;

import static com.graphaware.common.util.DatabaseUtils.registerShutdownHook;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.neo4j.kernel.configuration.Settings.FALSE;

/**
 * Integration test for {@link StartedTxBasedLoadMonitor}.
 */
public class StartedTxBasedLoadMonitorTest {

    private GraphDatabaseService database;
    private DatabaseLoadMonitor loadMonitor;

    @Before
    public void setUp() {
        database = new TestGraphDatabaseFactory()
                .newImpermanentDatabaseBuilder()
                .setConfig(OnlineBackupSettings.online_backup_enabled, FALSE)
                .setConfig(ShellSettings.remote_shell_enabled, FALSE)
                .newGraphDatabase();

        registerShutdownHook(database);

        loadMonitor = new StartedTxBasedLoadMonitor(database, new RunningWindowAverage(200, 2000));
    }

    @After
    public void tearDown() {
        database.shutdown();
    }

    @Test
    public void readTransactionsShouldBeMonitored() throws InterruptedException {
        assertEquals(TimingStrategy.UNKNOWN, loadMonitor.getLoad());

        for (int i = 0; i < 10; i++) {
            try (Transaction tx = database.beginTx()) {
                //do nothing
                tx.success();
            }

            Thread.sleep(1);
            assertTrue(loadMonitor.getLoad() > 0);
        }

        assertTrue(loadMonitor.getLoad() > 0);
    }
}
