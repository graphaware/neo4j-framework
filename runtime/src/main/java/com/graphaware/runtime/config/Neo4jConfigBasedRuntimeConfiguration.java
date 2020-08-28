/*
 * Copyright (c) 2013-2020 GraphAware
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

package com.graphaware.runtime.config;

import com.graphaware.runtime.config.function.StringToDatabaseWriterType;
import com.graphaware.runtime.write.DatabaseWriterType;
import com.graphaware.runtime.write.FluentWritingConfig;
import com.graphaware.runtime.write.WritingConfig;
import com.graphaware.writer.neo4j.BatchWriter;
import com.graphaware.writer.neo4j.DefaultWriter;
import com.graphaware.writer.neo4j.TxPerTaskWriter;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.config.Setting;
import org.neo4j.kernel.configuration.Config;

import static org.neo4j.kernel.configuration.Settings.*;

/**
 * Implementation of {@link RuntimeConfiguration} that loads bespoke settings from Neo4j's configuration properties, falling
 * back to default values when overrides aren't available. Intended for internal framework use, mainly for server deployments.
 * <p>
 * There are two main things configured using this mechanism: the {@link DatabaseWriterType} and the {@link StatsCollector}.
 * <p>
 * For {@link WritingConfig}, there are three choices:
 * <pre>
 *     com.graphaware.runtime.db.writer=default
 * </pre>
 * results in a {@link DefaultWriter} being constructed.
 * <p>
 * <pre>
 *     com.graphaware.runtime.db.writer=single
 *     #optional queue size, defaults to 10,000
 *     com.graphaware.runtime.db.writer.queueSize=10000
 * </pre>
 * results in a {@link TxPerTaskWriter} being constructed with the configured queue size
 * <p>
 * <pre>
 *     com.graphaware.runtime.db.writer=batch
 *     #optional queue size, defaults to 10,000
 *     com.graphaware.runtime.db.writer.queueSize=10000
 *     #optional batch size, defaults to 1,000
 *     com.graphaware.runtime.db.writer.batchSize=1000
 * </pre>
 * results in a {@link BatchWriter} being constructed with the configured queue and batch sizes.
 */
public final class Neo4jConfigBasedRuntimeConfiguration extends BaseRuntimeConfiguration {

    //writer
    private static final Setting<DatabaseWriterType> DATABASE_WRITER_TYPE_SETTING = setting("com.graphaware.runtime.db.writer", StringToDatabaseWriterType.getInstance(), (String) null);
    private static final Setting<Integer> WRITER_QUEUE_SIZE = setting("com.graphaware.runtime.db.writer.queueSize", INTEGER, (String) null);
    private static final Setting<Integer> WRITER_BATCH_SIZE = setting("com.graphaware.runtime.db.writer.batchSize", INTEGER, (String) null);

    /**
     * Constructs a new {@link Neo4jConfigBasedRuntimeConfiguration} based on the given Neo4j {@link Config}.
     *
     * @param config The {@link Config} containing the settings used to configure the runtime
     */
    public Neo4jConfigBasedRuntimeConfiguration(GraphDatabaseService database, Config config) {
        super(config, createWritingConfig(config));
    }

    private static WritingConfig createWritingConfig(Config config) {
        DatabaseWriterType databaseWriterType = config.get(DATABASE_WRITER_TYPE_SETTING);

        FluentWritingConfig result = FluentWritingConfig.defaultConfiguration();

        if (databaseWriterType != null) {
            result = result.withWriterType(databaseWriterType);
        }

        if (config.get(WRITER_QUEUE_SIZE) != null) {
            result = result.withQueueSize(config.get(WRITER_QUEUE_SIZE));
        }

        if (config.get(WRITER_BATCH_SIZE) != null) {
            result = result.withBatchSize(config.get(WRITER_BATCH_SIZE));
        }

        return result;
    }
}
