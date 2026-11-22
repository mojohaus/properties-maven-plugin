# MojoHaus Properties Maven Plugin

[![Maven Central](https://img.shields.io/maven-central/v/org.codehaus.mojo/properties-maven-plugin.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/org.codehaus.mojo/properties-maven-plugin)
[![GitHub CI](https://github.com/mojohaus/properties-maven-plugin/actions/workflows/maven.yml/badge.svg?branch=master)](https://github.com/mojohaus/properties-maven-plugin/actions/workflows/maven.yml?query=branch%3Amaster)

The Properties Maven Plugin reads properties from files and URLs into the project, for example to filter resources
differently per environment, and writes project or profile properties to a file. It can also set system properties.
The goals are `read-project-properties`, `write-project-properties`, `write-prefixed-properties`,
`write-active-profile-properties` and `set-system-properties`.

It requires Maven 3.9.0 or later and Java 8 or later. For example, to load the properties of one environment:

```xml
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
```

The [plugin site](https://www.mojohaus.org/properties-maven-plugin/) lists the goals and has the
[usage](https://www.mojohaus.org/properties-maven-plugin/usage.html), including what the plugin cannot do:
properties it reads are not visible where Maven resolves the POM, such as in `<version>` or `<goal>`.

## Releasing

* Make sure `gpg-agent` is running.
* Execute `mvn -B release:prepare release:perform`

For publishing the site do the following:

```
cd target/checkout
mvn verify site site:stage scm-publish:publish-scm
```

Conventions for contributors and agents are in [AGENTS.md](AGENTS.md).
