package com.graphaware.common.util;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class VersionReaderTest {

    @Test
    public void frameworkVersionIsReadCorrectly() {
        assertEquals("20.1.3", VersionReader.getVersion());
    }

    @Test
    public void frameworkVersionFileNotExisting() {
        assertEquals("Unknown", VersionReader.getVersion("randomFile"));
    }

    @Test
    public void frameworkVersionWrongFile() {
        assertEquals("Unknown", VersionReader.getVersion("wrongFramework.properties"));
    }
}
