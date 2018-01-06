/*
 * Copyright (c) 2013-2016 GraphAware
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

package com.graphaware.lifecycle.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class StrategyLoader<T> {

	/**
	 * Returns singleton instances given a comma-separated list of class names.
	 */
	public List<T> load(String classNames) {
		return load(Arrays.stream(classNames.split(",")).map(String::trim).collect(Collectors.toList()));
	}

	/**
	 * Given a list of class names, return the corresponding singleton instance
	 *
	 * @throws IllegalArgumentException
	 */
	public List<T> load(List<String> classNames) {
		List<T> results = new ArrayList<>();
		for (String className : classNames) {
			try {
				Class clazz = Class.forName(className);
				Method method = clazz.getMethod("getInstance");
				T instance = (T) method.invoke(clazz);
				results.add(instance);
			} catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
				throw new IllegalArgumentException(e);
			}
		}
		return results;
	}
}
