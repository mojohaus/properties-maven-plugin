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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.mojo.properties.managers.PropertiesManager;
import org.codehaus.plexus.util.FileUtils;

/**
 * @author <a href="mailto:zarars@gmail.com">Zarar Siddiqi</a>
 */
public abstract class AbstractWritePropertiesMojo extends AbstractPropertiesMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    /**
     * Output file for storing properties.
     *
     * @since 1.0.0
     */
    @Parameter(required = true, property = "properties.outputFile")
    private File outputFile;

    protected AbstractWritePropertiesMojo(List<PropertiesManager> propertiesManagers) {
        super(propertiesManagers);
    }

    /**
     * @param properties {@link Properties}
     * @throws MojoExecutionException {@link MojoExecutionException}
     */
    protected void writeProperties(Properties properties) throws MojoExecutionException {
        try {
            PropertiesManager manager = getPropertiesManager(FileUtils.extension(outputFile.getName()));
            manager.save(properties, Files.newOutputStream(outputFile.toPath()), "Properties");
        } catch (FileNotFoundException e) {
            getLog().error("Could not create FileOutputStream: " + outputFile);
            throw new MojoExecutionException(e.getMessage(), e);
        } catch (IOException e) {
            getLog().error("Error writing properties: " + outputFile);
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    /**
     * @throws MojoExecutionException {@link MojoExecutionException}
     */
    protected void validateOutputFile() throws MojoExecutionException {
        if (outputFile == null) {
            throw new MojoExecutionException("outputFile parameter is missing");
        }

        if (outputFile.isDirectory()) {
            throw new MojoExecutionException("outputFile must be a file and not a directory");
        }
        // ensure path exists
        if (outputFile.getParentFile() != null) {
            outputFile.getParentFile().mkdirs();
        }
    }

    /**
     * @return {@link MavenProject}
     */
    public MavenProject getProject() {
        return project;
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
     * Default scope for test access.
     */
    void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }
}
