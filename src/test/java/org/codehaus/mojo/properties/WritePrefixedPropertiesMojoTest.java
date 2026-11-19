package org.codehaus.mojo.properties;

import java.io.File;
import java.io.FileReader;
import java.util.Collections;
import java.util.Properties;
import java.util.UUID;

import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.properties.managers.JdkPropertiesManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WritePrefixedPropertiesMojoTest {

    @TempDir
    File temporaryFolder;

    private MavenProject projectStub;
    private WritePrefixedPropertiesMojo writePrefixedPropertiesMojo;
    private File outputFile;
    private String prefix;
    private String commonPrefix;

    @BeforeEach
    void setUp() {
        projectStub = new MavenProject();
        writePrefixedPropertiesMojo =
                new WritePrefixedPropertiesMojo(Collections.singletonList(new JdkPropertiesManager()));
        writePrefixedPropertiesMojo.setProject(projectStub);
        outputFile = new File(temporaryFolder, "output.properties");
        writePrefixedPropertiesMojo.setOutputFile(outputFile);
        prefix = "prefix.";
        writePrefixedPropertiesMojo.setKeyPrefix(prefix);
        commonPrefix = "*.";
        writePrefixedPropertiesMojo.setCommonKeyPrefix(commonPrefix);
    }

    @Test
    void prefixedPropertiesAreWritten() throws Exception {
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
    void propertiesWithCommonPrefixAreWritten() throws Exception {
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
    void propertiesWithSpecificAndCommonPrefixAreWrittenWithoutCommonValue() throws Exception {
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
