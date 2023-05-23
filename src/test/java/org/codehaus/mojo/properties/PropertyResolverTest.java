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

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;
import org.junit.Test;
import org.apache.maven.plugin.MojoFailureException;
import java.util.Properties;

/**
 * Tests the support class that produces concrete values from a set of properties.
 */
public class PropertyResolverTest
{
    private final PropertyResolver resolver = new PropertyResolver();

    @Test
    public void validPlaceholderIsResolved()
    {
        Properties properties = new Properties();
        properties.setProperty( "p1", "${p2}" );
        properties.setProperty( "p2", "value" );

        String value1 = resolver.getPropertyValue( "p1", properties, new Properties() );
        String value2 = resolver.getPropertyValue( "p2", properties, new Properties() );

        assertEquals( "value", value1 );
        assertEquals( "value", value2 );
    }

    @Test
    public void unknownPlaceholderIsLeftAsIs()
    {
        Properties properties = new Properties();
        properties.setProperty( "p1", "${p2}" );
        properties.setProperty( "p2", "value" );
        properties.setProperty( "p3", "${unknown}" );

        String value1 = resolver.getPropertyValue( "p1", properties, new Properties() );
        String value2 = resolver.getPropertyValue( "p2", properties, new Properties() );
        String value3 = resolver.getPropertyValue( "p3", properties, new Properties() );

        assertEquals( "value", value1 );
        assertEquals( "value", value2 );
        assertEquals( "${unknown}", value3 );
    }

    @Test
    public void multipleValuesAreResolved()
    {
        Properties properties = new Properties();
        properties.setProperty( "hostname", "localhost" );
        properties.setProperty( "port", "8080" );
        properties.setProperty( "base.url", "http://${hostname}:${port}/" );

        String value = resolver.getPropertyValue( "base.url", properties, new Properties() );

        assertEquals( "http://localhost:8080/", value );
    }

    @Test
    public void malformedPlaceholderIsLeftAsIs()
    {
        Properties properties = new Properties();
        properties.setProperty( "p1", "${p2}" );
        properties.setProperty( "p2", "value" );
        properties.setProperty( "p4", "${malformed" );

        String value1 = resolver.getPropertyValue( "p1", properties, new Properties() );
        String value2 = resolver.getPropertyValue( "p2", properties, new Properties() );
        String value4 = resolver.getPropertyValue( "p4", properties, new Properties() );

        assertEquals( "value", value1 );
        assertEquals( "value", value2 );
        assertEquals( "${malformed", value4 );
    }

    @Test
    public void propertyDefinedAsItselfIsIllegal()
    {
        Properties properties = new Properties();
        properties.setProperty( "p1", "${p2}" );
        properties.setProperty( "p2", "value" );
        properties.setProperty( "p5", "${p5}" );
        properties.setProperty( "p6", "${p7}" );
        properties.setProperty( "p7", "${p6}" );

        String value1 = resolver.getPropertyValue( "p1", properties, new Properties() );
        String value2 = resolver.getPropertyValue( "p2", properties, new Properties() );
        String value5 = null;
        try
        {
            value5 = resolver.getPropertyValue( "p5", properties, new Properties() );
            fail();
        }
        catch ( IllegalArgumentException e )
        {
            assertThat( e.getMessage(), containsString( "p5" ) );
        }
        String value6 = null;
        try
        {
            value6 = resolver.getPropertyValue( "p6", properties, new Properties() );
            fail();
        }
        catch ( IllegalArgumentException e )
        {
            assertThat( e.getMessage(), containsString( "p7" ) );
        }

        assertEquals( "value", value1 );
        assertEquals( "value", value2 );
        assertNull( value5 );
        assertNull( value6 );
    }

    @Test
    public void valueIsObtainedFromSystemProperty()
    {
        Properties saved = System.getProperties();
        System.setProperty( "system.property", "system.value" );

        Properties properties = new Properties();
        properties.setProperty( "p1", "${system.property}" );

        String value = resolver.getPropertyValue( "p1", properties, new Properties() );

        try
        {
            assertEquals( "system.value", value );
        }
        finally
        {
            System.setProperties( saved );
        }
    }

    @Test
    public void valueIsObtainedFromEnvironmentProperty()
    {
        Properties environment = new Properties();
        environment.setProperty( "PROPERTY", "env.value" );

        Properties properties = new Properties();
        properties.setProperty( "p1", "${env.PROPERTY}" );

        String value = resolver.getPropertyValue( "p1", properties, environment );

        assertEquals( "env.value", value );
    }

    @Test
    public void missingPropertyIsTolerated()
    {
        assertEquals( "", resolver.getPropertyValue( "non-existent", new Properties(), null ) );
    }

