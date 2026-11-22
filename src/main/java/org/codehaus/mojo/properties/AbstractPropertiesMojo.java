package org.codehaus.mojo.properties;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.mojo.properties.managers.PropertiesManager;

/**
 * Abstract Mojo with Properties managers support.
 */
public abstract class AbstractPropertiesMojo extends AbstractMojo {

    private final List<PropertiesManager> propertiesManagers;

    protected AbstractPropertiesMojo(List<PropertiesManager> propertiesManagers) {
        this.propertiesManagers = propertiesManagers;
    }

    /**
     * Resolve the configured encoding.
     *
     * @param encoding name of a character set, or {@code null} or empty to keep the manager's default
     * @return the character set, or {@code null} when none is configured
     * @throws MojoExecutionException if the name is not a supported character set
     */
    protected static Charset toCharset(String encoding) throws MojoExecutionException {
        if (encoding == null || encoding.trim().isEmpty()) {
            return null;
        }
        try {
            return Charset.forName(encoding.trim());
        } catch (IllegalCharsetNameException | UnsupportedCharsetException e) {
            throw new MojoExecutionException("Invalid encoding '" + encoding + "'", e);
        }
    }

    /**
     * Return the extension of a file name or path: the text after the last dot that follows the last file separator.
     *
     * @param filename a file name or path
     * @return the extension without the dot, or an empty string when there is none
     */
    static String extension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return lastDot > filename.lastIndexOf(File.separatorChar) ? filename.substring(lastDot + 1) : "";
    }

    protected PropertiesManager getPropertiesManager(String resourceExtension) {
        getLog().debug("Available properties managers: " + propertiesManagers);

        String resourceExtensionLowerCase = resourceExtension.toLowerCase(Locale.ROOT);
        Optional<PropertiesManager> propertiesStore = propertiesManagers.stream()
                .filter(manager -> manager.isExtensionSupport(resourceExtensionLowerCase))
                .findFirst();

        if (!propertiesStore.isPresent()) {
            getLog().warn("Unknown properties resource extension: '" + resourceExtension + "' assume as: '"
                    + PropertiesManager.DEFAULT_MANAGER_EXTENSION + "'");
            return getDefaultPropertiesManager();
        } else {
            return propertiesStore.get();
        }
    }

    private PropertiesManager getDefaultPropertiesManager() {
        return propertiesManagers.stream()
                .filter(manager -> manager.isExtensionSupport(PropertiesManager.DEFAULT_MANAGER_EXTENSION))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Default properties manager not exist"));
    }
}
