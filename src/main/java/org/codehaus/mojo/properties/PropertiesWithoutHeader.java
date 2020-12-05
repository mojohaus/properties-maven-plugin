package org.codehaus.mojo.properties;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

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

/**
 * PropertiesWithoutHeader
 *
 * Properties without writing header comment and generated timestamp at the beginning of the properties file
 *
 */
public class PropertiesWithoutHeader extends Properties {

    private static final long serialVersionUID = 8667775349218327383L;

    private static class SkipFirstLineStream extends FilterOutputStream {

        private boolean firstLineDetected = false;

        public SkipFirstLineStream(final OutputStream out) {
            super(out);
        }

        @Override
        public void write(final int b) throws IOException {
            if (firstLineDetected) {
                super.write(b);
            } else if (b == '\n') {
                firstLineDetected = true;
            }
        }
    }

    @Override
    public void store(final OutputStream out, final String comments) throws IOException {
        super.store(new SkipFirstLineStream(out), null);
    }
}
