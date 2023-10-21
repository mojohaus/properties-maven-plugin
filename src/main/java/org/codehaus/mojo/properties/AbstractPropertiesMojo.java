package org.codehaus.mojo.properties;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.apache.maven.plugin.AbstractMojo;
import org.codehaus.mojo.properties.managers.PropertiesManager;

/**
 * Abstract Mojo with Properties managers support.
 */
public abstract class AbstractPropertiesMojo extends AbstractMojo {

    private final List<PropertiesManager> propertiesManagers;

    protected AbstractPropertiesMojo(List<PropertiesManager> propertiesManagers) {
        this.propertiesManagers = propertiesManagers;
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
