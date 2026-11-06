 ------
 Introduction
 ------
 Zarar Siddiqi
 ------
 2007-07-18
 ------


Properties Maven Plugin

* Plugin limitations

  Maven load project model (from pom.xml) for project and all submodules at startup time.
  During project reading all available properties are resolved from projects and settings properties.

  This plugin (as all other) is executed in the later phase - when project model is already build in memory.

  So properties read from external files by this plugin can not by used in project definitions in items like
  <<<\<goal\>>>>, <<<\<version\>>>> and so on.

  Properties read by plugin in one module are not propagated to other modules or child projects.

  Properties are only available for other plugins in runtime like for <<<maven-resource-plugin>>> for filtering resources.

  In order to resolve such issue new features must by implement in Maven itself, like:

  * {{{https://issues.apache.org/jira/browse/MNG-7833}MNG-7833}}

  * {{{https://issues.apache.org/jira/browse/MNG-7914}MNG-7914}}

  []

* Plugin Overview

  The Properties Maven Plugin is here to make life a little easier when dealing
  with properties.  It provides goals to read properties from files and URLs and write
  properties to files, and also to set system properties.

  It's main use-case is loading properties from files or URLs instead of declaring them in
  <pom.xml>, something that comes in handy when dealing with different environments.

  If you have a properties file called <teams.properties> with this content:

----------------------------------------
  toronto=raptors
  miami=heat
----------------------------------------

  and invoke the {{{./read-project-properties-mojo.html}properties:read-project-properties}}
  goal, it would be similar (with described limitations) as declaring the following in your <pom.xml>:

----------------------------------------
  <properties>
    <toronto>raptors</toronto>
    <miami>heat</miami>
  </properties>
----------------------------------------

  The properties loaded from files or URLs can be used to filter resources
  differently for different environments.

  Optionally, the plugin can process default values within property placeholders.

  Property name is separated from the default value with <<<':'>>>.

  For example, given these initial values

----------------------------------------
  host=example.com
  port=9090
  fallback=fallback value
----------------------------------------

  the following replacements will happen:

*---------+-------------+----------------+
||<<Raw value>> || <<Replacement>> (<<<useDefaultValues>>> flag enabled) || <<Replacement>> (<<<useDefaultValues>>> flag disabled)
*---------*-------------*----------------*
|<<<$\{unknown:\}>>> | <<<$\{unknown\}>>> | <<<$\{unknown:\}>>> |
*---------*-------------*----------------*
<<<$\{unknown:defaultValue\}>>> | <<<defaultValue>>> | <<<$\{unknown:defaultValue\}>>> |
*---------*-------------*----------------*
<<<http://$\{uhost:localhost\}:$\{uport:8080\}>>> | <<<http://localhost:8080>>> | <<<http://$\{uhost:localhost\}:$\{uport:8080\}>>> |
*---------*-------------*----------------*
<<<http://$\{host:localhost\}:$\{port:8080\}>>> | <<<http://example.com:9090>>> | <<<http://$\{host:localhost\}:$\{port:8080\}>>> |
*---------*-------------*----------------*
<<<$\{unknown:$\{fallback\}\}>>> | <<<fallback value>>> | <<<$\{unknown:$\{fallback\}\}>>> |
*---------*-------------*----------------*
<<<$\{unknown:$\{double.unknown\}\}>>> | <<<$\{double.unknown\}>>> | <<<$\{unknown:$\{double.unknown\}\}>>> |
*---------*-------------*----------------*
<<<$\{unknown:with space\}>>> | <<<with space>>> | <<<$\{unknown:with space\}>>> |
*---------*-------------*----------------*
<<<$\{unknown:with extra :\}>>> | <<<with extra :>>> | <<<$\{unknown:with extra :\}>>> |
*---------*-------------*----------------*
<<<$\{malformed:defVal>>> | <<<$\{malformed:defVal>>> | <<<$\{malformed:defVal>>> |
*---------*-------------*----------------*
<<<$\{malformed:with space>>> | <<<$\{malformed:with space>>> | <<<$\{malformed:with space>>> |
*---------*-------------*----------------*
<<<$\{malformed:with extra :>>> | <<<$\{malformed:with extra :>>> | <<<$\{malformed:with extra :>>> |
*---------*-------------*----------------*
<<<$\{unknown::\}>>> | <<<:>>> | <<<$\{unknown::\}>>> |
*---------*-------------*----------------*
<<<$\{host\}>>> | <<<example.com>>> | <<<example.com>>> |
*---------*-------------*----------------*

* Goals Overview

~~  General Information about the goals.

  * {{{./read-project-properties-mojo.html}properties:read-project-properties}} Reads property
    files or properties from URLs as Project properties.

  * {{{./write-project-properties-mojo.html}properties:write-project-properties}} Writes project
    properties to a file.

  * {{{./write-active-profile-properties-mojo.html}properties:write-active-profile-properties}} Writes
    properties of all active profiles to a file.

  * {{{./set-system-properties-mojo.html}properties:set-system-properties}} Sets system properties.


* Usage

  Instructions on how to use the Properties Maven Plugin can be found {{{./usage.html}here}}.
