/*
 * Copyright (c) 2013-2020 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.common.stopwatch;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EventTest {

    @Test
    @Ignore
    public void shouldStartOnCreate() {
        Event event = new Event();
        assertTrue(event.duration().equals(event.getStartTime()));
    }

    @Test
    public void shouldAddPeriodOnStop() throws InterruptedException{
        Event event = new Event();
        sleep(100L);
        event.stop();
        assertTrue(event.duration() >= 100);
        assertEquals(1, event.getPeriods().size());
    }

    @Test
    public void shouldHandleLaps() throws InterruptedException{
        Event event = new Event();
        sleep(100L);
        event.addPeriod();
        sleep(100L);
        event.stop();

        assertEquals(100, event.getPeriods().get(0).duration(), 10L);
        assertEquals(100, event.getPeriods().get(1).duration(), 10L);
        assertEquals(200, event.duration(), 20L);
        assertEquals(2, event.getPeriods().size());
    }

    private void sleep(Long millis) throws InterruptedException{
        Thread.sleep(millis);
    }

}
