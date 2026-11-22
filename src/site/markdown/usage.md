title: Usage
author:
- Zarar Siddiqi
date: 2007-07-18

Usage
=====

read-project-properties
-----------------------

The [properties:read-project-properties](./read-project-properties-mojo.html) goal
reads property files or URLs and stores them as project properties.

```xml
<project>
  <build>
    <plugins>
      <plugin>
        <groupId>org.codehaus.mojo</groupId>
        <artifactId>properties-maven-plugin</artifactId>
        <version><!-- latest release --></version>
        <executions>
          <execution>
            <phase>initialize</phase>
            <goals>
              <goal>read-project-properties</goal>
            </goals>
            <configuration>
              <files>
                <file>etc/config/dev.properties</file>
              </files>
            </configuration>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
</project>
```

or alternatively

```xml
<project>
  <build>
    <plugins>
      <plugin>
        <groupId>org.codehaus.mojo</groupId>
        <artifactId>properties-maven-plugin</artifactId>
        <executions>
          <execution>
            <phase>initialize</phase>
            <goals>
              <goal>read-project-properties</goal>
            </goals>
            <configuration>
              <urls>
                <url>classpath:/config/dev.properties</url>
                <url>file:///${env.HOME}/mydev.properties</url>
              </urls>
            </configuration>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
</project>
```

The goal accepts more settings:

* `encoding` is the character set of the files and URLs, for example `UTF-8`. Without it `.properties` resources are
  read as ISO-8859-1, as the JDK does.
* `keyPrefix` is added to the name of every property read, which separates properties of the same name from
  different files.
* `includedPropertyKeys` and `excludedPropertyKeys` choose the keys to import. They are matched as written in the
  file, before `keyPrefix` is applied. When `includedPropertyKeys` is not empty, no other key is read.
* `override` (default `true`) controls whether a value replaces a project property of the same name, and `quiet`
  ignores files and URLs that cannot be found.
* `useDefaultValues` turns on the `${name:default}` placeholders described in the [introduction](./index.html).
* `prop.skipLoadProperties` skips the goal.

```xml
<configuration>
  <files>
    <file>etc/config/dev.properties</file>
  </files>
  <encoding>UTF-8</encoding>
  <keyPrefix>dev.</keyPrefix>
  <excludedPropertyKeys>
    <excludedPropertyKey>password</excludedPropertyKey>
  </excludedPropertyKeys>
</configuration>
```

write-project-properties
------------------------

The [properties:write-project-properties](./write-project-properties-mojo.html) goal
writes all project properties to a given file.

```xml
<project>
  <build>
    <plugins>
      <plugin>
        <groupId>org.codehaus.mojo</groupId>
        <artifactId>properties-maven-plugin</artifactId>
        <executions>
          <execution>
            <phase>generate-resources</phase>
            <goals>
              <goal>write-project-properties</goal>
            </goals>
            <configuration>
              <outputFile>
                ${project.build.outputDirectory}/app.properties
              </outputFile>
            </configuration>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
</project>
```

By default only the project properties are written. Set `includeUserProperties` to `true` to also write the user
properties of the Maven session, that is the values given with `-D` on the command line, for example `-Dbuild.id=42`.
Other JVM system properties are not written, and `includedPropertyKeys` and `excludedPropertyKeys` apply to the
user properties too.

```xml
<configuration>
  <includeUserProperties>true</includeUserProperties>
  <outputFile>
    ${project.build.outputDirectory}/app.properties
  </outputFile>
</configuration>
```

`includedPropertyKeys` and `excludedPropertyKeys` limit the keys written, and `encoding` sets the character set of the
output file, `ISO-8859-1` for a `.properties` file when it is not set. A JVM system property with the same name as a
project property replaces its value in the output.

write-prefixed-properties
-------------------------

The [properties:write-prefixed-properties](./write-prefixed-properties-mojo.html) goal writes the project properties
whose names start with a prefix to a file, without the prefix. Use it together with the `keyPrefix` of
`read-project-properties` to pick the properties of one environment.

A property that starts with the `commonKeyPrefix` (default `*.`) is written too, unless a property with the
`keyPrefix` and the same remaining name exists; that one wins. With the properties

```properties
*.db.url=jdbc:h2:mem:test
dev.db.url=jdbc:postgresql://dev/app
dev.db.user=app
```

and the configuration below, the file contains the keys `db.url`, with the value of `dev.db.url`, and `db.user`.

```xml
<plugin>
  <groupId>org.codehaus.mojo</groupId>
  <artifactId>properties-maven-plugin</artifactId>
  <executions>
    <execution>
      <phase>generate-resources</phase>
      <goals>
        <goal>write-prefixed-properties</goal>
      </goals>
      <configuration>
        <keyPrefix>dev.</keyPrefix>
        <outputFile>
          ${project.build.outputDirectory}/app.properties
        </outputFile>
      </configuration>
    </execution>
  </executions>
</plugin>
```

write-active-profile-properties
-------------------------------

The [properties:write-active-profile-properties](./write-active-profile-properties-mojo.html) goal
writes properties of all active profiles to a given file.

```xml
<project>
  <build>
    <plugins>
      <plugin>
        <groupId>org.codehaus.mojo</groupId>
        <artifactId>properties-maven-plugin</artifactId>
        <executions>
          <execution>
            <phase>generate-resources</phase>
            <goals>
              <goal>write-active-profile-properties</goal>
            </goals>
            <configuration>
              <outputFile>
                ${project.build.outputDirectory}/app.properties
              </outputFile>
            </configuration>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
</project>
```

set-system-properties
---------------------

The [properties:set-system-properties](./set-system-properties-mojo.html)
goal sets the specified system properties.

```xml
<project>
  <build>
    <plugins>
      <plugin>
        <groupId>org.codehaus.mojo</groupId>
        <artifactId>properties-maven-plugin</artifactId>
        <executions>
          <execution>
            <goals>
              <goal>set-system-properties</goal>
            </goals>
            <configuration>
              <properties>
                <property>
                  <name>my.property.name</name>
                  <value>my.property.value</value>
                </property>
              </properties>
            </configuration>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
</project>
```

