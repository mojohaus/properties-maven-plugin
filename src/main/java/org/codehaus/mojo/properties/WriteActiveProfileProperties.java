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
import java.util.Properties;

import org.apache.maven.model.Profile;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.codehaus.mojo.properties.managers.PropertiesManager;

/**
 * Writes properties of all active profiles to a file.
 *
 * @author <a href="mailto:zarars@gmail.com">Zarar Siddiqi</a>
 *
 * @since 1.0.0
 */
@Mojo(name = "write-active-profile-properties", defaultPhase = LifecyclePhase.NONE, threadSafe = true)
public class WriteActiveProfileProperties extends AbstractWritePropertiesMojo {

    /**
     * Default constructor
     *
     * @param propertiesManagers list of properties managers
     */
    @Inject
    protected WriteActiveProfileProperties(List<PropertiesManager> propertiesManagers) {
        super(propertiesManagers);
    }

    @Override
    public void execute() throws MojoExecutionException {
        validateOutputFile();

        List<Profile> profiles = getProject().getActiveProfiles();
        getLog().debug("Active profile: " + profiles);

        Properties properties = new Properties();
        for (Profile profile : profiles) {
            if (profile.getProperties() != null) {
                properties.putAll(profile.getProperties());
            }
        }

        writeProperties(properties);
    }
}
