package org.codehaus.mojo.properties;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import org.apache.maven.model.Model;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

public class ReadPropertiesMojoTest {
    private static final String NEW_LINE = System.getProperty("line.separator");

    private MavenProject projectStub;
    private ReadPropertiesMojo readPropertiesMojo;

    @Before
    public void setUp() {
        projectStub = new MavenProject();
        readPropertiesMojo = new ReadPropertiesMojo();
        readPropertiesMojo.setProject(projectStub);
    }

    @Test
    public void readPropertiesWithoutKeyprefix() throws Exception {
        try (FileReader fr = new FileReader(getPropertyFileForTesting())) {
            // load properties directly for comparison later
            Properties testProperties = new Properties();
            testProperties.load(fr);

            // do the work
            readPropertiesMojo.setFiles(new File[] {getPropertyFileForTesting()});
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
    }

    @Test
    public void readPropertiesWithKeyprefix() throws Exception {
        String keyPrefix = "testkey-prefix.";

        try (FileReader fs1 = new FileReader(getPropertyFileForTesting(keyPrefix));
                FileReader fs2 = new FileReader(getPropertyFileForTesting())) {
            Properties testPropertiesWithoutPrefix = new Properties();
            testPropertiesWithoutPrefix.load(fs2);

            // do the work
            readPropertiesMojo.setKeyPrefix(keyPrefix);
            readPropertiesMojo.setFiles(new File[] {getPropertyFileForTesting()});
            readPropertiesMojo.execute();

            // load properties directly and add prefix for comparison later
            Properties testPropertiesPrefix = new Properties();
            testPropertiesPrefix.load(fs1);

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
    }

    @Test
    public void testDefaultValueForUnresolvedPropertyWithEnabledFlag()
            throws MojoFailureException, MojoExecutionException, IOException {
        Properties properties = new Properties();
        properties.setProperty("p1", "${unknown:}");
        properties.setProperty("p2", "${unknown:defaultValue}");
        properties.setProperty("p3", "http://${uhost:localhost}:${uport:8080}");
        properties.setProperty("p4", "http://${host:localhost}:${port:8080}");
        properties.setProperty("p5", "${unknown:${fallback}}");
        properties.setProperty("p6", "${unknown:${double.unknown}}");
        properties.setProperty("p7", "${unknown:with space}");
        properties.setProperty("p8", "${unknown:with extra :}");
        properties.setProperty("p9", "${malformed:defVal");
        properties.setProperty("p10", "${malformed:with space");
        properties.setProperty("p11", "${malformed:with extra :");
        properties.setProperty("p12", "${unknown::}");
        properties.setProperty("p13", "${unknown:  }");

        properties.setProperty("host", "example.com");
        properties.setProperty("port", "9090");
        properties.setProperty("fallback", "fallback value");

        Model model = new Model();
        model.setProperties(properties);
        MavenProject project = new MavenProject(model);
        readPropertiesMojo.setProject(project);
        readPropertiesMojo.setUseDefaultValues(true);
        readPropertiesMojo.execute();

        Properties processed = readPropertiesMojo.getProject().getProperties();

        String value1 = processed.getProperty("p1");
        String value2 = processed.getProperty("p2");
        String value3 = processed.getProperty("p3");
        String value4 = processed.getProperty("p4");
        String value5 = processed.getProperty("p5");
        String value6 = processed.getProperty("p6");
        String value7 = processed.getProperty("p7");
        String value8 = processed.getProperty("p8");
        String value9 = processed.getProperty("p9");
        String value10 = processed.getProperty("p10");
        String value11 = processed.getProperty("p11");
        String value12 = processed.getProperty("p12");
        String value13 = processed.getProperty("p13");

        assertEquals("${unknown}", value1);
        assertEquals("defaultValue", value2);
        assertEquals("http://localhost:8080", value3);
        assertEquals("http://example.com:9090", value4);
        assertEquals("fallback value", value5);
        assertEquals("${double.unknown}", value6);
        assertEquals("with space", value7);
        assertEquals("with extra :", value8);
        assertEquals("${malformed:defVal", value9);
        assertEquals("${malformed:with space", value10);
        assertEquals("${malformed:with extra :", value11);
        assertEquals(":", value12);
        assertEquals("  ", value13);
    }

    @Test
    public void readPropertiesOverridingExisting() throws Exception {

        File testPropertyFile = getPropertyFileForTesting();
        // load properties directly for comparison later
        Properties testProperties = new Properties();
        testProperties.load(new FileReader(testPropertyFile));

        // Existing property value should be overridden
        projectStub.getProperties().put("test.property2", "old-value");

        // do the work
        readPropertiesMojo.setFiles(new File[] {testPropertyFile});
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
    public void readPropertiesPreserveExisting() throws Exception {

        File testPropertyFile = getPropertyFileForTesting();
        // load properties directly for comparison later
        Properties testProperties = new Properties();
        testProperties.load(new FileReader(testPropertyFile));

        // Existing property should keep the value
        testProperties.put("test.property2", "old-value");
        projectStub.getProperties().put("test.property2", "old-value");

        // do the work
        readPropertiesMojo.setFiles(new File[] {testPropertyFile});
        readPropertiesMojo.setOverride(false);
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

    /**
     * with the flag disabled (default behavior) nothing gets replaced
     * ':' is treated as a regular character and part of the property name
     */
    @Test
    public void testDefaultValueForUnresolvedPropertyWithDisabledFlag()
            throws MojoFailureException, MojoExecutionException, IOException {
        Properties properties = new Properties();
        properties.setProperty("p1", "${unknown:}");
        properties.setProperty("p2", "${unknown:defaultValue}");
        properties.setProperty("p3", "http://${uhost:localhost}:${uport:8080}");
        properties.setProperty("p4", "http://${host:localhost}:${port:8080}");
        properties.setProperty("p5", "${unknown:${fallback}}");
        properties.setProperty("p6", "${unknown:${double.unknown}}");
        properties.setProperty("p7", "${unknown:with space}");
        properties.setProperty("p8", "${unknown:with extra :}");
        properties.setProperty("p9", "${malformed:defVal");
        properties.setProperty("p10", "${malformed:with space");
        properties.setProperty("p11", "${malformed:with extra :");
        properties.setProperty("p12", "${unknown::}");
        properties.setProperty("p13", "${unknown:  }");

        properties.setProperty("host", "example.com");
        properties.setProperty("port", "9090");
        properties.setProperty("fallback", "fallback value");

        Model model = new Model();
        model.setProperties(properties);
        MavenProject project = new MavenProject(model);
        readPropertiesMojo.setProject(project);
        readPropertiesMojo.execute();

        Properties processed = readPropertiesMojo.getProject().getProperties();

        String value1 = processed.getProperty("p1");
        String value2 = processed.getProperty("p2");
        String value3 = processed.getProperty("p3");
        String value4 = processed.getProperty("p4");
        String value5 = processed.getProperty("p5");
        String value6 = processed.getProperty("p6");
        String value7 = processed.getProperty("p7");
        String value8 = processed.getProperty("p8");
        String value9 = processed.getProperty("p9");
        String value10 = processed.getProperty("p10");
        String value11 = processed.getProperty("p11");
        String value12 = processed.getProperty("p12");
        String value13 = processed.getProperty("p13");

        assertEquals("${unknown:}", value1);
        assertEquals("${unknown:defaultValue}", value2);
        assertEquals("http://${uhost:localhost}:${uport:8080}", value3);
        assertEquals("http://${host:localhost}:${port:8080}", value4);
        assertEquals("${unknown:${fallback}}", value5);
        assertEquals("${unknown:${double.unknown}}", value6);
        assertEquals("${unknown:with space}", value7);
        assertEquals("${unknown:with extra :}", value8);
        assertEquals("${malformed:defVal", value9);
        assertEquals("${malformed:with space", value10);
        assertEquals("${malformed:with extra :", value11);
        assertEquals("${unknown::}", value12);
        assertEquals("${unknown:  }", value13);
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
