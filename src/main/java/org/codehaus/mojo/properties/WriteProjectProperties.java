package org.codehaus.mojo.properties;

/*
 * Copyright 2006 The Codehaus.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.util.Properties;

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
        Properties properties = project.getProperties();
        writeProperties( properties, outputFile );
    }
}
