package org.metadatacenter.cadsr;

import org.dom4j.DocumentException;
import org.metadatacenter.cadsr.cadsr_objects.jaxb.DataElementsList;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import javax.management.InstanceNotFoundException;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class with a main method that executes the instances loading process.
 */
public class Main
{
  private static final String cadsrBaseFilePath = "src/main/resources/caDSR_production_CDEs_xml/";

  // Used for instances generation
  private static final String ontFilePath = "src/main/resources/caDSR.owl";
  private static final String populatedOntFilePath = "output/caDSR_populated.owl";

  // Used for value sets generation
  private static final String vsOntFilePath = "output/caDSR_vs.owl";

  private static List<File> cadsrXmlFiles;

  static {
    cadsrXmlFiles = new ArrayList<File>();
    for (int i = 1; i <= 1; i++) {
      String filePath = cadsrBaseFilePath + "xml_cde_201510293457_" + i + "_UTF8.xml";
      cadsrXmlFiles.add(new File(filePath));
    }
  }

  public static void main(String[] args)
  {

    final boolean LOAD_INSTANCES = false;
    final boolean CREATE_VS = true;

    Util util = new Util();

    DataElementsList dataElementsList = null;
    try {
      dataElementsList = util.cadsrXml2Objects(cadsrXmlFiles);
    } catch (DocumentException e) {
      e.printStackTrace();
    } catch (JAXBException e) {
      e.printStackTrace();
    }

    try {

      if (LOAD_INSTANCES) {
        // Load CaDSR instances into a CaDSR.owl ontology
        InstancesLoader instancesloader = new InstancesLoader();
        instancesloader
          .createOntologyInstances(util.createOntologyManager(), new File(ontFilePath), new File(populatedOntFilePath),
            dataElementsList);
      }

      if (CREATE_VS) {
        // Generate CaDSR value sets
        ValueSetsGenerator vsGenerator = new ValueSetsGenerator();
        vsGenerator.generateValueSets(util.createOntologyManager(), new File(vsOntFilePath), dataElementsList);
      }

    } catch (OWLOntologyCreationException e) {
      e.printStackTrace();
    } catch (OWLOntologyStorageException e) {
      e.printStackTrace();
    } catch (InstanceNotFoundException e) {
      e.printStackTrace();
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
  }
}
