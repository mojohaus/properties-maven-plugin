package org.codehaus.mojo.properties.managers;

import javax.inject.Named;
import javax.inject.Singleton;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.yaml.snakeyaml.Yaml;

/**
 * Properties manager using YAML as backand.
 */
@Named
@Singleton
public class YmlPropertiesManager implements PropertiesManager {

    private static final String SUPPORTED_EXTENSION_YML = "yml";
    private static final String SUPPORTED_EXTENSION_YAML = "yaml";

    @Override
    public boolean isExtensionSupport(final String extension) {
        return SUPPORTED_EXTENSION_YML.equals(extension) || SUPPORTED_EXTENSION_YAML.equals(extension);
    }

    @Override
    public Properties load(final InputStream in) throws IOException {
        final Properties properties = new Properties();
        final Map<String, Object> map = flattenYamlToMap("", new Yaml().load(in));
        properties.putAll(map);
        return properties;
    }

    public Map<String, Object> flattenYamlToMap(String parentKey, final Map<String, Object> yamlMap) {
        if (parentKey != null && !parentKey.trim().isEmpty()) {
            parentKey = parentKey.trim() + ".";
        }
        final Map<String, Object> result = new HashMap<>();

        for (final Map.Entry<String, Object> entry : yamlMap.entrySet()) {
            final String key = String.format("%s%s", parentKey, entry.getKey().trim());
            final Object value = entry.getValue();
            if (value instanceof Map) {
                final Map<String, Object> stringObjectMap = flattenYamlToMap(key, (Map<String, Object>) value);
                result.putAll(stringObjectMap);
            } else if (value instanceof Collection) {
                final Object[] values = ((Collection<?>) value).toArray();
                for (int i = 0; i < values.length; i++) {
                    if (values[i] instanceof Map) {
                        result.putAll(flattenYamlToMap(
                                String.format("%s%s[%d]", parentKey, entry.getKey(), i), (Map) values[i]));
                    } else {
                        result.put(String.format("%s%s[%d]", parentKey, entry.getKey(), i), String.valueOf(values[i]));
                    }
                }
            } else {
                result.put(key, value);
            }
        }
        return result;
    }

    @Override
    public void save(final Properties properties, final OutputStream out, String comments) throws IOException {

        final OutputStreamWriter outputStreamWriter = new OutputStreamWriter(out, StandardCharsets.ISO_8859_1);

        try (PrintWriter pw = new PrintWriter(outputStreamWriter);
                StringWriter sw = new StringWriter()) {
            properties.store(sw, comments);
            comments = '#' + comments;

            final List<String> lines = new ArrayList<>();
            try (BufferedReader r = new BufferedReader(new StringReader(sw.toString()))) {
                String line;
                while ((line = r.readLine()) != null) {
                    if (!line.startsWith("#") || line.equals(comments)) {
                        lines.add(line);
                    }
                }
            }

            Collections.sort(lines);
            for (final String l : lines) {
                pw.println(l);
            }
        }
    }

    @Override
    public String toString() {
        return String.format("%s [extension=%s]", getClass().getSimpleName(), SUPPORTED_EXTENSION_YML);
    }
}
