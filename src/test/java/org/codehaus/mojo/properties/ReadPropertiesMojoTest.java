package org.codehaus.mojo.properties;

import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import static org.junit.Assert.*;

public class ReadPropertiesMojoTest {
    private static final String NEW_LINE = System.getProperty("line.separator");

    private MavenProject projectStub;
    private ReadPropertiesMojo readPropertiesMojo;

    @Before
    public void setUp() {
        projectStub = new MavenProject();
        projectStub.setBasedir(new File("."));
        readPropertiesMojo = new ReadPropertiesMojo();
        readPropertiesMojo.setProject(projectStub);
    }


    @Test
    public void readPropertiesWithoutKeyprefix() throws Exception {
        File testPropertyFile = getPropertyFileForTesting();
        // load properties directly for comparison later
        Properties testProperties = new Properties();
        testProperties.load(new FileReader(testPropertyFile));

        // do the work
        readPropertiesMojo.setFiles(new File[]{testPropertyFile});
        readPropertiesMojo.execute();

        // check results
        Properties projectProperties = projectStub.getProperties();
        assertNotNull(projectProperties);
        // it should not be empty
        assertNotEquals(0, projectProperties.size());

        // we are not adding prefix, so properties should be same as in file
        assertEquals(testProperties.size(), projectProperties.size());
        assertEquals(testProperties, projectProperties);

    }

    @Test
    public void readPropertiesWithKeyprefix() throws Exception {
        String keyPrefix = "testkey-prefix.";

        File testPropertyFileWithoutPrefix = getPropertyFileForTesting();
        Properties testPropertiesWithoutPrefix = new Properties();
        testPropertiesWithoutPrefix.load(new FileReader(testPropertyFileWithoutPrefix));
        // do the work
        readPropertiesMojo.setKeyPrefix(keyPrefix);
        readPropertiesMojo.setFiles(new File[]{testPropertyFileWithoutPrefix});
        readPropertiesMojo.execute();

        // load properties directly and add prefix for comparison later
        Properties testPropertiesPrefix = new Properties();
        testPropertiesPrefix.load(new FileReader(getPropertyFileForTesting(keyPrefix)));

        // check results
        Properties projectProperties = projectStub.getProperties();
        assertNotNull(projectProperties);
        // it should not be empty
        assertNotEquals(0, projectProperties.size());

        // we are adding prefix, so prefix properties should be same as in projectProperties
        assertEquals(testPropertiesPrefix.size(), projectProperties.size());
        assertEquals(testPropertiesPrefix, projectProperties);

        // properties with and without prefix shouldn't be same
        assertNotEquals(testPropertiesPrefix, testPropertiesWithoutPrefix);
        assertNotEquals(testPropertiesWithoutPrefix, projectProperties);

    }

    private File getPropertyFileForTesting() throws IOException {
        return getPropertyFileForTesting(null);
    }

    private File getPropertyFileForTesting(String keyPrefix) throws IOException {
        File f = File.createTempFile("prop-test", ".properties");
        f.deleteOnExit();
        FileWriter writer = new FileWriter(f);
        String prefix = keyPrefix;
        if (prefix == null) {
            prefix = "";
        }
        try {
            writer.write(prefix + "test.property1=value1" + NEW_LINE);
            writer.write(prefix + "test.property2=value2" + NEW_LINE);
            writer.write(prefix + "test.property3=value3" + NEW_LINE);
            writer.flush();
        } finally {
            writer.close();
        }
        return f;
    }

}
