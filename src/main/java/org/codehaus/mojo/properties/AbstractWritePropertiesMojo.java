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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.io.BufferedReader;

/**
 * @author <a href="mailto:zarars@gmail.com">Zarar Siddiqi</a>
 */
public abstract class AbstractWritePropertiesMojo
    extends AbstractMojo
{
    /**
     * Default encoding for the output file. Package private for testing.
     */
    static final String DEFAULT_ENCODING = "ISO-8859-1";

    @Parameter( defaultValue = "${project}", required = true, readonly = true )
    private MavenProject project;

    @Parameter( required = true, property = "properties.outputFile" )
    private File outputFile;

    /**
     * The encoding to use when writing the properties file.
     */
    @Parameter( required = false, defaultValue = DEFAULT_ENCODING )
    private String encoding = DEFAULT_ENCODING;

    /**
     * Default scope for test access.
     *
     * @param project The test project.
     */
    void setProject( MavenProject project )
    {
        this.project = project;
    }

    /**
     * Default scope for test access.
     *
     * @param encoding to write the output file in
     */
    void setEncoding( String encoding )
    {
        this.encoding = encoding;
    }

    /**
     * Default scope for test access
     *
     * @param outputFile the outputFile to set
     */
    void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }

    /**
     * @param properties {@link Properties}
     * @param file {@link File}
     * @throws MojoExecutionException {@link MojoExecutionException}
     */
    protected void writeProperties( Properties properties, File file )
        throws MojoExecutionException
    {
        getLog().debug( String.format( "Writing properties to %s using encoding %s", file.toString(),  this.encoding ) );

        try
        {
            storeWithoutTimestamp( properties, file, "Properties" );
        }
        catch ( FileNotFoundException e )
        {
            getLog().error( "Could not create FileOutputStream: " + file );
            throw new MojoExecutionException( e.getMessage(), e );
        }
        catch ( IOException e )
        {
            getLog().error( "Error writing properties: " + file );
            throw new MojoExecutionException( e.getMessage(), e );
        }
    }

    // https://github.com/apache/maven-archiver/blob/master/src/main/java/org/apache/maven/archiver/PomPropertiesUtil.java#L81
    private void storeWithoutTimestamp( Properties properties, File outputFile, String comments )
        throws IOException
    {
        try ( PrintWriter pw = new PrintWriter( outputFile, this.encoding ); StringWriter sw = new StringWriter() )
        {
            properties.store( sw, comments );
            comments = '#' + comments;

            List<String> lines = new ArrayList<>();
            try ( BufferedReader r = new BufferedReader( new StringReader( sw.toString() ) ) )
            {
                String line;
                while ( ( line = r.readLine() ) != null )
                {
                    if ( !line.startsWith( "#" ) || line.equals( comments ) )
                    {
                        lines.add( line );
                    }
                }
            }

            Collections.sort( lines );
            for ( String l : lines )
            {
                pw.println( l );
            }
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
     * @throws MojoExecutionException {@link MojoExecutionException}
     */
    protected void validateEncoding()
        throws MojoExecutionException
    {
        try
        {
            Charset.forName(this.encoding);
        }
        catch(IllegalArgumentException e)
        {
            throw new MojoExecutionException(String.format("Invalid encoding '%s'", this.encoding), e);
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

}
