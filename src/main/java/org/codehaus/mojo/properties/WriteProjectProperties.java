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

import java.util.Properties;
import java.util.Enumeration;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Writes project properties to a file.
 *
 * @author <a href="mailto:zarars@gmail.com">Zarar Siddiqi</a>
 * @version $Id$
 * @goal write-project-properties
 */
public class WriteProjectProperties extends AbstractWritePropertiesMojo
{
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        validateOutputFile();
        Properties projProperties = new Properties();
        projProperties.putAll( project.getProperties() );
        
        Properties systemProperties = System.getProperties();
        
        //allow system properties to over write key/value found in maven properties
        Enumeration enumeration = systemProperties.keys();
        while ( enumeration.hasMoreElements() )
        {
            String key = (String) enumeration.nextElement();
            String value = systemProperties.getProperty( key );
            if ( projProperties.get( key ) != null )
            {
                projProperties.put( key, value );
            }
            
        }
        
        writeProperties( projProperties, outputFile );
    }
}
