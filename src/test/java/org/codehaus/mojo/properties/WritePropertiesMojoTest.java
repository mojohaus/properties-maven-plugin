package org.codehaus.mojo.properties;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Properties;
import java.util.UUID;

import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.DefaultMavenExecutionResult;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.properties.managers.JdkPropertiesManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class WritePropertiesMojoTest {

    @TempDir
    File temporaryFolder;

    private MavenProject projectStub;
    private WriteProjectProperties writeProjectProperties;
    private File outputFile;

    @BeforeEach
    void setUp() throws IOException {
        projectStub = new MavenProject();
        writeProjectProperties = new WriteProjectProperties(Collections.singletonList(new JdkPropertiesManager()));
        writeProjectProperties.setProject(projectStub);
        outputFile = File.createTempFile("write", ".properties", temporaryFolder);
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
            assertFalse(writtenProperties.containsKey(excludedKey));
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
            assertFalse(writtenProperties.containsKey(excludedKey));
        }
    }

    @Test
    void userPropertiesAreNotWrittenByDefault() throws Exception {
        String userKey = UUID.randomUUID().toString();
        writeProjectProperties.setSession(sessionWithUserProperty(userKey, "bar"));

        writeProjectProperties.execute();

        assertFalse(readWritten().containsKey(userKey));
    }

    @Test
    void userPropertiesAreWrittenWhenRequested() throws Exception {
        String userKey = UUID.randomUUID().toString();
        String projectKey = UUID.randomUUID().toString();
        projectStub.getProperties().put(projectKey, "foo");
        projectStub.getProperties().put(userKey, "from-pom");
        writeProjectProperties.setSession(sessionWithUserProperty(userKey, "bar"));
        writeProjectProperties.setIncludeUserProperties(true);

        writeProjectProperties.execute();

        Properties written = readWritten();
        assertEquals("bar", written.getProperty(userKey));
        assertEquals("foo", written.getProperty(projectKey));
    }

    @Test
    void userPropertiesHonourIncludedAndExcludedKeys() throws Exception {
        String keptKey = UUID.randomUUID().toString();
        String droppedKey = UUID.randomUUID().toString();
        MavenSession session = sessionWithUserProperty(keptKey, "bar");
        session.getUserProperties().setProperty(droppedKey, "baz");
        writeProjectProperties.setSession(session);
        writeProjectProperties.setIncludeUserProperties(true);
        writeProjectProperties.setExcludedPropertyKeys(Collections.singleton(droppedKey));

        writeProjectProperties.execute();

        Properties written = readWritten();
        assertEquals("bar", written.getProperty(keptKey));
        assertFalse(written.containsKey(droppedKey));

        writeProjectProperties.setExcludedPropertyKeys(null);
        writeProjectProperties.setIncludedPropertyKeys(Collections.singleton(droppedKey));

        writeProjectProperties.execute();

        written = readWritten();
        assertEquals("baz", written.getProperty(droppedKey));
        assertFalse(written.containsKey(keptKey));
    }

    @Test
    void jvmSystemPropertiesAreNotWrittenAsUserProperties() throws Exception {
        writeProjectProperties.setSession(sessionWithUserProperty("some.user.key", "bar"));
        writeProjectProperties.setIncludeUserProperties(true);

        writeProjectProperties.execute();

        assertFalse(readWritten().containsKey("java.version"));
    }

    private MavenSession sessionWithUserProperty(String key, String value) {
        DefaultMavenExecutionRequest request = new DefaultMavenExecutionRequest();
        request.getUserProperties().setProperty(key, value);
        return new MavenSession(null, null, request, new DefaultMavenExecutionResult());
    }

    private Properties readWritten() throws IOException {
        try (FileReader fr = new FileReader(outputFile)) {
            Properties written = new Properties();
            written.load(fr);
            return written;
        }
    }
}
