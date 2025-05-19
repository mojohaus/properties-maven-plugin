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

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.mojo.properties.managers.PropertiesManager;

/**
 * Writes project properties having keys with specific or common prefix to a file, removing this prefix.
 * Can be used in association with keyPrefix attribute on ReadPropertiesMojo, to isolate properties for specific
 *  environment for example.
 * @version $Id$
 */
@Mojo(name = "write-prefixed-properties", defaultPhase = LifecyclePhase.NONE, threadSafe = true)
public class WritePrefixedPropertiesMojo extends AbstractWritePropertiesMojo {

    /**
     * Prefix of the properties that should be written.
     * Can be useful to write properties with different prefix into different files removing this prefix.
     * A property with this prefix will be written even if another property with the common prefix exists.
     */
    @Parameter(required = true)
    protected String keyPrefix = null;

    /**
     * Common prefix of the properties that should be written.
     * Can be useful to write properties with common prefix into different files removing this prefix.
     * A property with this common prefix will be ignored if another property with the (specific) prefix exists.
     */
    @Parameter(defaultValue = "*.")
    protected String commonKeyPrefix = null;

    /**
     * Default constructor
     *
     * @param propertiesManagers list of properties managers
     */
    @Inject
    protected WritePrefixedPropertiesMojo(List<PropertiesManager> propertiesManagers) {
        super(propertiesManagers);
    }

    /** {@inheritDoc} */
    public void execute() throws MojoExecutionException, MojoFailureException {
        validateOutputFile();

        Properties prefixedProperties = new Properties();

        // Add common (prefix) properties
        Map<String, Object> commonPropertiesMap = getProject().getProperties().keySet().stream()
                .filter(k -> k.toString().startsWith(commonKeyPrefix))
                .collect(Collectors.toMap(
                        k -> k.toString().substring(commonKeyPrefix.length()),
                        k -> getProject().getProperties().get(k)));
        prefixedProperties.putAll(commonPropertiesMap);

        // Add (or override) specific (with prefix) properties
        Map<String, Object> prefixPropertiesMap = getProject().getProperties().keySet().stream()
                .filter(k -> k.toString().startsWith(keyPrefix))
                .collect(Collectors.toMap(
                        k -> k.toString().substring(keyPrefix.length()),
                        k -> getProject().getProperties().get(k)));
        prefixedProperties.putAll(prefixPropertiesMap);

        // stores K/V in outputfile
        writeProperties(prefixedProperties);
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public void setCommonKeyPrefix(String commonKeyPrefix) {
        this.commonKeyPrefix = commonKeyPrefix;
    }
}
