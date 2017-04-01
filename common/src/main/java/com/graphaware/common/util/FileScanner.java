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

package com.graphaware.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Utility class that converts a various inputs (files, byte arrays,...) into a list of lines.
 */
public final class FileScanner {

    private FileScanner() {
    }

    /**
     * Produce lines from a file.
     *
     * @param file to produce lines from.
     * @param skip how many lines to skip.
     * @return lines.
     */
    public static List<String> produceLines(File file, int skip) {
        try {
            return produceLines(new FileInputStream(file),skip);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Produce lines from an input stream.
     *
     * @param inputStream to produce lines from.
     * @param skip        how many lines to skip.
     * @return lines.
     */
    public static List<String> produceLines(InputStream inputStream, int skip) {
        final List<String> lines = new ArrayList<String>();

        Scanner scanner = new Scanner(inputStream, "UTF-8");

        int skipped = 0;
        while (scanner.hasNextLine()) {
            String nextLine = scanner.nextLine();
            if (skip > skipped) {
                skipped++;
            } else {
                lines.add(nextLine);
            }
        }
        scanner.close();

        return lines;
    }
}
