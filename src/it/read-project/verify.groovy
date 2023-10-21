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
File testFile = new File( basedir, 'target/classes/test.txt' )
assert testFile.exists()

String content = testFile.getText()
assert content.contains('props1 has value value1')
assert content.contains('props2 has value value2')
assert content.contains('props3 has value value3')

File buildLog = new File( basedir, 'build.log' )
assert buildLog.exists()
assert buildLog.getText().contains("[WARNING] Unknown properties resource extension: 'txt' assume as: 'properties'")

