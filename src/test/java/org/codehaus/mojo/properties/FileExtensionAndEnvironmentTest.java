package org.codehaus.mojo.properties;

import java.io.File;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.codehaus.mojo.properties.managers.JdkPropertiesManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FileExtensionAndEnvironmentTest {

    @ParameterizedTest
    @CsvSource(
            nullValues = "NULL",
            value = {
                "app.properties, properties",
                "app.yml, yml",
                "a.b.yaml, yaml",
                "noextension, ''",
                "trailingdot., ''",
                ".hidden, hidden",
                "'', ''"
            })
    void extensionOfFileName(String name, String expected) {
        assertEquals(expected, AbstractPropertiesMojo.extension(name));
    }

    @Test
    void extensionIgnoresDotsInDirectoryNames() {
        String path = "some.dir" + File.separator + "noextension";
        assertEquals("", AbstractPropertiesMojo.extension(path));
        assertEquals("yml", AbstractPropertiesMojo.extension(path + ".yml"));
    }

    @Test
    void extensionOfUrlFileKeepsQueryString() {
        assertEquals("properties?x=1", AbstractPropertiesMojo.extension("/conf/app.properties?x=1"));
    }

    @Test
    void systemEnvVarsMatchTheEnvironment() throws Exception {
        ReadPropertiesMojo mojo = new ReadPropertiesMojo(Collections.singletonList(new JdkPropertiesManager()));
        boolean windows =
                System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("windows");

        Properties envVars = mojo.getSystemEnvVars();

        assertEquals(System.getenv().size(), envVars.size());
        for (Map.Entry<String, String> entry : System.getenv().entrySet()) {
            String key = windows ? entry.getKey().toUpperCase(Locale.ENGLISH) : entry.getKey();
            assertEquals(entry.getValue(), envVars.getProperty(key));
        }
    }
}
