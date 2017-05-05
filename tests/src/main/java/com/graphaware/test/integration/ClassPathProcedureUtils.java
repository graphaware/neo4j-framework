package com.graphaware.test.integration;

import org.apache.commons.io.FileUtils;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.impl.factory.GraphDatabaseFacade;
import org.neo4j.kernel.impl.proc.Procedures;
import org.neo4j.procedure.Procedure;
import org.neo4j.procedure.UserFunction;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public final class ClassPathProcedureUtils {

    public static void registerAllProceduresAndFunctions(GraphDatabaseService database) throws Exception {
        registerAllProceduresAndFunctions(((GraphDatabaseFacade) database).getDependencyResolver().resolveDependency(Procedures.class));
    }

    public static void registerAllProceduresAndFunctions(Procedures procedures) throws Exception {
        for (Class cls : proceduresAndFunctionsOnClassPath()) {
            procedures.registerProcedure(cls);
            procedures.registerFunction(cls);
        }
    }

    /**
     * Find all classes on classpath (only .class files) that have a method annotated with {@link Procedure} or {@link org.neo4j.procedure.UserFunction}.
     *
     * @return classes with procedures.
     */
    public static Iterable<Class> proceduresAndFunctionsOnClassPath() {
        Enumeration<URL> urls;

        try {
            urls = ClassPathProcedureUtils.class.getClassLoader().getResources("");
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

    private ClassPathProcedureUtils() {
    }
}
