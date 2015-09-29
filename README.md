# Building

Prerequisities:
* Maven (package maven on Ubuntu and Arch Linux)
* JDK

The following command builds a runnable .jar file in ./target/
`mvn compile assembly:single`

# Running

First edit tourenplaner.conf to point to the right graph file and note the
httpport because that needs to be added to the URL for the clients.

Finally run:
`java -Xmx10g -jar target/tourenplaner-server-1.0-SNAPSHOT-jar-with-dependencies.jar -c tourenplaner.conf`
