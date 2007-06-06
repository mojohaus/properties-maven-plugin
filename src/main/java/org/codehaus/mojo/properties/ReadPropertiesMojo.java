package org.codehaus.mojo.properties;


import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.cli.CommandLineUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Reads property files as Project properties
 *
 * @author <a href="mailto:zarars@gmail.com">Zarar Siddiqi</a>
 * @version $Id$
 * @goal read-project-properties
 */
public class ReadPropertiesMojo
    extends AbstractMojo
{

    /**
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
     private MavenProject project;

    /**
     * @parameter
     * @required
     */
    private File[] files;

    public void execute()
        throws MojoExecutionException {
        Properties projectProperties = new Properties();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            try {
                FileInputStream stream = new FileInputStream(file);
                if (getLog().isDebugEnabled()) {
					getLog().debug("Loading property file: " + file);
				}
                projectProperties = project.getProperties();
                try {
                    projectProperties.load(stream);
                } finally {
                    if( stream != null ) {
                        stream.close();
                    }
                }
            } catch (Exception e) {
                throw new MojoExecutionException("Error: ", e);
            }
        }
        boolean useEnvVariables = false;
        for( Enumeration n = projectProperties.propertyNames(); n.hasMoreElements(); ) {
            String k = (String) n.nextElement();
            String p = (String) projectProperties.get(k);
            if( p.indexOf("${env.") != -1) {
                useEnvVariables = true;
                break;
            }
        }
        Properties environment = null;
        if( useEnvVariables ) {
            try {
                environment = CommandLineUtils.getSystemEnvVars();
            } catch (IOException e) {
                throw new MojoExecutionException("Error: ", e);
            }
        }
        for( Enumeration n = projectProperties.propertyNames(); n.hasMoreElements(); ) {
            String k = (String) n.nextElement();
            projectProperties.setProperty(k, getPropertyValue(k, projectProperties, environment));
        }
    }

    /**
     * Retrieves a property value, replacing values like ${token}
     * using the Properties to look them up.
     * Shamelessly adapted from:
     * http://maven.apache.org/plugins/maven-war-plugin/xref/org/apache/maven/plugin/war/PropertyUtils.html
     *
     * It will leave unresolved properties alone, trying for System
     * properties, and environment variables and implements reparsing
     * (in the case that the value of a property contains a key), and will
     * not loop endlessly on a pair like test = ${test}
     *
     * @param k property key
     * @param p project properties
     * @param environment environment variables
     * @return resolved property value
     */
    private String getPropertyValue(String k, Properties p, Properties environment) {
        String v = p.getProperty(k);
        String ret = "";
        int idx, idx2;

        while( (idx = v.indexOf("${")) >= 0 ) {
            // append prefix to result
            ret += v.substring(0, idx);

            // strip prefix from original
            v = v.substring(idx + 2);

            // if no matching } then bail
            if( (idx2 = v.indexOf("}")) < 0 ) {
                break;
            }

            // strip out the key and resolve it
            // resolve the key/value for the ${statement}
            String nk = v.substring(0, idx2);
            v = v.substring(idx2 + 1);
            String nv = p.getProperty(nk);

            // try global environment
            if( nv == null ) {
                nv = System.getProperty(nk);
            }

            // try environment variable
            if( nv == null && nk.startsWith("env.") && environment != null) {
                nv = environment.getProperty(nk.substring(4));
            }

            // if the key cannot be resolved,
            // leave it alone ( and don't parse again )
            // else prefix the original string with the
            // resolved property ( so it can be parsed further )
            // taking recursion into account.
            if( nv == null || nv.equals(nk) ) {
                ret += "${" + nk + "}";
            } else {
                v = nv + v;
            }
        }
        return ret + v;
    }
}

