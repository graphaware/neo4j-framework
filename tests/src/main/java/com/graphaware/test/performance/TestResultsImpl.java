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

package com.graphaware.test.performance;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Default implementation of {@link TestResults}.
 */
public class TestResultsImpl implements TestResults {

    private final Map<String, String> results = new LinkedHashMap<>();
    private String firstLine = "";

    /**
     * {@inheritDoc}
     */
    @Override
    public void acceptResult(Map<String, Object> params, long result) {
        if (results.isEmpty()) {
            StringBuilder firstLine = new StringBuilder();
            for (String paramName : params.keySet()) {
                firstLine.append(paramName).append(";");
            }
            firstLine.append("times in microseconds...");
            this.firstLine = firstLine.toString();
        }

        StringBuilder linePrefix = new StringBuilder();
        for (Object value : params.values()) {
            linePrefix.append(value.toString()).append(";");
        }

        if (!results.containsKey(linePrefix.toString())) {
            results.put(linePrefix.toString(), "");
        }

        results.put(linePrefix.toString(), results.get(linePrefix.toString()) + result + ";");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void printToFile(String title, String fileName) {
        try {
            File file = new File(fileName);
            file.createNewFile();

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);

            bw.write(title);
            bw.newLine();
            bw.newLine();
            bw.write(firstLine);
            bw.newLine();

            for (Map.Entry<String, String> entry : results.entrySet()) {
                bw.write(entry.getKey() + entry.getValue().substring(0, entry.getValue().length() - 1));   //remove last semicolon
                bw.newLine();
            }

            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
