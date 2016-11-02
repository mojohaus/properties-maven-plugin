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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.util.Properties;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Writes custom properties to a file.
 *
 * @author <a href="mailto:onurkaganozcan@gmail.com">Onur Kağan Özcan</a>
 * @version $Id$
 */
@Mojo( name = "write-custom-properties", defaultPhase = LifecyclePhase.NONE, threadSafe = true )
public class WriteCustomProperties
    extends AbstractWritePropertiesMojo
{

   /**
    * The custom properties to set.
    */
   @Parameter(name = "properties", required = true)
   private Properties properties;

   public void execute()
       throws MojoExecutionException, MojoFailureException
   {
      validateOutputFile();
      writeProperties(properties, getOutputFile());
   }
}
