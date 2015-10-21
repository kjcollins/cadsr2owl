caDSR2OWL
============

Custom utility to load caDSR Common Data Elements (CDEs) into an OWL ontology.

#### Building Prerequisites

To build this library you must have the following items installed:

+ [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
+ A tool for checking out a [Git](http://git-scm.com/) repository.
+ Apache's [Maven](http://maven.apache.org/index.html).

#### Building

Get a copy of the latest code:

`git clone https://github.com/metadatacenter/cadsr2owl.git`

Go to the cadsr2owl directory:

`cd cadsr2owl`

Then build it with Maven:

`mvn clean install`

On build completion your local Maven repository will contain the generated cadsr2owl-${version}.jar and a fat JAR called cadsr2owl-${version}-jar-with-dependencies.jar.
The ./target directory will also contain these JARs.

#### Running

To run with Maven:

`mvn exec:java`

The populated caDSR ontology generated will be placed at the cadsr2owl directory.

To run using the fat JAR:

`java -jar ./target/cadsr2owl-${version}-jar-with-dependencies.jar`
