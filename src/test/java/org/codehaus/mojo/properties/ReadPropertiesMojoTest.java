package org.codehaus.mojo.properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;

public class ReadPropertiesMojoTest
{
    private static final String NEW_LINE = System.getProperty( "line.separator" );

    private static final Map<String, String> ISO_8859_REPRESENTABLE = Map.of( "test.property1", "value1®",
                                                                              "test.property2", "value2" );

    private static final Map<String, String> ISO_8859_NOT_REPRESENTABLE = Map.of ( "test.property3", "value3™",
                                                                                   "test.property4", "κόσμε" );

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
        final Map<String, String> propMap = new HashMap<>();
        propMap.putAll( ISO_8859_REPRESENTABLE );
        propMap.putAll( ISO_8859_NOT_REPRESENTABLE );

        final String encoding = ReadPropertiesMojo.DEFAULT_ENCODING;
        File referenceFile = getPropertyFileForTesting( encoding, propMap );
        try ( FileInputStream fileStream = new FileInputStream( referenceFile );
              InputStreamReader fr = new InputStreamReader( fileStream, encoding ) )
        {
            // load properties directly for comparison later
            Properties testProperties = new Properties();
            testProperties.load( fr );

            // do the work
            readPropertiesMojo.setFiles( new File[] {getPropertyFileForTesting( encoding, propMap )} );
            readPropertiesMojo.execute();

            // check results
            Properties projectProperties = projectStub.getProperties();
            assertNotNull( projectProperties );
            // it should not be empty
            assertNotEquals( 0, projectProperties.size() );

            // we are not adding prefix, so properties should be same as in file
            assertEquals( testProperties.size(), projectProperties.size() );
            assertEquals( testProperties, projectProperties );

            // strings which are representable in ISO-8859-1 should match the source data
            for ( String sourceKey : ISO_8859_REPRESENTABLE.keySet() )
            {
                assertEquals( ISO_8859_REPRESENTABLE.get( sourceKey ), projectProperties.getProperty( sourceKey ) );
            }

            // strings which are not representable in ISO-8859-1 underwent a conversion which lost data
            for ( String sourceKey : ISO_8859_NOT_REPRESENTABLE.keySet() )
            {
                String sourceValue = ISO_8859_NOT_REPRESENTABLE.get( sourceKey );
                String converted = new String( sourceValue.getBytes( encoding ), StandardCharsets.UTF_8 );
                String propVal = projectProperties.getProperty( sourceKey );
                assertNotEquals( sourceValue, propVal );
                assertEquals( converted, propVal );
            }
        }
    }

    @Test
    public void readPropertiesWithKeyprefix() throws Exception
    {
        String keyPrefix = "testkey-prefix.";
        final String encoding = ReadPropertiesMojo.DEFAULT_ENCODING;

        final Map<String, String> propMap = new HashMap<>();
        propMap.putAll( ISO_8859_REPRESENTABLE );
        propMap.putAll( ISO_8859_NOT_REPRESENTABLE );

        try ( FileInputStream fs1 = new FileInputStream( getPropertyFileForTesting( keyPrefix, encoding, propMap ) );
              FileInputStream fs2 = new FileInputStream( getPropertyFileForTesting( encoding, propMap ) );
              InputStreamReader fr1 = new InputStreamReader( fs1, encoding );
              InputStreamReader fr2 = new InputStreamReader( fs2, encoding ) )
        {
            Properties testPropertiesWithoutPrefix = new Properties();
            testPropertiesWithoutPrefix.load( fr2 );

            // do the work
            readPropertiesMojo.setKeyPrefix( keyPrefix );
            readPropertiesMojo.setFiles( new File[] {getPropertyFileForTesting( encoding, propMap )} );
            readPropertiesMojo.execute();

            // load properties directly and add prefix for comparison later
            Properties testPropertiesPrefix = new Properties();
            testPropertiesPrefix.load( fr1 );

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

            // strings which are representable in ISO-8859-1 should match the source data
            for ( String sourceKey : ISO_8859_REPRESENTABLE.keySet() )
            {
                assertEquals( ISO_8859_REPRESENTABLE.get( sourceKey ), projectProperties.getProperty( keyPrefix + sourceKey ) );
            }

            // strings which are not representable in ISO-8859-1 underwent a conversion which lost data
            for ( String sourceKey : ISO_8859_NOT_REPRESENTABLE.keySet() )
            {
                String sourceValue = ISO_8859_NOT_REPRESENTABLE.get( sourceKey );
                String converted = new String( sourceValue.getBytes( encoding ), StandardCharsets.UTF_8 );
                String propVal = projectProperties.getProperty( keyPrefix + sourceKey );
                assertNotEquals( sourceValue, propVal );
                assertEquals( converted, propVal );
            }
        }
    }

    @Test
    public void readPropertiesWithEncoding() throws Exception
    {
        final Map<String, String> propMap = new HashMap<>();
        propMap.putAll( ISO_8859_REPRESENTABLE );
        propMap.putAll( ISO_8859_NOT_REPRESENTABLE );

        final String encoding = StandardCharsets.UTF_8.name();
        File referenceFile = getPropertyFileForTesting( encoding, propMap );
        try ( FileInputStream fileStream = new FileInputStream( referenceFile );
              InputStreamReader fr = new InputStreamReader( fileStream, encoding ) )
        {
            // load properties directly for comparison later
            Properties testProperties = new Properties();
            testProperties.load( fr );

            // do the work
            readPropertiesMojo.setFiles( new File[] {getPropertyFileForTesting( encoding, propMap )} );
            readPropertiesMojo.setEncoding( encoding );
            readPropertiesMojo.execute();

            // check results
            Properties projectProperties = projectStub.getProperties();
            assertNotNull( projectProperties );
            // it should not be empty
            assertNotEquals( 0, projectProperties.size() );

            // we are not adding prefix, so properties should be same as in file
            assertEquals( testProperties.size(), projectProperties.size() );
            assertEquals( testProperties, projectProperties );

            // all test values are representable in UTF-8 and should match original source data
            for ( String sourceKey : propMap.keySet() )
            {
                assertEquals( propMap.get( sourceKey ), projectProperties.getProperty( sourceKey ) );
            }
        }
    }

    @Test
    public void testInvalidEncoding() throws Exception
    {
        readPropertiesMojo.setFiles( new File[] {getPropertyFileForTesting( StandardCharsets.UTF_8.name(), ISO_8859_REPRESENTABLE )} );
        readPropertiesMojo.setEncoding( "invalid-encoding" );
        MojoExecutionException thrown = assertThrows( MojoExecutionException.class, () -> readPropertiesMojo.execute() );
        assertEquals( thrown.getMessage(), "Invalid encoding 'invalid-encoding'" );
    }

    private File getPropertyFileForTesting( String encoding, Map<String, String> properties ) throws IOException
    {
        return getPropertyFileForTesting( null, encoding, properties );
    }

    private File getPropertyFileForTesting( String keyPrefix, String encoding, Map<String, String> properties ) throws IOException
    {
        File f = File.createTempFile( "prop-test", ".properties" );
        f.deleteOnExit();
        try (FileOutputStream fileStream = new FileOutputStream( f ); //
             OutputStreamWriter writer = new OutputStreamWriter( fileStream, encoding ) )
        {
            String prefix = keyPrefix;
            if ( prefix == null )
            {
                prefix = "";
            }
            for ( Map.Entry<String, String> entry : properties.entrySet() )
            {
                writer.write( prefix + entry.getKey() + "=" + entry.getValue() + NEW_LINE );
            }
            writer.flush();
        }
        return f;
    }
}
