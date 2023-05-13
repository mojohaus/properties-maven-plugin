# MojoHaus Properties Maven Plugin

This is the [properties-maven-plugin](http://www.mojohaus.org/properties-maven-plugin/).

[![Maven Central](https://img.shields.io/maven-central/v/org.codehaus.mojo/properties-maven-plugin.svg?label=Maven%20Central)](https://search.maven.org/artifact/org.codehaus.mojo/properties-maven-plugin)
[![GitHub CI](https://github.com/mojohaus/properties-maven-plugin/actions/workflows/maven.yml/badge.svg)](https://github.com/mojohaus/properties-maven-plugin/actions/workflows/maven.yml)
## Releasing

* Make sure `gpg-agent` is running.
* Execute `mvn -B release:prepare release:perform`

For publishing the site do the following:

```
cd target/checkout
mvn verify site site:stage scm-publish:publish-scm
```
