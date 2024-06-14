package org.codehaus.mojo.properties.managers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class JdkPropertiesManagerTest {

    private static final String NL = System.lineSeparator();

    private final JdkPropertiesManager manager = new JdkPropertiesManager();

    @Test
    public void testLoad() throws IOException {

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
    public void testSave() throws IOException {

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
