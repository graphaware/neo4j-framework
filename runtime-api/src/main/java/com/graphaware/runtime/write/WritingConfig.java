package com.graphaware.runtime.write;

import com.graphaware.writer.DatabaseWriter;
import org.neo4j.graphdb.GraphDatabaseService;

/**
 * A configuration of {@link DatabaseWriter}s for the purposes of the framework.
 */
public interface WritingConfig {

    /**
     * Produce a database writer configured by this configuration, setup to write to the given database.
     *
     * @param database that the writer will write to.
     * @return writer.
     */
    DatabaseWriter produceWriter(GraphDatabaseService database);
}
