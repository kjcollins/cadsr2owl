package org.metadatacenter.cadsr.cdes_loader;

import org.metadatacenter.cadsr.cdes_loader.cadsr_objects.jaxb.AlternateName;
import org.metadatacenter.cadsr.cdes_loader.cadsr_objects.jaxb.Classification;
import org.metadatacenter.cadsr.cdes_loader.cadsr_objects.jaxb.ComponentDataElement;
import org.metadatacenter.cadsr.cdes_loader.cadsr_objects.jaxb.Concept;
import org.metadatacenter.cadsr.cdes_loader.cadsr_objects.jaxb.DataElement;
import org.metadatacenter.cadsr.cdes_loader.cadsr_objects.jaxb.DataElementsList;
import org.metadatacenter.cadsr.cdes_loader.cadsr_objects.jaxb.PermissibleValue;
import org.metadatacenter.cadsr.cdes_loader.cadsr_objects.jaxb.ReferenceDocument;
import org.metadatacenter.cadsr.cdes_loader.cadsr_objects.jaxb.ValueDomainConcept;
import org.dom4j.DocumentException;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.OWLXMLDocumentFormat;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import javax.annotation.Nonnull;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Load caDSR Common Data Elements (CDEs) into an existing caDSR OWL ontology.
 *
 */
public class Loader
{

  private DataElementsList dataElementsList;
  private OWLOntology ont;

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
   */
  @Nonnull public void cadsrXml2Objects(@Nonnull List<File> xmlFiles) throws DocumentException, JAXBException
  {
    dataElementsList = new DataElementsList();
    List<DataElement> dataElements = new ArrayList<DataElement>();

    for (File f : xmlFiles) {
      JAXBContext jaxbContext = JAXBContext.newInstance(DataElementsList.class);
      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

      List<DataElement> dataElementsTmp = ((DataElementsList)jaxbUnmarshaller.unmarshal(f)).getDataElements();
      dataElements.addAll(dataElementsTmp);
    }

    dataElementsList.setDataElements(dataElements);
  }

  /**
   * Create all CDEs in the ontology. They are represented as OWL individuals.
   *
   * @param manager          The OWL Ontology Manager.
   * @param ontFile          The file in which the caDSR ontology to be populated is stored (input file).
   * @param populatedOntFile The file that will contain the caDSR ontology populated will all the instances (output file).
   * @throws Exception
   */
  @Nonnull public void createOntologyInstances(@Nonnull OWLOntologyManager manager, @Nonnull File ontFile,
    @Nonnull File populatedOntFile) throws Exception
  {
    ont = manager.loadOntologyFromOntologyDocument(ontFile);

    // Types that contain a value property inside
    List<String> typesToExplore = new ArrayList<String>();
    typesToExplore.add("publicid");
    typesToExplore.add("contextname");
    typesToExplore.add("maximumvalue");
    typesToExplore.add("minimumvalue");
    typesToExplore.add("charactersetname");
    typesToExplore.add("decimalplace");
    typesToExplore.add("maximumlength");
    typesToExplore.add("contextversion");
    typesToExplore.add("preferredname");
    typesToExplore.add("origin");
    typesToExplore.add("displayformat");
    typesToExplore.add("version");
    typesToExplore.add("longname");
    typesToExplore.add("minimumlength");
    typesToExplore.add("unitofmeasure");
    typesToExplore.add("methods");
    typesToExplore.add("rule");
    typesToExplore.add("derivationtype");
    typesToExplore.add("derivationtypedescription");
    typesToExplore.add("concatenationcharacter");
    typesToExplore.add("displayorder");
    typesToExplore.add("language");
    typesToExplore.add("alternatenamevalue");
    typesToExplore.add("alternatenametype");
    typesToExplore.add("organizationname");
    typesToExplore.add("url");
    typesToExplore.add("documenttext");
    typesToExplore.add("meaningconcepts");
    typesToExplore.add("pvbegindate");
    typesToExplore.add("pvenddate");
    typesToExplore.add("definitionsource");

    int i = 0;
    int previousProgress = 0;
    int newProgress = 0;
    for (DataElement de : dataElementsList.getDataElements()) {
      createDataElement(manager, de, typesToExplore);
      i++;
      newProgress = (i * 100) / dataElementsList.getDataElements().size();
      if (newProgress != previousProgress) {
        System.out.println("Progress: " + newProgress + "%");
        previousProgress = newProgress;
      }
    }
    System.out.println("Number of data elements loaded: " + dataElementsList.getDataElements().size());
    manager.saveOntology(ont, new OWLXMLDocumentFormat(), IRI.create(populatedOntFile.toURI()));
  }

