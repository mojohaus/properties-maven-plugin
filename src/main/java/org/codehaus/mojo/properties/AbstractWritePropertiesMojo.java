package org.codehaus.mojo.properties;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * @author <a href="mailto:zarars@gmail.com">Zarar Siddiqi</a>
 * @version $Id$
 */
public abstract class AbstractWritePropertiesMojo extends AbstractMojo
{

    /**
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

    /**
     * The properties file that will be used when writing properties.
     *
     * @parameter
     * @required
     */
    protected File outputFile;

    protected void writeProperties( Properties properties, File file )
    {
        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream( file );
        }
        catch ( FileNotFoundException e )
        {
            getLog().error( "Could not create FileOutputStream: " + fos );
            e.printStackTrace();
        }
        try
        {
            properties.store( fos, "Properties" );
        }
        catch ( IOException e )
        {
            getLog().error( "Error writing properties: " + fos );
            e.printStackTrace();
        }
        try
        {
            fos.close();
        }
        catch ( IOException e )
        {
            getLog().error( "Error closing FileOutputStream: " + fos );
            e.printStackTrace();
        }
    }

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
}
