# Building

Prerequisities:
* Maven (package maven on Ubuntu and Arch Linux)
* JDK

The following command builds a runnable .jar file in ./target/

`mvn compile assembly:single`

# Running

First edit tourenplaner.conf to point to the right graph file and note the
httpport because that needs to be added to the URL for the clients.
Alternatively just use the default file by downloading the Saarland graph:

`wget -O - http://niklas.frickel.club/uni/saarland_ch_mine.txt.bz2 | bunzip2 -c - > ch_graph.txt`

Finally run:

`java -Xmx10g -jar target/tourenplaner-server-1.0-SNAPSHOT-jar-with-dependencies.jar -c tourenplaner.conf`

# Updating before Building

The versions-maven-plugin is a simple way for updating dependencies in `pom.xml`, but with some shortcomings:
- Problems with non standard version schemes, for example:
  older versions of `commons-collections` (not used in this project) are formatted like `20040102.233541`,
  newer versions like `2.0`, but so the old version is detected as newer, with having the higher version number.
- A change of the groupId for newer versions remains undetected, for example:
  `c3p0` now has the groupId `com.mchange`.
- Versions with qualifiers like `beta` or `RC` in the version string are considered as full releases,
  but with the `rules.xml` file and suitable regex definitions, those versions will get ignored.
  But this feature is marked as deprecated for the upcoming `versions-maven-plugin 3.0.0`,
  hopefully than replaced with a better versions handling.
- Maven plugins will not get updated automatically.

View Dependencies with newer versions:

`mvn versions:display-dependency-updates`

View Plugins with Updates available:

`mvn versions:display-plugin-updates`

Update `pom.xml` to newer Dependencies:

`mvn versions:use-latest-versions`

Update version properties in `pom.xml`:

`versions:update-properties`

Commit Updates - Remove `pom.xml.versionsBackup`:

`mvn versions:commit`

Revert Updates - Restore `pom.xml` from `pom.xml.versionsBackup`:

`mvn versions:revert`