  /**
   * Create a particular Common Data Element (CDE) and all related information as OWL individuals
   *
   * @param manager        The OWL Ontology Manager.
   * @param dataElement    The CDE to be created.
   * @param typesToExplore List of Java object types that should be managed differently. These classes contain a value
   *                       field with the relevant information and it was necessary to write ad-hoc code to retrieve it.
   * @return The CDE that has been created.
   * @throws Exception
   */
  @Nonnull private OWLIndividual createDataElement(@Nonnull OWLOntologyManager manager,
    @Nonnull DataElement dataElement, @Nonnull List<String> typesToExplore) throws Exception
  {
    OWLDataFactory factory = manager.getOWLDataFactory();
    IRI ontologyIRI = ont.getOntologyID().getOntologyIRI().get();

    // DataElement
    OWLIndividual indDataElement = createInstanceFromObject(manager, dataElement, "getPublicId", typesToExplore);

    // ValueDomain
    OWLIndividual indValueDomain = createInstanceFromObject(manager, dataElement.getValueDomain(), "getPublicId",
      typesToExplore);
    // indDataElement --> [valueDomain] --> indValueDomain"
    createObjectPropertyAssertion(manager, ontologyIRI, indDataElement, "valueDomain", indValueDomain);

    // PermissibleValues
    for (PermissibleValue v : dataElement.getValueDomain().getPermissibleValues().getPermissibleValuesITEM()) {
      // PermissibleValue
      OWLIndividual indPermissibleValue = createInstanceFromObject(manager, v, "getVmPublicId", typesToExplore);
      // indValueDomain --> [permissibleValue] --> indPermissibleValue"
      createObjectPropertyAssertion(manager, ontologyIRI, indValueDomain, "permissibleValue", indPermissibleValue);
    }

    // ValueDomainConcepts
    for (ValueDomainConcept c : dataElement.getValueDomain().getValueDomainConcepts().getValueDomainConceptsITEM()) {
      // ValueDomainConcept
      OWLIndividual indValueDomainConcept = createInstanceFromObject(manager, c, "getConId", typesToExplore);
      // indValueDomain --> [valueDomainConcept] --> indValueDomainConcept"
      createObjectPropertyAssertion(manager, ontologyIRI, indValueDomain, "valueDomainConcept", indValueDomainConcept);
    }

    // Representation
    OWLIndividual indRepresentation = createInstanceFromObject(manager,
      dataElement.getValueDomain().getRepresentation(), "getPublicId", typesToExplore);
    // indValueDomain --> [representation] --> indRepresentation"
    createObjectPropertyAssertion(manager, ontologyIRI, indValueDomain, "representation", indRepresentation);

    // DataElementConcept
    OWLIndividual indDataElementConcept = createInstanceFromObject(manager, dataElement.getDataElementConcept(),
      "getPublicId", typesToExplore);
    // indDataElement --> [valueDomain] --> indDataElementConcept"
    createObjectPropertyAssertion(manager, ontologyIRI, indDataElement, "dataElementConcept", indDataElementConcept);

    // ConceptualDomain (from ValueDomain)
    OWLIndividual indConceptualDomain1 = createInstanceFromObject(manager,
      dataElement.getValueDomain().getConceptualDomain(), "getPublicId", typesToExplore);
    // indValueDomain --> [conceptualDomain] --> indConceptualDomain"
    createObjectPropertyAssertion(manager, ontologyIRI, indValueDomain, "conceptualDomain", indConceptualDomain1);

    // ConceptualDomain (from DataElementConcept)
    OWLIndividual indConceptualDomain2 = createInstanceFromObject(manager,
      dataElement.getDataElementConcept().getConceptualDomain(), "getPublicId", typesToExplore);
    // indDataElementConcept --> [conceptualDomain] --> indConceptualDomain"
    createObjectPropertyAssertion(manager, ontologyIRI, indDataElementConcept, "conceptualDomain",
      indConceptualDomain2);

    // Property
    OWLIndividual indProperty = createInstanceFromObject(manager, dataElement.getDataElementConcept().getProperty(),
      "getPublicId", typesToExplore);
    // indDataElementConcept --> [property] --> indProperty"
    createObjectPropertyAssertion(manager, ontologyIRI, indDataElementConcept, "property", indProperty);

    // ObjectClass
    OWLIndividual indObjectClass = createInstanceFromObject(manager,
      dataElement.getDataElementConcept().getObjectClass(), "getPublicId", typesToExplore);
    // indDataElementConcept --> [objectClass] --> indObjectClass"
    createObjectPropertyAssertion(manager, ontologyIRI, indDataElementConcept, "objectClass", indObjectClass);

    // Concepts (from Representation)
    for (Concept c : dataElement.getValueDomain().getRepresentation().getConceptDetails().getConceptDetailsITEM()) {
      // Concept
      OWLIndividual indConcept = createInstanceFromObject(manager, c, "getConId", typesToExplore);
      // indRepresentation --> [concept] --> indConcept"
      createObjectPropertyAssertion(manager, ontologyIRI, indRepresentation, "concept", indConcept);
    }

    // Concepts (from Property)
    for (Concept c : dataElement.getDataElementConcept().getProperty().getConceptDetails().getConceptDetailsITEM()) {
      // Concept
      OWLIndividual indConcept = createInstanceFromObject(manager, c, "getConId", typesToExplore);
      // indProperty --> [concept] --> indConcept"
      createObjectPropertyAssertion(manager, ontologyIRI, indProperty, "concept", indConcept);
    }

    // Concepts (from ObjectClass)
    for (Concept c : dataElement.getDataElementConcept().getObjectClass().getConceptDetails().getConceptDetailsITEM()) {
      // Concept
      OWLIndividual indConcept = createInstanceFromObject(manager, c, "getConId", typesToExplore);
      // indObjectClass --> [concept] --> indConcept"
      createObjectPropertyAssertion(manager, ontologyIRI, indObjectClass, "concept", indConcept);
    }

    // DataElementDerivation
    OWLIndividual indDataElementDerivation = createInstanceFromObject(manager, dataElement.getDataElementDerivation(),
      null, typesToExplore);
    // indDataElement --> [dataElementDerivation] --> indDataElementDerivation"
    createObjectPropertyAssertion(manager, ontologyIRI, indDataElement, "dataElementDerivation",
      indDataElementDerivation);

    // ComponentDataElements
    for (ComponentDataElement c : dataElement.getDataElementDerivation().getComponentDataElementsList()
      .getComponentDataElementsListITEM()) {
      // ComponentDataElement
      OWLIndividual indComponentDataElement = createInstanceFromObject(manager, c, "getPublicId", typesToExplore);
      // indDataElementDerivation --> [componentDataElement] --> indComponentDataElement"
      createObjectPropertyAssertion(manager, ontologyIRI, indDataElementDerivation, "componentDataElement",
        indComponentDataElement);
    }

    // AlternateNames
    for (AlternateName n : dataElement.getAlternateNameList().getALTERNATENAMELISTITEM()) {
      // AlternateName
      OWLIndividual indAlternateName = createInstanceFromObject(manager, n, null, typesToExplore);
      // indDataElement --> [alternateName] --> indAlternateName"
      createObjectPropertyAssertion(manager, ontologyIRI, indDataElement, "alternateName", indAlternateName);
    }

    // Classifications
    for (Classification c : dataElement.getClassificationsList().getCLASSIFICATIONSLISTITEM()) {
      // Classification
      OWLIndividual indClassification = createInstanceFromObject(manager, c, "getCsiPublicId", typesToExplore);
      // indDataElement --> [classification] --> indClassification"
      createObjectPropertyAssertion(manager, ontologyIRI, indDataElement, "classification", indClassification);

      // ClassificationScheme
      OWLIndividual indClassificationScheme = createInstanceFromObject(manager, c.getClassificationScheme(),
        "getPublicId", typesToExplore);
      // indClassification --> [classificationScheme] --> indClassificationScheme"
      createObjectPropertyAssertion(manager, ontologyIRI, indClassification, "classificationScheme",
        indClassificationScheme);
    }

    // ReferenceDocuments
    for (ReferenceDocument d : dataElement.getReferenceDocumentsList().getREFERENCEDOCUMENTSLISTITEM()) {
      // ReferenceDocument
      OWLIndividual indReferenceDocument = createInstanceFromObject(manager, d, null, typesToExplore);
      // indDataElement --> [referenceDocument] --> indReferenceDocument"
      createObjectPropertyAssertion(manager, ontologyIRI, indDataElement, "referenceDocument", indReferenceDocument);
    }

    return indDataElement;
  }

