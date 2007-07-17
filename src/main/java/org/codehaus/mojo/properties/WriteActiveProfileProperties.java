package org.codehaus.mojo.properties;

/*
 */

import org.apache.maven.model.Profile;
import org.apache.maven.plugin.MojoExecutionException;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * Writes properties of all active profiles to a file.
 * 
 * @author <a href="mailto:zarars@gmail.com">Zarar Siddiqi</a>
 * @version $Id$
 * @goal write-active-profile-properties
 */
public class WriteActiveProfileProperties
    extends AbstractWritePropertiesMojo
{

    public void execute()
        throws MojoExecutionException {
        validateOutputFile();
        List list = project.getActiveProfiles();
        if (getLog().isInfoEnabled()) {
            getLog().info(list.size() + " profile(s) active");
        }
        Properties properties = new Properties();
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            Profile profile = (Profile) iter.next();
            if (profile.getProperties() != null) {
                properties.putAll(profile.getProperties());
            }
        }

        writeProperties(properties, outputFile);
    }
}

