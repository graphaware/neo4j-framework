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

import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class FileScannerTest {

    @Test
    public void verifyCorrectNumberOfScannedLines() throws IOException {
        assertEquals(4, FileScanner.produceLines(FileScannerTest.class.getClassLoader().getResourceAsStream("scanner-test.csv"), 0).size());
    }

    @Test
    public void verifyCorrectlyScannedLines() throws IOException {
        List<String> lines = FileScanner.produceLines(FileScannerTest.class.getClassLoader().getResourceAsStream("scanner-test.csv"),1);

        assertEquals(3, lines.size());
        assertEquals("line1;bla", lines.get(0));
        assertEquals("line2;bla", lines.get(1));
        assertEquals("line3;bla", lines.get(2));
    }
}
