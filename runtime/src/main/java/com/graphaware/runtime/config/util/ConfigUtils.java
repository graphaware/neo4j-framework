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

package com.graphaware.runtime.config.util;

import org.neo4j.logging.Log;
import com.graphaware.common.log.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Utilities for runtime configurations.
 */
public final class ConfigUtils {

    private static final Log LOG = LoggerFactory.getLogger(ConfigUtils.class);

    private ConfigUtils() {
    }

    /**
     * Create an instance of a class specified in the config under configKey. This is done exclusively by using a no-arg
     * <code>getInstance()</code> method, or a public no-arg constructor, in this order.
     *
     * @param config                            in which to look for FQN of the class to instantiate.
     * @param configKey                         under which to look.
     * @param tryingToInstantiateWhat           for logging purposes, what is it that is being instantiated.
     * @param whatHappensWhenInstantiationFails for logging purposes, what will be the impact of failing to instantiate the class.
     * @param <T>                               type of the class being instantiated.
     * @return instantiated class.
     */
    public static <T> T instantiate(Map<String, String> config, String configKey, String tryingToInstantiateWhat, String whatHappensWhenInstantiationFails) {
        if (config.get(configKey) != null) {
            String className = config.get(configKey);

            LOG.info("Trying to instantiate class " + className);

            try {
                Class<?> cls = Class.forName(className);
                try {
                    LOG.info("Attempting to instantiate as a singleton...");
                    Method factoryMethod = cls.getDeclaredMethod("getInstance");
                    T result = (T) factoryMethod.invoke(null, null);
                    LOG.info("Success.");
                    return result;
                } catch (NoSuchMethodException | InvocationTargetException e) {
                    LOG.debug("Not a singleton.");
                }

                LOG.info("Attempting to instantiate using public no-arg constructor...");
                T result = (T) cls.newInstance();
                LOG.info("Success.");
                return result;
            } catch (ClassNotFoundException e) {
                LOG.error(tryingToInstantiateWhat + " " + className + " wasn't found on the classpath. " + whatHappensWhenInstantiationFails, e);
                throw new RuntimeException(tryingToInstantiateWhat + " " + className + " wasn't found on the classpath. " + whatHappensWhenInstantiationFails, e);
            } catch (InstantiationException | IllegalAccessException | ClassCastException e) {
                LOG.error("Could not instantiate " + tryingToInstantiateWhat + " " + className + ". " + whatHappensWhenInstantiationFails, e);
                throw new RuntimeException("Could not instantiate " + tryingToInstantiateWhat + " " + className + ". " + whatHappensWhenInstantiationFails, e);
            }
        } else {
            LOG.error(whatHappensWhenInstantiationFails + " No " + tryingToInstantiateWhat + " specified!");
            throw new RuntimeException(whatHappensWhenInstantiationFails + " No " + tryingToInstantiateWhat + " specified!");
        }
    }
}
