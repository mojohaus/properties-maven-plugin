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

import java.util.*;

class CircularDefinitionPreventer
{
    private static class VisitedProperty
    {
        public final String key;
        public final String value;

        private VisitedProperty(String key, String value)
        {
            this.key = key;
            this.value = value;
        }
    }

    private final List entriesVisited = new LinkedList();
    private final Set keysUsed = new HashSet();

    public CircularDefinitionPreventer visited(String key, String value)
    {
        entriesVisited.add(new VisitedProperty(key, value));
        if (keysUsed.contains(key))
        {
            circularDefinition();
        }
        else
        {
            keysUsed.add(key);
        }

        return this;
    }

    private void circularDefinition()
    {
        StringBuilder buffer = new StringBuilder("Circular property definition: ");
        for (Iterator iterator = entriesVisited.iterator(); iterator.hasNext();)
        {
            VisitedProperty visited = (VisitedProperty) iterator.next();
            buffer.append(visited.key).append("=").append(visited.value);
            if (iterator.hasNext())
            {
                buffer.append(" -> ");
            }
        }
        throw new IllegalArgumentException(buffer.toString());
    }
}
