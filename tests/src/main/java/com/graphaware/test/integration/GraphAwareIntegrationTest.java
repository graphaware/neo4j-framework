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

package com.graphaware.test.integration;

import org.apache.commons.io.FileUtils;
import org.neo4j.kernel.api.exceptions.KernelException;
import org.neo4j.kernel.impl.proc.Procedures;
import org.neo4j.procedure.Procedure;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;


/**
 * Base-class for tests of APIs that are written as Spring MVC {@link org.springframework.stereotype.Controller}s
 * and deployed using the GraphAware Framework.
 * <p/>
 * Starts a Neo4j server on the port specified using {@link #neoServerPort()} (or 7575 by default) and deploys all
 * {@link org.springframework.stereotype.Controller} annotated controllers.
 * <p/>
 * Allows implementing tests to call {@link #getDatabase()} and thus gain low-level access to the database
 * even when running within a server. This is useful, for instance, when using
 * {@link com.graphaware.test.unit.GraphUnit} to assert the database state. Before tests are run, the database can be populated
 * by overriding the {@link #populateDatabase(org.neo4j.graphdb.GraphDatabaseService)} method, or by providing a
 * {@link com.graphaware.test.data.DatabasePopulator} in {@link #databasePopulator()}.
 * <p>
 * Also works around the issue that Neo4j only registers procedures that it finds in .jar files (not .class) files, so
 * this class by default registers all {@link Procedure}s that are part of the current project (are on classpath). You
 * can disable this behaviour by overridding {@link #shouldRegisterProcedures()}.
 */
public abstract class GraphAwareIntegrationTest extends ServerIntegrationTest {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void registerProcedures(Procedures procedures) throws Exception {
        super.registerProcedures(procedures);

        for (Class cls : proceduresOnClassPath()) {
            procedures.registerProcedure(cls);
        }
    }

    @Override
    protected Map<String, String> thirdPartyJaxRsPackageMappings() {
        return Collections.singletonMap("com.graphaware.server", "/graphaware");
    }

    public String baseUrl() {
        return baseNeoUrl() + "/graphaware";
    }

    /**
     * Find all classes on classpath (only .class files) that have a method annotated with {@link Procedure}.
     *
     * @return classes with procedures.
     */
    protected final Iterable<Class> proceduresOnClassPath() {
        Enumeration<URL> urls;

        try {
            urls = this.getClass().getClassLoader().getResources("");
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
                    Class<?> candidate = Class.forName(path.substring(directory.getAbsolutePath().length() + 1, path.length() - 6).replaceAll("\\/", "."));

                    for (Method m : candidate.getDeclaredMethods()) {
                        if (m.isAnnotationPresent(Procedure.class)) {
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
}
