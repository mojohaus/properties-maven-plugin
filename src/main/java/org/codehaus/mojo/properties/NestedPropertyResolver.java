package org.codehaus.mojo.properties;

import java.util.ArrayList;
import java.util.List;

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
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

class NestedPropertyResolver implements IPropertyResolver
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
    static final String START_PLACEHOLDER = "${";
    static final String END_PLACEHOLDER = "}";
    static final String DELIMITER = ":";

    public String getPropertyValue( String key, Properties properties, Properties environment )
    {
        Stack<String> visited = new Stack<String>();
        Set<String> unresolved = new TreeSet<String>();

        key = resolveValue(key, properties, environment, visited, unresolved);
        String value = properties.getProperty(key);
        value = resolveValue(value, properties, environment, visited, unresolved);

//        properties.put(key, value);
//        System.out.println("############-=RESULTS=-##############");
//        System.out.println("Properties: " + properties);
//        System.out.println("Unresolvable properties: " + unresolved);
//        System.out.println("Key: " + key + " Value: " + value + "\n\n");

        return value;
    }

    private String resolveValue( String value, Properties properties, Properties environment, Stack<String> visited, Set<String> unresolved )
    {
        String buffer = value != null ? value : "";

        String newKey;
        while ((newKey = findInnermostPlaceholders(buffer, unresolved)) != null )
        {
            String originalKey = newKey;
            String defaultValue = null;
            if(newKey.contains(DELIMITER)) {
                String[] split = removePlaceholder(newKey).split(":",2);
                defaultValue = split[1];
                newKey = START_PLACEHOLDER + split[0] + END_PLACEHOLDER;
            }
            visited.push(newKey);
            String cleanNewKey = removePlaceholder(newKey);
            String newValue = fromPropertiesThenSystemThenEnvironment(cleanNewKey, properties, environment);
            if(newValue != null) {
                if(newValue.startsWith(START_PLACEHOLDER) && visited.contains(newValue)) {
                    String err = "Circular Reference to " +  newValue + " found cannot continue";
                    throw new IllegalArgumentException(err);
                } else if(newValue.contains(START_PLACEHOLDER)) {
                    newValue = resolveValue(newValue, properties, environment, visited, unresolved);
                    properties.put(cleanNewKey, newValue);
                }
            } else {
                unresolved.add(newKey);
            }
            //Set default value if necessary
            if((newValue == null || unresolved.contains(newValue)) && defaultValue != null) {
                newValue = defaultValue;
            }
            if(newValue != null) {
                buffer = buffer.replace(originalKey, newValue);
            }
            visited.pop();
        }
        return buffer;
    }

    private String removePlaceholder(String key) {
        return key.substring(2,key.length()-1);
    }

    private String findInnermostPlaceholders(String buffer, Set<String> unresolved)
    {
        Stack<Integer> q = new Stack<Integer>();
        int endIndex = -1;
        for(int i=0;i<buffer.length();i++) {
            if(i < (buffer.length() - (START_PLACEHOLDER.length() - 1)) && buffer.substring(i).startsWith(START_PLACEHOLDER)) {
                q.add(i);
            } else if(buffer.substring(i).startsWith(END_PLACEHOLDER) && !q.isEmpty()) {
                endIndex = i+1;
                String key = buffer.substring(q.pop(), endIndex);
                if(unresolved.contains(key)) {
                    continue;
                }
                return key;
            }
        }
        return null;
    }

    private String fromPropertiesThenSystemThenEnvironment( String key, Properties properties, Properties environment )
    {
        if(key.equals("")) { //empty place holder is always unresolved
            return null;
        }
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

        return value;
    }
}
