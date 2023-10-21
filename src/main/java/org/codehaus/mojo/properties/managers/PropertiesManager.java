package org.codehaus.mojo.properties.managers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 * Interface for properties managers implementations.
 */
public interface PropertiesManager {

    String DEFAULT_MANAGER_EXTENSION = "properties";

    /**
     * Determinate if manager support resource by file extension.
     *
     * @param extension file extension
     * @return true if extension is supported
     */
    boolean isExtensionSupport(String extension);
    /**
     * Load properties.
     *
     * @param in input stream of properties resource
     * @return a properties
     * @throws IOException in case of IO problems
     */
    Properties load(InputStream in) throws IOException;

    /**
     * Store properties
     *
     * @param properties properties to store
     * @param out        output stream of properties resource
     * @param comments   a comments added to output resource
     * @throws IOException in case of IO problems
     */
    void save(Properties properties, OutputStream out, String comments) throws IOException;
}
