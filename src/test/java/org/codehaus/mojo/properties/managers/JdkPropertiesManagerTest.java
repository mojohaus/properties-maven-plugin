package org.codehaus.mojo.properties.managers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
}
