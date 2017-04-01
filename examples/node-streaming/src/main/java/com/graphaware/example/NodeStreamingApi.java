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

package com.graphaware.example;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphaware.api.json.LongIdJsonNode;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Sample REST API for streaming out all nodes in the database.
 */
@Controller
@RequestMapping("stream")
public class NodeStreamingApi {

    private static final JsonFactory JSON_FACTORY = new JsonFactory(new ObjectMapper());

    private final GraphDatabaseService database;
    private final AsyncTaskExecutor taskExecutor;

    @Autowired
    public NodeStreamingApi(GraphDatabaseService database, AsyncTaskExecutor taskExecutor) {
        this.database = database;
        this.taskExecutor = taskExecutor;
    }

    @RequestMapping(path = "v1", method = RequestMethod.GET)
    public ResponseBodyEmitter streamV1() {
        final ResponseBodyEmitter emitter = new ResponseBodyEmitter();

        taskExecutor.execute(new Runnable() {
            @Override
            public void run() {
                boolean first = true;

                try (Transaction tx = database.beginTx()) {
                    emitter.send("[");

                    for (Node node : database.getAllNodes()) {
                        if (!first) {
                            emitter.send(",");
                        } else {
                            first = false;
                        }
                        emitter.send(new LongIdJsonNode(node));
                    }

                    emitter.send("]");
                    emitter.complete();

                    tx.success();
                } catch (Exception e) {
                    emitter.completeWithError(e);
                }
            }
        });


        return emitter;
    }

    @RequestMapping(path = "v2", method = RequestMethod.GET)
    public StreamingResponseBody streamV2() {

        return new StreamingResponseBody() {
            @Override
            public void writeTo(final OutputStream outputStream) throws IOException {
                JsonGenerator jsonGenerator = JSON_FACTORY.createGenerator(outputStream);
                jsonGenerator.writeStartArray();

                try (Transaction tx = database.beginTx()) {
                    for (Node node : database.getAllNodes()) {
                        jsonGenerator.writeObject(new LongIdJsonNode(node));
                    }
                    tx.success();
                }

                jsonGenerator.writeEndArray();
                jsonGenerator.close();
            }
        };
    }
}