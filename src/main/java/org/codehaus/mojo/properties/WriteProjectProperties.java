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

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.mojo.properties.managers.PropertiesManager;

/**
 * Writes project properties to a file.
 *
 * @author <a href="mailto:zarars@gmail.com">Zarar Siddiqi</a>
 *
 * @since 1.0.0
 */
@Mojo(name = "write-project-properties", defaultPhase = LifecyclePhase.NONE, threadSafe = true)
public class WriteProjectProperties extends AbstractWritePropertiesMojo {

    /**
     * Property keys to exclude.
     */
    @Parameter(property = "properties.excludedPropertyKeys")
    private Set<String> excludedPropertyKeys;

    /**
     * Property keys to include.
     */
    @Parameter(property = "properties.includedPropertyKeys")
    private Set<String> includedPropertyKeys;

    /**
     * Default constructor
     *
     * @param propertiesManagers list of properties managers
     */
    @Inject
    protected WriteProjectProperties(List<PropertiesManager> propertiesManagers) {
        super(propertiesManagers);
    }

    @Override
    public void execute() throws MojoExecutionException {
        validateOutputFile();
        Properties projProperties = new Properties();
        projProperties.putAll(getProject().getProperties());

        Properties systemProperties = System.getProperties();

        // allow system properties to over write key/value found in maven properties
        Enumeration<?> enumeration = systemProperties.keys();
        while (enumeration.hasMoreElements()) {
            String key = (String) enumeration.nextElement();
            String value = systemProperties.getProperty(key);
            if (projProperties.get(key) != null) {
                projProperties.put(key, value);
            }
        }

        Optional.ofNullable(excludedPropertyKeys)
                .orElseGet(Collections::emptySet)
                .forEach(projProperties::remove);

        if (includedPropertyKeys != null && !includedPropertyKeys.isEmpty()) {
            projProperties.keySet().removeIf(key -> !includedPropertyKeys.contains(String.valueOf(key)));
        }

        writeProperties(projProperties);
    }
}