  /**
   * Create an object property assertion.
   *
   * @param manager      OWL Ontology Manager.
   * @param ontologyIRI  IRI of the caDSR ontology.
   * @param sourceInd    Source individual.
   * @param propertyName Name of the property to be created.
   * @param targetInd    Target individual.
   * @return The object property that has been created.
   * @throws Exception
   */
  private OWLObjectProperty createObjectPropertyAssertion(@Nonnull OWLOntologyManager manager, @Nonnull IRI ontologyIRI,
    @Nonnull OWLIndividual sourceInd, @Nonnull String propertyName, @Nonnull OWLIndividual targetInd) throws Exception
  {
    OWLDataFactory factory = manager.getOWLDataFactory();
    IRI opIRI = IRI.create(ontologyIRI + "#" + propertyName);
    if (ont.containsObjectPropertyInSignature(opIRI) == false) {
      throw new Exception("The object property \"" + opIRI + "\" was not found in the ontology");
    }
    OWLObjectProperty op = factory.getOWLObjectProperty(opIRI);
    OWLObjectPropertyAssertionAxiom assertion = factory.getOWLObjectPropertyAssertionAxiom(op, sourceInd, targetInd);
    manager.applyChange(new AddAxiom(ont, assertion));
    return op;
  }

  /**
   * Create a particular OWL individual.
   *
   * @param manager        OWL Ontology Manager.
   * @param obj            Java object that contains all the information about the individual to be created.
   * @param idMethodName   Operation in the Java object that provides the identifier for the individual. If it is null,
   *                       then a unique identifier is automatically generated for it.
   * @param typesToExplore List of Java object types that should be managed differently. These classes contain a value
   *                       field with the relevant information and it was necessary to write ad-hoc code to retrieve it.
   * @return The OWL individual that has been created.
   * @throws Exception
   */
  @Nonnull private OWLIndividual createInstanceFromObject(@Nonnull OWLOntologyManager manager, @Nonnull Object obj,
    String idMethodName, @Nonnull List<String> typesToExplore) throws Exception
  {
    OWLDataFactory factory = manager.getOWLDataFactory();
    IRI ontologyIRI = ont.getOntologyID().getOntologyIRI().get();
    // Get class
    IRI classIRI = IRI.create(ontologyIRI + "#" + obj.getClass().getSimpleName());
    if (ont.containsClassInSignature(classIRI) == false) {
      throw new Exception("The class \"" + obj.getClass().getSimpleName() + "\" was not found in the ontology");
    }
    OWLClass cls = factory.getOWLClass(classIRI);

    // Create individual
    OWLIndividual ind = null;
    String indId = null;
    if (idMethodName != null) {
      String idPropertyName = Character.toLowerCase(idMethodName.substring(3).charAt(0)) + idMethodName.substring(4);
      try {
        // If it is necessary to call an additional method to retrieve the id
        if (typesToExplore.contains(idPropertyName.toLowerCase()) && (!obj.getClass().getMethod(idMethodName)
          .getReturnType().getSimpleName().equalsIgnoreCase("String"))) {
          Object obj2 = obj.getClass().getMethod(idMethodName).invoke(obj);
          indId = obj2.getClass().getMethod("getValue").invoke(obj2).toString().trim();
        } else {
          indId = obj.getClass().getMethod(idMethodName).invoke(obj).toString();
        }
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      } catch (InvocationTargetException e) {
        e.printStackTrace();
      } catch (NoSuchMethodException e) {
        e.printStackTrace();
      }
    } else { // Autogenerated id
      indId = UUID.randomUUID().toString();
    }

    IRI indIRI = IRI.create(ontologyIRI + "#" + indId);
    ind = factory.getOWLNamedIndividual(indIRI);
    // Create assertion that associates the individual to the class
    OWLClassAssertionAxiom indAxiom = factory.getOWLClassAssertionAxiom(cls, ind);
    manager.addAxiom(ont, indAxiom);

    for (Field f : obj.getClass().getDeclaredFields()) {
      if (f.getName().compareTo("_null") != 0) { // Ignore "NULL" field, which is present in some entities
        if (f.getType().equals(String.class)) {
          // Get data property
          OWLDataProperty dp = factory.getOWLDataProperty(IRI.create(ontologyIRI + "#" + f.getName()));
          // Create assertion
          String methodName = "get" + f.getName().substring(0, 1).toUpperCase() + f.getName().substring(1);
          // Get value
          String value = obj.getClass().getMethod(methodName).invoke(obj).toString().trim();
          if (value.length() > 0) {
            AddAxiom addAxiom = new AddAxiom(ont, factory.getOWLDataPropertyAssertionAxiom(dp, ind, value));
            manager.applyChange(addAxiom);
          }
        } else if (typesToExplore.contains(f.getType().getSimpleName().toLowerCase())) {
          // Get data property
          OWLDataProperty dp = factory.getOWLDataProperty(IRI.create(ontologyIRI + "#" + f.getName()));
          // Create assertion
          String methodName = "get" + f.getName().substring(0, 1).toUpperCase() + f.getName().substring(1);
          // Get value
          Object obj2 = obj.getClass().getMethod(methodName).invoke(obj);
          String value = obj2.getClass().getMethod("getValue").invoke(obj2).toString().trim();
          if (value.length() > 0) {
            AddAxiom addAxiom = new AddAxiom(ont, factory.getOWLDataPropertyAssertionAxiom(dp, ind, value));
            manager.applyChange(addAxiom);
          }
        }
      }
    }
    return ind;
  }

}
