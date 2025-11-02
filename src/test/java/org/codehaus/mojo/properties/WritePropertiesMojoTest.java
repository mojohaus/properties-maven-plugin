package org.codehaus.mojo.properties;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Properties;
import java.util.UUID;

import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.properties.managers.JdkPropertiesManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class WritePropertiesMojoTest {

    @TempDir
    public File temporaryFolder;

    private MavenProject projectStub;
    private WriteProjectProperties writeProjectProperties;
    private File outputFile;

    @BeforeEach
    void setUp() throws IOException {
        projectStub = new MavenProject();
        writeProjectProperties = new WriteProjectProperties(Collections.singletonList(new JdkPropertiesManager()));
        writeProjectProperties.setProject(projectStub);
        outputFile = File.createTempFile("junit", null, temporaryFolder);
        writeProjectProperties.setOutputFile(outputFile);
    }

    @Test
    void projectPropertiesAreWritten() throws Exception {
        String propertyKey = UUID.randomUUID().toString();
        projectStub.getProperties().put(propertyKey, "foo");

        writeProjectProperties.execute();

        try (FileReader fr = new FileReader(outputFile)) {
            Properties writtenProperties = new Properties();
            writtenProperties.load(fr);

            assertEquals("foo", writtenProperties.getProperty(propertyKey));
        }
    }

    @Test
    void onlyIncludedPropertiesAreWritten() throws Exception {
        String includedKey = UUID.randomUUID().toString();
        projectStub.getProperties().put(includedKey, "foo");
        String excludedKey = UUID.randomUUID().toString();
        projectStub.getProperties().put(excludedKey, "foo");

        writeProjectProperties.setIncludedPropertyKeys(Collections.singleton(includedKey));
        writeProjectProperties.execute();

        try (FileReader fr = new FileReader(outputFile)) {
            Properties writtenProperties = new Properties();
            writtenProperties.load(fr);

            assertEquals("foo", writtenProperties.getProperty(includedKey));
            assertFalse(writtenProperties.contains(excludedKey));
        }
    }

    @Test
    void onlyNonExcludedPropertiesAreWritten() throws Exception {
        String includedKey = UUID.randomUUID().toString();
        projectStub.getProperties().put(includedKey, "foo");
        String excludedKey = UUID.randomUUID().toString();
        projectStub.getProperties().put(excludedKey, "foo");

        writeProjectProperties.setExcludedPropertyKeys(Collections.singleton(excludedKey));
        writeProjectProperties.execute();

        try (FileReader fr = new FileReader(outputFile)) {
            Properties writtenProperties = new Properties();
            writtenProperties.load(fr);

            assertEquals("foo", writtenProperties.getProperty(includedKey));
            assertFalse(writtenProperties.contains(excludedKey));
        }
    }
}
