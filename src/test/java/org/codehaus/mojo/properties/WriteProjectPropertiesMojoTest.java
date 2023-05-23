package org.codehaus.mojo.properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThrows;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;

public class WriteProjectPropertiesMojoTest
{
    private static final Map<String, String> ISO_8859_REPRESENTABLE = Map.of("test.property1", "value1®",
                                                                             "test.property2", "value2");

    private static final Map<String, String> ISO_8859_NOT_REPRESENTABLE = Map.of ("test.property3", "value3™",
                                                                                  "test.property4", "κόσμε");

    private MavenProject projectStub;
    private WriteProjectProperties writePropertiesMojo;

    @Before
    public void setUp()
    {
        projectStub = new MavenProject();
        writePropertiesMojo = new WriteProjectProperties();
        writePropertiesMojo.setProject( projectStub );
    }

    @Test
    public void writePropertiesDefaultEncoding() throws Exception
    {
        final Map<String, String> propMap = new HashMap<>();
        propMap.putAll( ISO_8859_REPRESENTABLE );
        propMap.putAll( ISO_8859_NOT_REPRESENTABLE );

        final String encoding = AbstractWritePropertiesMojo.DEFAULT_ENCODING;

        File outputFile = File.createTempFile( "prop-test", ".properties" );
        outputFile.deleteOnExit();
        writePropertiesMojo.setOutputFile( outputFile );

        Properties projectProperties = projectStub.getProperties();
        projectProperties.putAll( propMap );

        writePropertiesMojo.execute();

        try ( FileInputStream fileStream = new FileInputStream( outputFile );
              InputStreamReader fr = new InputStreamReader( fileStream, encoding ) )
        {
            // load the properties we just saved
            Properties savedProperties = new Properties();
            savedProperties.load( fr );

            // it should not be empty
            assertNotEquals( 0, savedProperties.size() );

            // we are not adding prefix, so properties should be same as in file
            assertEquals( projectProperties.size(), savedProperties.size() );

            // strings which are representable in ISO-8859-1 should match the source data
            for ( String sourceKey : ISO_8859_REPRESENTABLE.keySet() )
            {
                assertEquals( ISO_8859_REPRESENTABLE.get( sourceKey ), savedProperties.getProperty( sourceKey ) );
            }

            // strings which are not representable in ISO-8859-1 underwent a conversion which lost data
            for ( String sourceKey : ISO_8859_NOT_REPRESENTABLE.keySet() )
            {
                String sourceValue = ISO_8859_NOT_REPRESENTABLE.get( sourceKey );
                String converted = new String( sourceValue.getBytes( encoding ), StandardCharsets.UTF_8 );
                String propVal = savedProperties.getProperty( sourceKey );
                assertNotEquals( sourceValue, propVal );
                assertEquals( converted, propVal );
            }
        }
    }

    @Test
    public void writePropertiesUTF8() throws Exception
    {
        final Map<String, String> propMap = new HashMap<>();
        propMap.putAll( ISO_8859_REPRESENTABLE );
        propMap.putAll( ISO_8859_NOT_REPRESENTABLE );

        final String encoding = StandardCharsets.UTF_8.name();

        File outputFile = File.createTempFile( "prop-test", ".properties" );
        outputFile.deleteOnExit();
        writePropertiesMojo.setOutputFile( outputFile );

        Properties projectProperties = projectStub.getProperties();
        projectProperties.putAll( propMap );

        writePropertiesMojo.setEncoding( encoding );
        writePropertiesMojo.execute();

        try ( FileInputStream fileStream = new FileInputStream( outputFile );
              InputStreamReader fr = new InputStreamReader( fileStream, encoding ) )
        {
            // load the properties we just saved
            Properties savedProperties = new Properties();
            savedProperties.load( fr );

            // it should not be empty
            assertNotEquals( 0, savedProperties.size() );

            // we are not adding prefix, so properties should be same as in file
            assertEquals( projectProperties.size(), savedProperties.size() );

            // all test data is representable in UTF8, all should match source data
            for ( String sourceKey : propMap.keySet() )
            {
                assertEquals( propMap.get( sourceKey ), savedProperties.getProperty( sourceKey ) );
            }
        }
    }

    @Test
    public void testInvalidEncoding() throws Exception
    {
        final Map<String, String> propMap = new HashMap<>();
        propMap.putAll(ISO_8859_REPRESENTABLE);

        File outputFile = File.createTempFile( "prop-test", ".properties" );
        outputFile.deleteOnExit();
        writePropertiesMojo.setOutputFile( outputFile );
        writePropertiesMojo.setEncoding( "invalid-encoding" );
        MojoExecutionException thrown = assertThrows( MojoExecutionException.class, () -> writePropertiesMojo.execute() );
        assertEquals( thrown.getMessage(), "Invalid encoding 'invalid-encoding'" );
    }
}
