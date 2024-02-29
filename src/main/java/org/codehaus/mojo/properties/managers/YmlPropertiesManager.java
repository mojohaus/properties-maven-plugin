package org.codehaus.mojo.properties.managers;

import org.yaml.snakeyaml.Yaml;

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

/**
 * Properties manager using Yaml as backand.
 */
@Named
@Singleton
public class YmlPropertiesManager implements PropertiesManager {

    private static final String SUPPORTED_EXTENSION1 = "yml";
    private static final String SUPPORTED_EXTENSION2 = "yaml";

    @Override
    public boolean isExtensionSupport(String extension) {
        return SUPPORTED_EXTENSION1.equals(extension) || SUPPORTED_EXTENSION2.equals(extension);
    }

    @Override
    public Properties load(InputStream in) throws IOException {
        Properties properties = new Properties();
        Map map = flattenYamlToMap("", new Yaml().load(in));
        properties.putAll(map);
        return properties;
    }

    public Map<String, Object> flattenYamlToMap(String parentKey, Map<String, Object> yamlMap) {
        if (parentKey != null && !parentKey.trim().isEmpty()) {
            parentKey = parentKey.trim() + ".";
        }
        Map<String, Object> result = new HashMap<String, Object>();
        for (Map.Entry entry : yamlMap.entrySet()) {
            String key = String.format("%s%s", parentKey, ((String) entry.getKey()).trim());
            Object value = entry.getValue();
            if (value instanceof Map) {
                Map<String, Object> stringObjectMap = flattenYamlToMap(key, (Map<String, Object>) value);
                result.putAll(stringObjectMap);
            } else if (value instanceof Collection) {
                Object[] values = ((Collection) value).toArray();
                for (int i = 0; i < values.length; i++) {
                    if (values[i] instanceof Map) {
                        result.putAll(flattenYamlToMap(String.format("%s%s[%d]", parentKey, entry.getKey(), i), (Map) values[i]));
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
    public void save(Properties properties, OutputStream out, String comments) throws IOException {

        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(out, StandardCharsets.ISO_8859_1);

        try (PrintWriter pw = new PrintWriter(outputStreamWriter);
             StringWriter sw = new StringWriter()) {
            properties.store(sw, comments);
            comments = '#' + comments;

            List<String> lines = new ArrayList<>();
            try (BufferedReader r = new BufferedReader(new StringReader(sw.toString()))) {
                String line;
                while ((line = r.readLine()) != null) {
                    if (!line.startsWith("#") || line.equals(comments)) {
                        lines.add(line);
                    }
                }
            }

            Collections.sort(lines);
            for (String l : lines) {
                pw.println(l);
            }
        }
    }

    @Override
    public String toString() {
        return String.format("%s [extension=%s]", getClass().getSimpleName(), SUPPORTED_EXTENSION1);
    }
}
