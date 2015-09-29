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