    public void testDefaultValueForUnresolvedPropertyWithEnabledFlag()
    {
        Properties properties = new Properties();
        properties.setProperty("p1", "${unknown:}");
        properties.setProperty("p2", "${unknown:defaultValue}");
        properties.setProperty("p3", "http://${uhost:localhost}:${uport:8080}");
        properties.setProperty("p4", "http://${host:localhost}:${port:8080}");
        properties.setProperty("p5", "${unknown:${fallback}}");
        properties.setProperty("p6", "${unknown:${double.unknown}}");
        properties.setProperty("p7", "${unknown:with space}");
        properties.setProperty("p8", "${unknown:with extra :}");
        properties.setProperty("p9", "${malformed:defVal");
        properties.setProperty("p10", "${malformed:with space");
        properties.setProperty("p11", "${malformed:with extra :");
        properties.setProperty("p12", "${unknown::}");
        properties.setProperty("p13", "${unknown:  }");

        properties.setProperty("host", "example.com");
        properties.setProperty("port", "9090");
        properties.setProperty("fallback", "fallback value");


        String value1 = resolver.getPropertyValue("p1", properties, new Properties(), true);
        String value2 = resolver.getPropertyValue("p2", properties, new Properties(), true);
        String value3 = resolver.getPropertyValue("p3", properties, new Properties(), true);
        String value4 = resolver.getPropertyValue("p4", properties, new Properties(), true);
        String value5 = resolver.getPropertyValue("p5", properties, new Properties(), true);
        String value6 = resolver.getPropertyValue("p6", properties, new Properties(), true);
        String value7 = resolver.getPropertyValue("p7", properties, new Properties(), true);
        String value8 = resolver.getPropertyValue("p8", properties, new Properties(), true);
        String value9 = resolver.getPropertyValue("p9", properties, new Properties(), true);
        String value10 = resolver.getPropertyValue("p10", properties, new Properties(), true);
        String value11 = resolver.getPropertyValue("p11", properties, new Properties(), true);
        String value12 = resolver.getPropertyValue("p12", properties, new Properties(), true);
        String value13 = resolver.getPropertyValue("p13", properties, new Properties(), true);

        assertEquals("${unknown}", value1);
        assertEquals("defaultValue", value2);
        assertEquals("http://localhost:8080", value3);
        assertEquals("http://example.com:9090", value4);
        assertEquals("fallback value", value5);
        assertEquals("${double.unknown}", value6);
        assertEquals("with space", value7);
        assertEquals("with extra :", value8);
        assertEquals("${malformed:defVal", value9);
        assertEquals("${malformed:with space", value10);
        assertEquals("${malformed:with extra :", value11);
        assertEquals(":", value12);
        assertEquals("  ", value13);
    }

    /**
     * with the flag disabled (default behavior) nothing gets replaced
     * ':' is treated as a regular character and part of the property name
     */
    public void testDefaultValueForUnresolvedPropertyWithDisabledFlag()
    {
        Properties properties = new Properties();
        properties.setProperty("p1", "${unknown:}");
        properties.setProperty("p2", "${unknown:defaultValue}");
        properties.setProperty("p3", "http://${uhost:localhost}:${uport:8080}");
        properties.setProperty("p4", "http://${host:localhost}:${port:8080}");
        properties.setProperty("p5", "${unknown:${fallback}}");
        properties.setProperty("p6", "${unknown:${double.unknown}}");
        properties.setProperty("p7", "${unknown:with space}");
        properties.setProperty("p8", "${unknown:with extra :}");
        properties.setProperty("p9", "${malformed:defVal");
        properties.setProperty("p10", "${malformed:with space");
        properties.setProperty("p11", "${malformed:with extra :");
        properties.setProperty("p12", "${unknown::}");
        properties.setProperty("p13", "${unknown:  }");

        properties.setProperty("host", "example.com");
        properties.setProperty("port", "9090");
        properties.setProperty("fallback", "fallback value");


        String value1 = resolver.getPropertyValue("p1", properties, new Properties());
        String value2 = resolver.getPropertyValue("p2", properties, new Properties());
        String value3 = resolver.getPropertyValue("p3", properties, new Properties());
        String value4 = resolver.getPropertyValue("p4", properties, new Properties());
        String value5 = resolver.getPropertyValue("p5", properties, new Properties());
        String value6 = resolver.getPropertyValue("p6", properties, new Properties());
        String value7 = resolver.getPropertyValue("p7", properties, new Properties());
        String value8 = resolver.getPropertyValue("p8", properties, new Properties());
        String value9 = resolver.getPropertyValue("p9", properties, new Properties());
        String value10 = resolver.getPropertyValue("p10", properties, new Properties());
        String value11 = resolver.getPropertyValue("p11", properties, new Properties());
        String value12 = resolver.getPropertyValue("p12", properties, new Properties());
        String value13 = resolver.getPropertyValue("p13", properties, new Properties());

        assertEquals("${unknown:}", value1);
        assertEquals("${unknown:defaultValue}", value2);
        assertEquals("http://${uhost:localhost}:${uport:8080}", value3);
        assertEquals("http://${host:localhost}:${port:8080}", value4);
        assertEquals("${unknown:${fallback}}", value5);
        assertEquals("${unknown:${double.unknown}}", value6);
        assertEquals("${unknown:with space}", value7);
        assertEquals("${unknown:with extra :}", value8);
        assertEquals("${malformed:defVal", value9);
        assertEquals("${malformed:with space", value10);
        assertEquals("${malformed:with extra :", value11);
        assertEquals("${unknown::}", value12);
        assertEquals("${unknown:  }", value13);
    }
}
