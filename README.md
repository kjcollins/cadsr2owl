caDSR2OWL
=========

Custom utility to load caDSR Common Data Elements (CDEs) into an OWL ontology.

All the caDSR production CDEs can be downloaded both in Excel and XML format from [here] (https://wiki.nci.nih.gov/display/caDSR/caDSR+Hosted+Data+Standards%2C+Downloads%2C+and+Transformation+Utilities#caDSRHostedDataStandards,Downloads,andTransformationUtilities-caDSRProductionCDEsinXML). 

We have used the JAXB xjc compiler to create Java classes from the [caDSR DataElement DTD](https://github.com/metadatacenter/cadsr2owl/blob/master/dtd/DataElement_V4.0.dtd):

  xjc -dtd -p generated_classes DataElement_V4.0.dtd

Our tool reads all CDEs from the XML files using JAXB and uses the previously generated Java classes and the [OWL API](http://owlapi.sourceforge.net/) to load the CDEs into the caDSR.owl ontology as OWL individuals.

An online [CED Browser](https://cdebrowser.nci.nih.gov/CDEBrowser/) can be used to get CDEs from caDSR.

A large sample of XML-encoded CDEs downloaded from caDSR can be found in the ```./src/main/resources/caDSR_production_CDEs_xml/``` directory. 
The DTD describing the schema of the XML-defined CDEs is in the ```./dtd``` directory.

An set of example CDEs in Excel format can be found in the ```./src/main/resources/xls/``` directory.

An OWL ontology defining a CDE can be found at ```.src/main/resources/caDSR.owl```.

#### Building from Source

To build this library you must have the following items installed:

+ [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
+ A tool for checking out a [Git](http://git-scm.com/) repository.
+ Apache's [Maven](http://maven.apache.org/index.html).

Get a copy of the latest code:

  git clone https://github.com/metadatacenter/cadsr2owl.git

Go to the cadsr2owl directory:

  cd cadsr2owl

Then build it with Maven:

  mvn clean install

On build completion your local Maven repository will contain the generated ```cadsr2owl-${version}.jar``` and a fat JAR called ```cadsr2owl-${version}-jar-with-dependencies.jar```.

#### Running

To run with Maven:

  mvn exec:java

The populated caDSR ontology generated will be placed at the ```./cadsr2owl``` subdirectory.

To run using the fat JAR:

  java -jar ./target/cadsr2owl-${version}-jar-with-dependencies.jar
