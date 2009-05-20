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

import java.util.Enumeration;
import java.util.Properties;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Sets system properties.
 * 
 * @author <a href="mailto:markh@apache.org">Mark Hobson</a>
 * @version $Id$
 * @goal set-system-properties
 * @phase initialize
 */
public class SetSystemPropertiesMojo
    extends AbstractMojo
{
    // fields -----------------------------------------------------------------

    /**
     * The system properties to set.
     * 
     * @parameter
     * @required
     */
    private Properties properties;

    // Mojo methods -----------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( properties.isEmpty() )
        {
            getLog().debug( "No system properties found" );

            return;
        }

        getLog().debug( "Setting system properties:" );

        for ( Enumeration propertyNames = properties.propertyNames(); propertyNames.hasMoreElements(); )
        {
            String propertyName = propertyNames.nextElement().toString();
            String propertyValue = properties.getProperty( propertyName );

            getLog().debug( "- " + propertyName + " = " + propertyValue );

            System.setProperty( propertyName, propertyValue );
        }

        int count = properties.size();

        getLog().info( "Set " + count + " system " + ( count > 1 ? "properties" : "property" ) );
    }
}
