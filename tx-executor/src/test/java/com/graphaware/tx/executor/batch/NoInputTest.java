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

package com.graphaware.tx.executor.batch;

import com.graphaware.tx.executor.NullItem;
import com.graphaware.tx.executor.input.NoInput;
import org.junit.Test;

import java.util.NoSuchElementException;

import static org.junit.Assert.*;

/**
 * Unit test for {@link NoInput}.
 */
public class NoInputTest {

    @Test
    public void shouldGenerateTheRightNumberOfItems() {
        NoInput generator = new NoInput(3);

        assertTrue(generator.hasNext());
        assertEquals(NullItem.getInstance(), generator.next());
        assertTrue(generator.hasNext());
        assertEquals(NullItem.getInstance(), generator.next());
        assertTrue(generator.hasNext());
        assertEquals(NullItem.getInstance(), generator.next());
        assertFalse(generator.hasNext());
    }

    @Test(expected = NoSuchElementException.class)
    public void shouldThrowExceptionWhenNoMoreItems() {
        NoInput generator = new NoInput(1);

        generator.next();
        generator.next();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldNotSupportRemove() {
        new NoInput(2).remove();
    }

}
