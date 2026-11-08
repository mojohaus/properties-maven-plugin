package org.codehaus.mojo.properties.managers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
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
     * Load properties using the given character set.
     *
     * @param in      input stream of properties resource
     * @param charset the character set the resource is encoded in
     * @return a properties
     * @throws IOException in case of IO problems
     * @throws UnsupportedOperationException if the manager cannot choose the character set
     * @since 1.4.0
     */
    default Properties load(InputStream in, Charset charset) throws IOException {
        throw new UnsupportedOperationException(getClass().getName() + " does not support a configurable encoding");
    }

    /**
     * Store properties
     *
     * @param properties properties to store
     * @param out        output stream of properties resource
     * @param comments   a comments added to output resource
     * @throws IOException in case of IO problems
     */
    void save(Properties properties, OutputStream out, String comments) throws IOException;

    /**
     * Store properties using the given character set.
     *
     * @param properties properties to store
     * @param out        output stream of properties resource
     * @param comments   a comments added to output resource
     * @param charset    the character set to encode the resource in
     * @throws IOException in case of IO problems
     * @throws UnsupportedOperationException if the manager cannot choose the character set
     * @since 1.4.0
     */
    default void save(Properties properties, OutputStream out, String comments, Charset charset) throws IOException {
        throw new UnsupportedOperationException(getClass().getName() + " does not support a configurable encoding");
    }
}
