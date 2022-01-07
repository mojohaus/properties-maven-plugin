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

import java.io.*;
import java.util.Enumeration;
import java.util.Properties;
import java.util.regex.Pattern;

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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * @author <a href="mailto:zarars@gmail.com">Zarar Siddiqi</a>
 * @version $Id$
 */
public abstract class AbstractWritePropertiesMojo
    extends AbstractMojo
{

    @Parameter( defaultValue = "${project}", required = true, readonly = true )
    private MavenProject project;

    @Parameter( required = true )
    private File outputFile;

    @Parameter(required = false)
    private String exclusionRegex;

    @Parameter(required = false, defaultValue = "true")
    private boolean generateCommentHeader;

    /**
     * @param properties {@link Properties}
     * @param file {@link File}
     * @throws MojoExecutionException {@link MojoExecutionException}
     */
    protected void writeProperties( Properties properties, File file )
        throws MojoExecutionException
    {
        // Filter keys and/or the header comment
        final Properties propertiesToWrite = generateCommentHeader ? new Properties() : new PropertiesWithoutHeader();
        if (this.exclusionRegex == null) {
            propertiesToWrite.putAll(properties);
        } else {
            final Pattern exclusionPattern = Pattern.compile(exclusionRegex);
            final Enumeration<?> enumeration = properties.keys();
            while ( enumeration.hasMoreElements() )
            {
                final String key = (String) enumeration.nextElement();
                if (!exclusionPattern.matcher(key).find()) {
                    propertiesToWrite.put(key, properties.getProperty(key));
                }
            }
        }

        // Write the properties
        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream( file );
            propertiesToWrite.store( fos, "Properties" );

        }
        catch ( FileNotFoundException e )
        {
            getLog().error( "Could not create FileOutputStream: " + fos );
            throw new MojoExecutionException( e.getMessage(), e );
        }
        catch ( IOException e )
        {
            getLog().error( "Error writing properties: " + fos );
            throw new MojoExecutionException( e.getMessage(), e );
        }

        try
        {
            fos.close();
        }
        catch ( IOException e )
        {
            getLog().error( "Error closing FileOutputStream: " + fos );
            throw new MojoExecutionException( e.getMessage(), e );
        }
    }

    /**
     * @throws MojoExecutionException {@link MojoExecutionException}
     */
    protected void validateOutputFile()
        throws MojoExecutionException
    {
        if ( outputFile.isDirectory() )
        {
            throw new MojoExecutionException( "outputFile must be a file and not a directory" );
        }
        // ensure path exists
        if ( outputFile.getParentFile() != null )
        {
            outputFile.getParentFile().mkdirs();
        }
    }

    /**
     * @return {@link MavenProject}
     */
    public MavenProject getProject()
    {
        return project;
    }

    /**
     * @return {@link #outputFile}
     */
    public File getOutputFile()
    {
        return outputFile;
    }

    /**
     * @return {@link #exclusionRegex}
     */
    public String getExclusionRegex() {
        return exclusionRegex;
    }

    /**
     * @return {@link #generateCommentHeader}
     */
    public boolean getGenerateCommentHeader() {
        return generateCommentHeader;
    }

}
