package org.codehaus.mojo.properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

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

public class WritePropertiesMojoTest {
    private static final String NEW_LINE = System.getProperty("line.separator");

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private MavenProject projectStub;
    private WriteProjectProperties writeProjectProperties;
    private File outputFile;

    @Before
    public void setUp() throws IOException {
        projectStub = new MavenProject();
        writeProjectProperties = new WriteProjectProperties(Collections.singletonList(new JdkPropertiesManager()));
        writeProjectProperties.setProject(projectStub);
        outputFile = temporaryFolder.newFile();
        writeProjectProperties.setOutputFile(outputFile);
    }

    @Test
    public void projectPropertiesAreWritten() throws Exception {
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
    public void onlyIncludedPropertiesAreWritten() throws Exception {
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
    public void onlyNonExcludedPropertiesAreWritten() throws Exception {
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
