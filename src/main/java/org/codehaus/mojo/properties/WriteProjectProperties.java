package org.codehaus.mojo.properties;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.util.Properties;

/**
 * Writes project properties to a file
 *
 * @author <a href="mailto:zarars@gmail.com">Zarar Siddiqi</a>
 * @version $Id$
 * @goal write-project-properties
 */
public class WriteProjectProperties extends AbstractWritePropertiesMojo {

    public void execute() throws MojoExecutionException, MojoFailureException {
        validateOutputFile();
        Properties properties = project.getProperties();
        writeProperties(properties, outputFile);
    }
}
