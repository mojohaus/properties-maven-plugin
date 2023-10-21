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
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Properties manager using JDK properties as backand.
 */
@Named
@Singleton
public class JdkPropertiesManager implements PropertiesManager {

    private static final String SUPPORTED_EXTENSION = "properties";

    @Override
    public boolean isExtensionSupport(String extension) {
        return SUPPORTED_EXTENSION.equals(extension);
    }

    @Override
    public Properties load(InputStream in) throws IOException {
        Properties properties = new Properties();
        properties.load(in);
        return properties;
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
        return String.format("%s [extension=%s]", getClass().getSimpleName(), SUPPORTED_EXTENSION);
    }
}
