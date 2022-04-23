package org.codehaus.mojo.properties;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

public class ReadPropertiesMojoTest
{
    private static final String NEW_LINE = System.getProperty( "line.separator" );

    private MavenProject projectStub;
    private ReadPropertiesMojo readPropertiesMojo;

    @Before
    public void setUp()
    {
        projectStub = new MavenProject();
        readPropertiesMojo = new ReadPropertiesMojo();
        readPropertiesMojo.setProject( projectStub );
    }

    @Test
    public void readPropertiesWithoutKeyprefix() throws Exception
    {
        try ( FileReader fr = new FileReader( getPropertyFileForTesting() ) )
        {
            // load properties directly for comparison later
            Properties testProperties = new Properties();
            testProperties.load( fr );

            // do the work
            readPropertiesMojo.setFiles( new File[] {getPropertyFileForTesting()} );
            readPropertiesMojo.execute();

            // check results
            Properties projectProperties = projectStub.getProperties();
            assertNotNull( projectProperties );
            // it should not be empty
            assertNotEquals( 0, projectProperties.size() );

            // we are not adding prefix, so properties should be same as in file
            assertEquals( testProperties.size(), projectProperties.size() );
            assertEquals( testProperties, projectProperties );
        }
    }

    @Test
    public void readPropertiesWithKeyprefix() throws Exception
    {
        String keyPrefix = "testkey-prefix.";

        try ( FileReader fs1 = new FileReader( getPropertyFileForTesting( keyPrefix ) );
              FileReader fs2 = new FileReader( getPropertyFileForTesting() ) )
        {
            Properties testPropertiesWithoutPrefix = new Properties();
            testPropertiesWithoutPrefix.load( fs2 );

            // do the work
            readPropertiesMojo.setKeyPrefix( keyPrefix );
            readPropertiesMojo.setFiles( new File[] {getPropertyFileForTesting()} );
            readPropertiesMojo.execute();

            // load properties directly and add prefix for comparison later
            Properties testPropertiesPrefix = new Properties();
            testPropertiesPrefix.load( fs1 );

            // check results
            Properties projectProperties = projectStub.getProperties();
            assertNotNull( projectProperties );
            // it should not be empty
            assertNotEquals( 0, projectProperties.size() );

            // we are adding prefix, so prefix properties should be same as in projectProperties
            assertEquals( testPropertiesPrefix.size(), projectProperties.size() );
            assertEquals( testPropertiesPrefix, projectProperties );

            // properties with and without prefix shouldn't be same
            assertNotEquals( testPropertiesPrefix, testPropertiesWithoutPrefix );
            assertNotEquals( testPropertiesWithoutPrefix, projectProperties );
        }
    }

    private File getPropertyFileForTesting() throws IOException
    {
        return getPropertyFileForTesting( null );
    }

    private File getPropertyFileForTesting( String keyPrefix ) throws IOException
    {
        File f = File.createTempFile( "prop-test", ".properties" );
        f.deleteOnExit();
        FileWriter writer = new FileWriter( f );
        String prefix = keyPrefix;
        if ( prefix == null )
        {
            prefix = "";
        }
        try
        {
            writer.write( prefix + "test.property1=value1" + NEW_LINE );
            writer.write( prefix + "test.property2=value2" + NEW_LINE );
            writer.write( prefix + "test.property3=value3" + NEW_LINE );
            writer.flush();
        }
        finally
        {
            writer.close();
        }
        return f;
    }
}
