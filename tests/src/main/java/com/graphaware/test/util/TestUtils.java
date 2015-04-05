/*
 * Copyright (c) 2014 GraphAware
 *
 * This file is part of GraphAware.
 *
 * GraphAware is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.test.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.charset.Charset;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Utilities mainly intended for testing.
 */
public final class TestUtils {

    private static final Logger LOG = LoggerFactory.getLogger(TestUtils.class);

    /**
     * Assert that two JSON objects represented as Strings are semantically equal.
     *
     * @param one one.
     * @param two two.
     */
    public static void assertJsonEquals(String one, String two) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            assertTrue(mapper.readTree(one).equals(mapper.readTree(two)));
        } catch (IOException e) {
            fail();
        }
    }

    /**
     * Convert a JSON file to String.
     *
     * @param fileName name of the file present in the root of the resources directory.
     * @return JSON as String.
     */
    public static String jsonAsString(String fileName) {
        return jsonAsString("", fileName);
    }

    /**
     * Convert a JSON file to String.
     *
     * @param caller   the class calling this method. The file is expected to be in the resources directory in the same
     *                 package as this class.
     * @param fileName name of the file present in the resources directory in the same package as the class above.
     * @return JSON as String.
     */
    public static String jsonAsString(Class caller, String fileName) {
        return jsonAsString(caller.getPackage().getName().replace(".", "/") + "/", fileName);
    }

    /**
     * Convert a JSON file to String.
     *
     * @param packagePath path to package. The file is expected to be in the resources directory in the same
     *                    package.
     * @param fileName    name of the file present in the resources directory in the package defined above.
     * @return JSON as String.
     */
    public static String jsonAsString(String packagePath, String fileName) {
        try {
            return IOUtils.toString(new ClassPathResource(packagePath + fileName + ".json").getInputStream(), Charset.forName("UTF-8"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get some available port.
     *
     * @return port number.
     */
    public static int getAvailablePort() {
        try {
            ServerSocket socket = new ServerSocket(0);
            try {
                return socket.getLocalPort();
            } finally {
                socket.close();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot find available port: " + e.getMessage(), e);
        }
    }

    /**
     * Measure the time of the timed callback.
     *
     * @param timed callback.
     * @return time in microseconds
     */
    public static long time(Timed timed) {
        long start = System.nanoTime();
        timed.time();
        return (System.nanoTime() - start) / 1000;
    }

    /**
     * Timed operation.
     */
    public interface Timed {

        /**
         * Perform the operation to be timed.
         */
        void time();
    }

    private TestUtils() {
    }
}
