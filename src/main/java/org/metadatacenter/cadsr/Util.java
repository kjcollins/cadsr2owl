package org.metadatacenter.cadsr;

import org.dom4j.DocumentException;
import org.metadatacenter.cadsr.cadsr_objects.jaxb.DataElement;
import org.metadatacenter.cadsr.cadsr_objects.jaxb.DataElementsList;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import javax.annotation.Nonnull;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Util
{
  /**
   * Create OWLOntologyManager.
   *
   * @return An OWLOntologyManager.
   */
  @Nonnull public OWLOntologyManager createOntologyManager()
  {
    OWLOntologyManager m = OWLManager.createOWLOntologyManager();
    return m;
  }

  /**
   * Loads all caDSR CDEs from XML files into Java objects using JAXB (https://jaxb.java.net/)
   *
   * @param xmlFiles List of source XML files.
   * @throws DocumentException
   * @throws JAXBException
   * @return List of Data Elements
   */
  @Nonnull public DataElementsList cadsrXml2Objects(@Nonnull List<File> xmlFiles) throws DocumentException, JAXBException
  {
    DataElementsList dataElementsList = new DataElementsList();
    List<DataElement> dataElements = new ArrayList<DataElement>();

    for (File f : xmlFiles) {
      JAXBContext jaxbContext = JAXBContext.newInstance(DataElementsList.class);
      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

      List<DataElement> dataElementsTmp = ((DataElementsList)jaxbUnmarshaller.unmarshal(f)).getDataElements();
      dataElements.addAll(dataElementsTmp);
    }

    dataElementsList.setDataElements(dataElements);

    return dataElementsList;
  }
}
