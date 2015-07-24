# MojoHaus Properties Maven Plugin

This is the [properties-maven-plugin](http://www.mojohaus.org/properties-maven-plugin/).
 
[![Build Status](https://travis-ci.org/mojohaus/properties-maven-plugin.svg?branch=master)](https://travis-ci.org/mojohaus/properties-maven-plugin)

## Releasing

* Make sure `gpg-agent` is running.
* Execute `mvn -B release:prepare release:perform`

For publishing the site do the following:

```
cd target/checkout
mvn verify site site:stage scm-publish:publish-scm
```
