package com.graphaware.common.util;

import java.io.InputStream;
import java.util.Properties;

/**
 * Reads version from property file.
 *
 * Version is replaced in the property file by maven to match the version in POM.
 */
public class VersionReader {

    private static final String UNKNOWN = "Unknown";
    private static final String DEFAULT_VERSION_FILE = "framework.properties";
    private static final String VERSION_PROPERTY = "version";

    /**
     * Reads the version from the default file.
     *
     * The default file is the one, where maven sets current version.
     *
     * The version is not cached and every call of this method results in a file read.
     *
     * @return current version of the project as defined in the POM
     */
    public static String getVersion(){
        return getVersion(DEFAULT_VERSION_FILE);
    }

    /**
     * Reads version from different file
     *
     * Mainly for testing purposes
     *
     * @param fileName file to read from
     * @return version of the project
     */
    public static String getVersion(String fileName) {
        Properties prop = new Properties();
        try (InputStream in = VersionReader.class.getClassLoader().getResourceAsStream(fileName)) {
            prop.load(in);
        } catch (Exception e) {
            return UNKNOWN;
        }
        return prop.getProperty(VERSION_PROPERTY, UNKNOWN);
    }

}
