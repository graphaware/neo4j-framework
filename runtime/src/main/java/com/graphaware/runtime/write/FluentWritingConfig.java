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

package com.graphaware.runtime.write;

import com.graphaware.writer.neo4j.*;
import org.neo4j.graphdb.GraphDatabaseService;

/**
 * Simple implementation of {@link WritingConfig} with fluent interface.
 */
public class FluentWritingConfig implements WritingConfig {

    private final DatabaseWriterType writerType;
    private final int queueSize;
    private final int batchSize;

    /**
     * Create an instance of {@link FluentWritingConfig} with default configuration.
     *
     * @return instance.
     */
    public static FluentWritingConfig defaultConfiguration() {
        return new FluentWritingConfig(DatabaseWriterType.DEFAULT, SingleThreadedWriter.DEFAULT_QUEUE_CAPACITY, BatchWriter.DEFAULT_BATCH_SIZE);
    }

    /**
     * Return a new instance of this configuration with a different writer type.
     *
     * @param writerType of the new instance.
     * @return new instance.
     */
    public FluentWritingConfig withWriterType(DatabaseWriterType writerType) {
        return new FluentWritingConfig(writerType, queueSize, batchSize);
    }

    /**
     * Return a new instance of this configuration with a different queue size. Please note that queue size might not
     * be applicable to all {@link DatabaseWriterType}s (so might be ignored).
     *
     * @param queueSize of the new instance.
     * @return new instance.
     */
    public FluentWritingConfig withQueueSize(int queueSize) {
        return new FluentWritingConfig(writerType, queueSize, batchSize);
    }

    /**
     * Return a new instance of this configuration with a different batch size. Please note that batch size might not
     * be applicable to all {@link DatabaseWriterType}s (so might be ignored).
     *
     * @param batchSize of the new instance.
     * @return new instance.
     */
    public FluentWritingConfig withBatchSize(int batchSize) {
        return new FluentWritingConfig(writerType, queueSize, batchSize);
    }

    private FluentWritingConfig(DatabaseWriterType writerType, int queueSize, int batchSize) {
        this.writerType = writerType;
        this.queueSize = queueSize;
        this.batchSize = batchSize;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Neo4jWriter produceWriter(GraphDatabaseService database) {
        switch (writerType) {
            case DEFAULT:
                return new DefaultWriter(database);
            case SINGLE_THREADED:
                return new TxPerTaskWriter(database, queueSize);
            case BATCH:
                return new BatchWriter(database, queueSize, batchSize);
        }

        throw new IllegalStateException("Unknown writer type: " + writerType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FluentWritingConfig that = (FluentWritingConfig) o;

        if (batchSize != that.batchSize) return false;
        if (queueSize != that.queueSize) return false;
        if (writerType != that.writerType) return false;

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = writerType.hashCode();
        result = 31 * result + queueSize;
        result = 31 * result + batchSize;
        return result;
    }
}
