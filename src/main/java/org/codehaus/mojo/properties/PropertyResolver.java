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

import java.util.Properties;

class PropertyResolver
{

    /**
     * Retrieves a property value, replacing values like ${token} using the Properties to look them up. Shamelessly
     * adapted from:
     * http://maven.apache.org/plugins/maven-war-plugin/xref/org/apache/maven/plugin/war/PropertyUtils.html It will
     * leave unresolved properties alone, trying for System properties, and environment variables and implements
     * reparsing (in the case that the value of a property contains a key), and will not loop endlessly on a pair like
     * test = ${test}
     *
     * @param key property key
     * @param properties project properties
     * @param environment environment variables
     * @return resolved property value
     * @throws IllegalArgumentException when properties are circularly defined
     */
    public String getPropertyValue( String key, Properties properties, Properties environment ) {
        return getPropertyValue(key, properties, environment, false);
    }

    /**
     * Same as the previous method. Accepts an extra flag to indicate whether default values should be
     * processed within property placeholders or not.
     *
     * @param key           property key
     * @param properties           project properties
     * @param environment environment variables
     * @param useDefaultValues    process default values flag
     * @return resolved property value
     * @throws IllegalArgumentException when properties are circularly defined
     */
    public String getPropertyValue( String key, Properties properties, Properties environment, boolean useDefaultValues )
    {
        String value = properties.getProperty( key );

        ExpansionBuffer buffer;
        if ( useDefaultValues ) {
            buffer = new DefaultValuesAwareExpansionBufferImpl(value);
        } else {
            buffer = new ExpansionBufferImpl( value );
        }

        CircularDefinitionPreventer circularDefinitionPreventer =
            new CircularDefinitionPreventer().visited( key, value );

        while ( buffer.hasMoreLegalPlaceholders() )
        {
            KeyAndDefaultValue kv = buffer.extractPropertyKeyAndDefaultValue();
            String newKey = kv.getKey();
            String newValue = fromPropertiesThenSystemThenEnvironment( newKey, kv.getDefaultValue(), properties, environment );

            circularDefinitionPreventer.visited( newKey, newValue );

            buffer.add( newKey, newValue );
        }

        return buffer.toString();
    }

    private String fromPropertiesThenSystemThenEnvironment( String key, String defaultValue, Properties properties, Properties environment )
    {
        String value = properties.getProperty( key );

        // try global environment
        if ( value == null )
        {
            value = System.getProperty( key );
        }

        // try environment variable
        if ( value == null && key.startsWith( "env." ) && environment != null )
        {
            value = environment.getProperty( key.substring( 4 ) );
        }

        // try default value
        if ( value == null )
        {
            value = defaultValue;
        }

        return value;
    }
}
