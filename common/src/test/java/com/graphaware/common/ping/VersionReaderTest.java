package com.graphaware.common.ping;

import com.graphaware.common.util.VersionReader;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

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
