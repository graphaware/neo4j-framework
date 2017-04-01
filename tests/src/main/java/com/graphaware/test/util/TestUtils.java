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

package com.graphaware.test.util;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.charset.Charset;

/**
 * Utilities mainly intended for testing.
 */
public final class TestUtils {

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
     * Wait for a specified number of ms.
     *
     * @param ms how long to wait.
     */
    public static void waitFor(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
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
