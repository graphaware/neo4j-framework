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
package com.graphaware.common.uuid;

import org.junit.Test;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertFalse;

public class UuidGeneratorTest {

    @Test
    public void shouldBeNoDuplicatesWithConcurrentAccess() throws InterruptedException {
        final Set<String> uuids = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
        final UuidGenerator generator = new EaioUuidGenerator();
        final AtomicBoolean failure = new AtomicBoolean(false);
        ExecutorService executor = Executors.newFixedThreadPool(100);
        for (int i = 0; i < 1000; i++) {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    String uuid = generator.generateUuid();
                    if (!uuids.add(uuid)) {
                        failure.set(true);
                    }
                }
            });

        }
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        assertFalse("Duplicate UUID Generated", failure.get());
    }
}
