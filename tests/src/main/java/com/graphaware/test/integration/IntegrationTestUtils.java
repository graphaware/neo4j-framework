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

package com.graphaware.test.integration;

import org.apache.commons.io.FileUtils;
import org.neo4j.configuration.SettingImpl;
import org.neo4j.configuration.SettingValueParsers;
import org.neo4j.harness.Neo4jBuilder;
import org.neo4j.procedure.Procedure;
import org.neo4j.procedure.UserFunction;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystems;
import java.util.*;
import java.util.regex.Pattern;

public final class IntegrationTestUtils {

    public static Neo4jBuilder applyConfig(Neo4jBuilder builder, String fileName) {
        Properties properties = new Properties();
        try {
            properties.load(new ClassPathResource(fileName).getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (String key : properties.stringPropertyNames()) {
            builder = builder.withConfig(SettingImpl.newBuilder(key, SettingValueParsers.STRING, "").build(), properties.getProperty(key));
        }
        return builder;
    }

    public static Neo4jBuilder registerAllProceduresAndFunctions(Neo4jBuilder builder) {
        for (Class cls : proceduresAndFunctionsOnClassPath()) {
            builder.withProcedure(cls);
            builder.withFunction(cls);
        }

        return builder;
    }

    /**
     * Find all classes on classpath (only .class files) that have a method annotated with {@link Procedure} or {@link org.neo4j.procedure.UserFunction}.
     *
     * @return classes with procedures.
     */
    public static Iterable<Class> proceduresAndFunctionsOnClassPath() {
        Enumeration<URL> urls;

        try {
            urls = IntegrationTestUtils.class.getClassLoader().getResources("");
        } catch (IOException e) {
            throw new RuntimeException();
        }

        Set<Class> classes = new HashSet<>();

        while (urls.hasMoreElements()) {
            Iterator<File> fileIterator;
            File directory;

            URI uri = null;
            try {
                uri = urls.nextElement().toURI();
                directory = new File(uri);
                fileIterator = FileUtils.iterateFiles(directory, new String[]{"class"}, true);
            } catch (Exception e) {
                System.out.println("Skipping " + (uri != null ? uri.toString() : null) + "... " + e.getMessage());
                continue;
            }

            while (fileIterator.hasNext()) {
                File file = fileIterator.next();
                try {
                    String path = file.getAbsolutePath();
                    Class<?> candidate = Class.forName(path.substring(directory.getAbsolutePath().length() + 1, path.length() - 6)
                            .replaceAll(Pattern.quote(FileSystems.getDefault().getSeparator()), "."));

                    for (Method m : candidate.getDeclaredMethods()) {
                        if (m.isAnnotationPresent(Procedure.class) || m.isAnnotationPresent(UserFunction.class)) {
                            classes.add(candidate);
                        }
                    }

                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return classes;
    }

    private IntegrationTestUtils() {
    }
}
