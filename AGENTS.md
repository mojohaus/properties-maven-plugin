# Agent guide for properties-maven-plugin

This file is for automated agents and human contributors working on this repository. It records
what the code and the POMs do not make obvious.

## Layout

- One module with five goals in `org.codehaus.mojo.properties`: `read-project-properties`
  (`ReadPropertiesMojo`), `write-project-properties` (`WriteProjectProperties`),
  `write-prefixed-properties` (`WritePrefixedPropertiesMojo`), `write-active-profile-properties`
  (`WriteActiveProfileProperties`), `set-system-properties` (`SetSystemPropertiesMojo`); `PropertyResolver` expands `${key:default}` references.
- `managers` holds the file formats: `JdkPropertiesManager` for `.properties` and
  `YmlPropertiesManager` for YAML.
- The site is built from `src/site/markdown`.

## Build gates

- `mvn verify` runs Spotless, Checkstyle and the unit tests. On a local build Spotless applies the
  formatting; it also reformats Markdown, so run `mvn spotless:apply` after editing the README or
  this file.
- The parent, `mojo-parent`, sets `maven.compiler.release` to 8, so main code must use the Java 8
  API.
- The plugin compiles against the Maven 3.9 line (`mavenVersion`) and declares a literal
  `<prerequisites><maven>3.9.0</maven>`; keep the two apart, so raising `mavenVersion` does not
  raise the prerequisite.

## Unit tests

- Unit tests use JUnit 5. File-based tests use `@TempDir`.

## Integration tests

- They live in `src/it`, one directory per test, and run with maven-invoker-plugin in the `run-its`
  profile. The plugin is installed into `target/local-repo` first, and `src/it/settings.xml`
  points the forked builds at it.
- Run a subset:

  ```shell
  mvn -B -P run-its verify -Dinvoker.test=write-project,read-project
  ```
- Write `verify.groovy` as `assert` statements. Invoker fails a script whose last expression is
  anything other than `true` or `null`, so a trailing `list.each { }` fails the test; use a `for`
  loop.

## Dependencies

- Deliberate pins in `.github/dependabot.yml`: `maven-core`, `maven-model` and
  `maven-plugin-api` stay below 3.10.0, because the plugin baseline is the Maven 3.9.x line.

## Continuous integration and pull requests

- `.github/workflows/maven.yml` calls the shared MojoHaus verify workflow from
  `mojohaus/.github`. Branches in this repository build under the push event, so look
  at the push run for a pull request from a same-repository branch.
- Pull requests are squash- or rebase-merged; merge commits are disabled. Release Drafter builds
  the release notes from the titles and labels, so the title should describe the change for a
  user.

## Documentation updates

A change that users or contributors can notice ships with the documentation it affects, in the
same pull request. A refactor with no visible effect needs none.

- A new goal, parameter or default: the mojo Javadoc, which becomes the goal page, with `@since`
  set to the version in the release-drafter draft, and the goal list in `src/site/markdown/index.md`.
- New or changed usage: a section in `src/site/markdown/usage.md`. Only the first plugin snippet
  carries a version, written as `<version><!-- latest release --></version>`; the others leave it
  out, so no example goes stale.
- A page added, removed or renamed: the menu in `src/site/site.xml`. Never rename a published
  page; its URL is linked from elsewhere.
- The Maven or Java requirement, or how to build and release: `README.md`.
- A new convention, build gate or test fixture: this file.

Check the result with `mvn site` and open `target/site/index.html`.
