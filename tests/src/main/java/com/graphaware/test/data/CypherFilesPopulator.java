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

package com.graphaware.test.data;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * {@link com.graphaware.test.data.CypherPopulator} that reads Cypher statement groups from file located on paths provided
 * by {@link #files()}. Each file represents a single statement group and will thus be executed in a single transaction.
 */
public abstract class CypherFilesPopulator extends CypherPopulator {

    /**
     * {@inheritDoc}
     */
    @Override
    protected String[] statementGroups() {
        List<String> result = new LinkedList<>();

        try {
            String[] files = files();
            if (files == null) {
                return new String[0];
            }

            for (String file : files) {
                result.add(FileUtils.readFileToString(new File(file)));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return result.toArray(new String[result.size()]);
    }

    /**
     * @return paths to file with Cypher statements.
     */
    protected abstract String[] files() throws IOException;
}
