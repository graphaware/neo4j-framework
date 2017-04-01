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

package com.graphaware.runtime.module.thirdparty;

import com.graphaware.writer.thirdparty.BaseThirdPartyWriter;
import com.graphaware.writer.thirdparty.WriteOperation;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Writer for testing. Remembers what's been submitted to it.
 */
class RememberingWriter extends BaseThirdPartyWriter {

    List<Collection<WriteOperation<?>>> remembered = new LinkedList<>();

    @Override
    protected void processOperations(List<Collection<WriteOperation<?>>> operations) {
        remembered.addAll(operations);
    }

    public List<Collection<WriteOperation<?>>> getRemembered() {
        return remembered;
    }
}
