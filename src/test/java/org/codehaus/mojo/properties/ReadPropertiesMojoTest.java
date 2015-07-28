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

import junit.framework.TestCase;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import java.io.IOException;
import java.util.Properties;


public class ReadPropertiesMojoTest extends TestCase {
    ReadPropertiesMojo mojo = new ReadPropertiesMojo();

    public void testDefaultValueForUnresolvedPropertyWithEnabledFlag() throws MojoFailureException, MojoExecutionException, IOException {
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

        Model model = new Model();
        model.setProperties(properties);
        MavenProject project = new MavenProject(model);
        mojo.setProject(project);
        mojo.setUseDefaultValues(true);
        mojo.execute();

        Properties processed = mojo.getProject().getProperties();

        String value1 = processed.getProperty("p1");
        String value2 = processed.getProperty("p2");
        String value3 = processed.getProperty("p3");
        String value4 = processed.getProperty("p4");
        String value5 = processed.getProperty("p5");
        String value6 = processed.getProperty("p6");
        String value7 = processed.getProperty("p7");
        String value8 = processed.getProperty("p8");
        String value9 = processed.getProperty("p9");
        String value10 = processed.getProperty("p10");
        String value11 = processed.getProperty("p11");
        String value12 = processed.getProperty("p12");
        String value13 = processed.getProperty("p13");

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
    public void testDefaultValueForUnresolvedPropertyWithDisabledFlag() throws MojoFailureException, MojoExecutionException, IOException {
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

        Model model = new Model();
        model.setProperties(properties);
        MavenProject project = new MavenProject(model);
        mojo.setProject(project);
        mojo.execute();

        Properties processed = mojo.getProject().getProperties();

        String value1 = processed.getProperty("p1");
        String value2 = processed.getProperty("p2");
        String value3 = processed.getProperty("p3");
        String value4 = processed.getProperty("p4");
        String value5 = processed.getProperty("p5");
        String value6 = processed.getProperty("p6");
        String value7 = processed.getProperty("p7");
        String value8 = processed.getProperty("p8");
        String value9 = processed.getProperty("p9");
        String value10 = processed.getProperty("p10");
        String value11 = processed.getProperty("p11");
        String value12 = processed.getProperty("p12");
        String value13 = processed.getProperty("p13");

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
