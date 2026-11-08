package org.codehaus.mojo.properties.managers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JdkPropertiesManagerTest {

    private static final String NL = System.lineSeparator();

    private final JdkPropertiesManager manager = new JdkPropertiesManager();

    @Test
    void load() throws Exception {

        // given
        String props = "# comments" + NL + NL + "key1=value1" + NL + "key2 = value2";

        ByteArrayInputStream inputStream = new ByteArrayInputStream(props.getBytes());

        // when
        Properties properties = manager.load(inputStream);

        // then
        assertEquals(2, properties.size());
        assertEquals("value1", properties.getProperty("key1"));
        assertEquals("value2", properties.getProperty("key2"));
    }

    @Test
    void save() throws Exception {

        // given
        Properties properties = new Properties();
        properties.setProperty("key1", "value1");
        properties.setProperty("key2", "value2");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // when
        manager.save(properties, outputStream, "Test comments");

        // then
        String expected = "#Test comments" + NL + "key1=value1" + NL + "key2=value2" + NL;

        assertEquals(expected, outputStream.toString());
    }

    @Test
    void loadAndSaveWithCharset() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("key", "Gr\u00fc\u00dfe \u20ac");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        manager.save(properties, outputStream, "Test comments", StandardCharsets.UTF_8);
        assertEquals(
                "#Test comments" + NL + "key=Gr\u00fc\u00dfe \u20ac" + NL,
                new String(outputStream.toByteArray(), StandardCharsets.UTF_8));

        Properties loaded = manager.load(new ByteArrayInputStream(outputStream.toByteArray()), StandardCharsets.UTF_8);
        assertEquals("Gr\u00fc\u00dfe \u20ac", loaded.getProperty("key"));
    }
}
