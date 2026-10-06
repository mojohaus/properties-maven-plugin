package org.codehaus.mojo.properties;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Properties;
import java.util.UUID;

import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.properties.managers.JdkPropertiesManager;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.assertEquals;

public class WritePrefixedPropertiesMojoTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private MavenProject projectStub;
    private WritePrefixedPropertiesMojo writePrefixedPropertiesMojo;
    private File outputFile;
    private String prefix;
    private String commonPrefix;

    @Before
    public void setUp() throws IOException {
        projectStub = new MavenProject();
        writePrefixedPropertiesMojo =
                new WritePrefixedPropertiesMojo(Collections.singletonList(new JdkPropertiesManager()));
        writePrefixedPropertiesMojo.setProject(projectStub);
        outputFile = temporaryFolder.newFile();
        writePrefixedPropertiesMojo.setOutputFile(outputFile);
        prefix = "prefix.";
        writePrefixedPropertiesMojo.setKeyPrefix(prefix);
        commonPrefix = "*.";
        writePrefixedPropertiesMojo.setCommonKeyPrefix(commonPrefix);
    }

    @Test
    public void prefixedPropertiesAreWritten() throws Exception {
        String propertyKey = UUID.randomUUID().toString();
        projectStub.getProperties().put(prefix + propertyKey, "foo");
        projectStub.getProperties().put(propertyKey, "bar");

        writePrefixedPropertiesMojo.execute();

        try (FileReader fr = new FileReader(outputFile)) {
            Properties writtenProperties = new Properties();
            writtenProperties.load(fr);

            assertEquals("foo", writtenProperties.getProperty(propertyKey));
        }
    }

    @Test
    public void propertiesWithCommonPrefixAreWritten() throws Exception {
        String propertyKey = UUID.randomUUID().toString();
        projectStub.getProperties().put(commonPrefix + propertyKey, "foo");

        writePrefixedPropertiesMojo.execute();

        try (FileReader fr = new FileReader(outputFile)) {
            Properties writtenProperties = new Properties();
            writtenProperties.load(fr);

            assertEquals("foo", writtenProperties.getProperty(propertyKey));
        }
    }

    @Test
    public void propertiesWithSpecificAndCommonPrefixAreWrittenWithoutCommonValue() throws Exception {
        String propertyKey = UUID.randomUUID().toString();
        projectStub.getProperties().put(commonPrefix + propertyKey, "foo");
        projectStub.getProperties().put(prefix + propertyKey, "bar");

        writePrefixedPropertiesMojo.execute();

        try (FileReader fr = new FileReader(outputFile)) {
            Properties writtenProperties = new Properties();
            writtenProperties.load(fr);

            assertEquals("bar", writtenProperties.getProperty(propertyKey));
        }
    }
}
