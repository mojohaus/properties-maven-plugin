package org.codehaus.mojo.properties;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import javax.inject.Inject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.properties.managers.PropertiesManager;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.cli.CommandLineUtils;

/**
 * The read-project-properties goal reads property files and URLs and stores the properties as project properties. It
 * serves as an alternate to specifying properties in pom.xml. It is especially useful when making properties defined in
 * a runtime resource available at build time.
 *
 * @author <a href="mailto:zarars@gmail.com">Zarar Siddiqi</a>
 * @author <a href="mailto:Krystian.Nowak@gmail.com">Krystian Nowak</a>
 *
 * @since 1.0.0
 */
@Mojo(name = "read-project-properties", defaultPhase = LifecyclePhase.NONE, threadSafe = true)
public class ReadPropertiesMojo extends AbstractPropertiesMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    /**
     * The properties files that will be used when reading properties.
     *
     * @since 1.0.0
     */
    @Parameter
    private File[] files = new File[0];

    /**
     * The URLs that will be used when reading properties. These may be non-standard URLs of the form
     * <code>classpath:com/company/resource.properties</code>. Note that the type is not <code>URL</code> for this
     * reason and therefore will be explicitly checked by this Mojo.
     *
     * @since 1.0.0
     */
    @Parameter
    private String[] urls = new String[0];

    /**
     * If the plugin should be quiet if any of the files was not found
     *
     * @since 1.0.0
     */
    @Parameter(defaultValue = "false")
    private boolean quiet;

    /**
     * Prefix that will be added before name of each property.
     * Can be useful for separating properties with same name from different files.
     *
     * @since 1.1.0
     */
    @Parameter
    private String keyPrefix = null;

    /**
     * Skip plugin execution.
     *
     * @since 1.2.0
     */
    @Parameter(defaultValue = "false", property = "prop.skipLoadProperties")
    private boolean skipLoadProperties;

    /**
     * If the plugin should process default values within property placeholders
     *
     * @since 1.2.0
     */
    @Parameter(defaultValue = "false")
    private boolean useDefaultValues;

    /**
     * Determine, whether existing properties should be overridden or not. Default: <code>true</code>.
     *
     * @since 1.2.0
     */
    @Parameter(defaultValue = "true")
    private boolean override = true;

    /**
     * Used for resolving property placeholders.
     */
    private final PropertyResolver resolver = new PropertyResolver();

    /**
     * Default constructor
     *
     * @param propertiesManagers list of properties managers
     */
    @Inject
    public ReadPropertiesMojo(List<PropertiesManager> propertiesManagers) {
        super(propertiesManagers);
    }

    /**
     * @param files The files to set for tests.
     */
    public void setFiles(File[] files) {
        if (files == null) {
            this.files = new File[0];
        } else {
            this.files = new File[files.length];
            System.arraycopy(files, 0, this.files, 0, files.length);
        }
    }

    /**
     * Default scope for test access.
     *
     * @param urls The URLs to set for tests.
     */
    public void setUrls(String[] urls) {
        if (urls == null) {
            this.urls = null;
        } else {
            this.urls = new String[urls.length];
            System.arraycopy(urls, 0, this.urls, 0, urls.length);
        }
    }

    /** {@inheritDoc} */
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!skipLoadProperties) {
            checkParameters();
            loadFiles();
            loadUrls();
            resolveProperties();
        } else {
            getLog().warn("The properties are ignored");
        }
    }

    private void checkParameters() throws MojoExecutionException {
        if (files.length > 0 && urls.length > 0) {
            throw new MojoExecutionException(
                    "Set files or URLs but not both - otherwise " + "no order of precedence can be guaranteed");
        }
    }

    private void loadFiles() throws MojoExecutionException {
        for (File file : files) {
            load(new FileResource(file));
        }
    }

    private void loadUrls() throws MojoExecutionException {
        for (String url : urls) {
            load(new UrlResource(url));
        }
    }

    private void load(Resource resource) throws MojoExecutionException {
        if (resource.canBeOpened()) {
            loadProperties(resource);
        } else {
            missing(resource);
        }
    }

    private void loadProperties(Resource resource) throws MojoExecutionException {
        try {
            getLog().debug("Loading properties from " + resource);

            try (InputStream stream = resource.getInputStream()) {
                String effectivePrefix = "";
                if (keyPrefix != null) {
                    effectivePrefix = keyPrefix;
                }

                PropertiesManager manager = getPropertiesManager(resource.getResourceExtension());
                Properties properties = manager.load(stream);

                Properties projectProperties = project.getProperties();
                Map<String, String> newProperties = new HashMap<>();

                for (String key : properties.stringPropertyNames()) {
                    String propertyName = effectivePrefix + key;
                    if (override || !projectProperties.containsKey(propertyName)) {
                        newProperties.put(propertyName, properties.getProperty(key));
                    }
                }
                // change project properties at one call
                projectProperties.putAll(newProperties);
                getLog().info("Loading " + newProperties.size() + " properties from " + resource);
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Error reading properties from " + resource, e);
        }
    }

    private void missing(Resource resource) throws MojoExecutionException {
        if (quiet) {
            getLog().info("Quiet processing - ignoring properties cannot be loaded from " + resource);
        } else {
            throw new MojoExecutionException("Properties could not be loaded from " + resource);
        }
    }

    private void resolveProperties() throws MojoExecutionException, MojoFailureException {
        getLog().debug("resolve properties");
        Properties environment = loadSystemEnvironmentPropertiesWhenDefined();
        Properties projectProperties = project.getProperties();

        Map<String, String> newProperties = new HashMap<>();

        for (String key : projectProperties.stringPropertyNames()) {
            String newValue = getPropertyValue(key, projectProperties, environment);
            String oldValue = projectProperties.getProperty(key);
            if (!Objects.equals(newValue, oldValue)) {
                newProperties.put(key, newValue);
            }
        }

        if (!newProperties.isEmpty()) {
            getLog().debug("resolve " + newProperties.size() + " properties");
            // change project properties at one call
            projectProperties.putAll(newProperties);
        } else {
            getLog().debug("all properties was resolved");
        }
        getLog().debug("resolve properties - done");
    }

    private Properties loadSystemEnvironmentPropertiesWhenDefined() throws MojoExecutionException {
        Properties projectProperties = project.getProperties();

        boolean useEnvVariables = false;
        for (Enumeration<?> n = projectProperties.propertyNames(); n.hasMoreElements(); ) {
            String k = (String) n.nextElement();
            String p = (String) projectProperties.get(k);
            if (p.contains("${env.")) {
                useEnvVariables = true;
                break;
            }
        }
        Properties environment = null;
        if (useEnvVariables) {
            try {
                environment = getSystemEnvVars();
            } catch (IOException e) {
                throw new MojoExecutionException("Error getting system environment variables: ", e);
            }
        }
        return environment;
    }

    private String getPropertyValue(String k, Properties p, Properties environment) throws MojoFailureException {
        try {
            return resolver.getPropertyValue(k, p, environment, useDefaultValues);
        } catch (IllegalArgumentException e) {
            throw new MojoFailureException(e.getMessage());
        }
    }

    /**
     * Override-able for test purposes.
     *
     * @return The shell environment variables, can be empty but never <code>null</code>.
     * @throws IOException If the environment variables could not be queried from the shell.
     */
    Properties getSystemEnvVars() throws IOException {
        return CommandLineUtils.getSystemEnvVars();
    }

    /**
     * @param useDefaultValues set to <code>true</code> if default values need to be processed within property placeholders
     */
    public void setUseDefaultValues(boolean useDefaultValues) {
        this.useDefaultValues = useDefaultValues;
    }

    void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    void setOverride(boolean override) {
        this.override = override;
    }

    /**
     * Default scope for test access.
     *
     * @param project The test project.
     */
    void setProject(MavenProject project) {
        this.project = project;
    }

    /**
     * For test access.
     * @return The test project
     */
    public MavenProject getProject() {
        return project;
    }

    private abstract static class Resource {
        private InputStream stream;

        public abstract String getResourceExtension();

        public abstract boolean canBeOpened();

        protected abstract InputStream openStream() throws IOException;

        public InputStream getInputStream() throws IOException {
            if (stream == null) {
                stream = openStream();
            }
            return stream;
        }
    }

    private static class FileResource extends Resource {
        private final File file;

        FileResource(File file) {
            this.file = file;
        }

        @Override
        public String getResourceExtension() {
            return FileUtils.extension(file.getName());
        }

        @Override
        public boolean canBeOpened() {
            return file.exists();
        }

        @Override
        protected InputStream openStream() throws IOException {
            return new BufferedInputStream(Files.newInputStream(file.toPath()));
        }

        @Override
        public String toString() {
            return "File: " + file;
        }
    }

    private static class UrlResource extends Resource {
        private static final String CLASSPATH_PREFIX = "classpath:";

        private static final String SLASH_PREFIX = "/";

        private final URL url;

        private boolean isMissingClasspathResouce = false;

        private String classpathUrl;

        UrlResource(String url) throws MojoExecutionException {
            if (url.startsWith(CLASSPATH_PREFIX)) {
                String resource = url.substring(CLASSPATH_PREFIX.length());
                if (resource.startsWith(SLASH_PREFIX)) {
                    resource = resource.substring(1);
                }
                this.url = getClass().getClassLoader().getResource(resource);
                if (this.url == null) {
                    isMissingClasspathResouce = true;
                    classpathUrl = url;
                }
            } else {
                try {
                    this.url = new URL(url);
                } catch (MalformedURLException e) {
                    throw new MojoExecutionException("Badly formed URL " + url + " - " + e.getMessage());
                }
            }
        }

        @Override
        public String getResourceExtension() {
            return FileUtils.extension(url.getFile());
        }

        @Override
        public boolean canBeOpened() {
            if (isMissingClasspathResouce) {
                return false;
            }
            try {
                openStream();
            } catch (IOException e) {
                return false;
            }
            return true;
        }

        @Override
        protected InputStream openStream() throws IOException {
            return new BufferedInputStream(url.openStream());
        }

        @Override
        public String toString() {
            if (!isMissingClasspathResouce) {
                return "URL " + url.toString();
            }
            return classpathUrl;
        }
    }
}
