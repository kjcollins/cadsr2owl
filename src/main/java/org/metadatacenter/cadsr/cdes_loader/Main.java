package org.metadatacenter.cadsr.cdes_loader;

import org.dom4j.DocumentException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Class with a main method that executes the instances loading process.
 */
public class Main
{

  private static final String cadsrBaseFilePath = "src/main/resources/caDSR_production_CDEs_xml/";
  private static final String ontFilePath = "src/main/resources/caDSR.owl";
  private static final String populatedOntFilePath = "caDSR_populated.owl";

  private static List<File> cadsrXmlFiles;

  static {
    cadsrXmlFiles = new ArrayList<File>();
    for (int i = 1; i <= 11; i++) {
      String filePath = cadsrBaseFilePath + "xml_cde_201510293457_" + i + "_UTF8.xml";
      cadsrXmlFiles.add(new File(filePath));
    }
  }

  public static void main(String[] args)
  {

    Loader loader = new Loader();

    try {
      loader.cadsrXml2Objects(cadsrXmlFiles);
    } catch (DocumentException e) {
      e.printStackTrace();
    } catch (JAXBException e) {
      e.printStackTrace();
    }

    OWLOntologyManager m = loader.createOntologyManager();

    try {
      loader.createOntologyInstances(m, new File(ontFilePath), new File(populatedOntFilePath));
    } catch (OWLOntologyCreationException e) {
      e.printStackTrace();
    } catch (OWLOntologyStorageException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
