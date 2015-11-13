package org.metadatacenter.cadsr;

import org.metadatacenter.cadsr.cadsr_objects.jaxb.DataElement;
import org.metadatacenter.cadsr.cadsr_objects.jaxb.DataElementsList;
import org.metadatacenter.cadsr.cadsr_objects.jaxb.PermissibleValue;
import org.metadatacenter.cadsr.cadsr_objects.jaxb.ValueDomain;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class ValueSetsGenerator
{

  @Nonnull public final IRI CADSRVS_IRI = IRI.create("http://metadatacenter.org/owl/caDSR_VS");

  // Types that contain a value property inside
  @Nonnull public final List<String> typesToExplore = new ArrayList<String>();

  public ValueSetsGenerator()
  {
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
  }

  @Nonnull public void generateValueSets(@Nonnull OWLOntologyManager manager, @Nonnull File vsOntFile,
    @Nonnull DataElementsList dataElementsList)
    throws OWLOntologyCreationException, OWLOntologyStorageException, NoSuchMethodException, InvocationTargetException,
    IllegalAccessException, UnsupportedEncodingException
  {
    // Now create the ontology - we use the ontology IRI (not the physical URI)
    IRI ontologyIRI = CADSRVS_IRI;
    IRI ontologySaveIRI = IRI.create(vsOntFile.toURI());
    System.out.println("Ontology IRI: " + ontologyIRI);
    OWLOntology ontology = manager.createOntology(ontologyIRI);

    OWLDataFactory factory = manager.getOWLDataFactory();

    int value_sets_count = 0;
    int values_count = 0;
    for (DataElement dataElement : dataElementsList.getDataElements()) {
      ValueDomain valueDomain = dataElement.getValueDomain();

      // Keep only value sets with more than one permissible value
      if (valueDomain.getPermissibleValues().getPermissibleValuesITEM().size() > 1) {
        /*** CREATE VALUE SET ***/
      String vsId = valueDomain.getPublicId().getValue();
//        String vsName = valueDomain.getLongName().getValue();
//        vsName = (vsName.trim()).replaceAll("\\s+", "_");
        OWLClass valueSetClass = factory.getOWLClass(IRI.create(ontologyIRI + "#" + vsId));
        OWLAxiom declareValueSetClass = factory.getOWLDeclarationAxiom(valueSetClass);
        manager.addAxiom(ontology, declareValueSetClass);
        value_sets_count++;

        // Default annotation properties
        // Preferred Name Property
        manager.applyChange(new AddAxiom(ontology, factory.getOWLAnnotationAssertionAxiom(valueSetClass.getIRI(),
          factory.getOWLAnnotation(
            factory.getOWLAnnotationProperty(IRI.create("http://www.w3.org/2004/02/skos/core#prefLabel")),
            factory.getOWLLiteral(valueDomain.getLongName().getValue())))));
        // Synonym Property
        manager.applyChange(new AddAxiom(ontology, factory.getOWLAnnotationAssertionAxiom(valueSetClass.getIRI(),
          factory.getOWLAnnotation(
            factory.getOWLAnnotationProperty(IRI.create("http://www.w3.org/2004/02/skos/core#altLabel")),
            factory.getOWLLiteral(valueDomain.getPreferredName().getValue())))));
        // Definition property
        manager.applyChange(new AddAxiom(ontology, factory.getOWLAnnotationAssertionAxiom(valueSetClass.getIRI(),
          factory.getOWLAnnotation(
            factory.getOWLAnnotationProperty(IRI.create("http://www.w3.org/2004/02/skos/core#definition")),
            factory.getOWLLiteral(valueDomain.getPreferredDefinition())))));

        // All the other annotation properties
        for (Field f : valueDomain.getClass().getDeclaredFields()) {
          if (f.getName().compareTo("_null") != 0) { // Ignore "NULL" field, which is present in some entities
            // Get annotation property
            OWLAnnotationProperty annProperty = factory
              .getOWLAnnotationProperty(IRI.create(ontologyIRI + "#" + f.getName()));
            // Create assertion
            String methodName = "get" + f.getName().substring(0, 1).toUpperCase() + f.getName().substring(1);
            // Get value
            String value = "";
            if (f.getType().equals(String.class)) {
              value = valueDomain.getClass().getMethod(methodName).invoke(valueDomain).toString().trim();
            } else if (typesToExplore.contains(f.getType().getSimpleName().toLowerCase())) {
              Object obj2 = valueDomain.getClass().getMethod(methodName).invoke(valueDomain);
              value = obj2.getClass().getMethod("getValue").invoke(obj2).toString().trim();
            }
            if (value.length() > 0) {
              OWLAnnotationValue annValue = factory.getOWLLiteral(value);
              OWLAnnotation annotation = factory.getOWLAnnotation(annProperty, annValue);
              AddAxiom addAxiom = new AddAxiom(ontology,
                factory.getOWLAnnotationAssertionAxiom(valueSetClass.getIRI(), annotation));
              manager.applyChange(addAxiom);
            }
          }
        }

        /*** CREATE VALUES FOR THE VALUE SET (as children classes) ***/
        for (PermissibleValue pv : valueDomain.getPermissibleValues().getPermissibleValuesITEM()) {
          // Create value class
          // IMPORTANT: generate id for each value because there is not id for it
          //String valueClassId = UUID.randomUUID().toString();
          String valueClassId = valueDomain.getPublicId().getValue() + "_" + pv.getVmPublicId();
//          String value = pv.getValidValue();
//          value = value.trim().replaceAll("\\s+", "_");
//          value = value.replaceAll("\\[", "-");
//          value = value.replaceAll("\\]", "-");
//          if (Character.isDigit(value.charAt(0))) {
//            value = "_" + value;
//          }
//          value = URLEncoder.encode(value, "UTF-8");
          OWLClass valueClass = factory.getOWLClass(IRI.create(ontologyIRI + "#" + valueClassId));
          OWLAxiom subClassAxiom = factory.getOWLSubClassOfAxiom(valueClass, valueSetClass);
          AddAxiom addAxiom = new AddAxiom(ontology, subClassAxiom);
          manager.applyChange(addAxiom);
          values_count++;

          // Default annotation properties
          // Preferred Name Property
          manager.applyChange(new AddAxiom(ontology, factory.getOWLAnnotationAssertionAxiom(valueClass.getIRI(),
            factory.getOWLAnnotation(
              factory.getOWLAnnotationProperty(IRI.create("http://www.w3.org/2004/02/skos/core#prefLabel")),
              factory.getOWLLiteral(pv.getValidValue())))));
          // Synonym Property
          manager.applyChange(new AddAxiom(ontology, factory.getOWLAnnotationAssertionAxiom(valueClass.getIRI(),
            factory.getOWLAnnotation(
              factory.getOWLAnnotationProperty(IRI.create("http://www.w3.org/2004/02/skos/core#altLabel")),
              factory.getOWLLiteral(pv.getValueMeaning())))));
          // Definition property
          manager.applyChange(new AddAxiom(ontology, factory.getOWLAnnotationAssertionAxiom(valueClass.getIRI(),
            factory.getOWLAnnotation(
              factory.getOWLAnnotationProperty(IRI.create("http://www.w3.org/2004/02/skos/core#definition")),
              factory.getOWLLiteral(pv.getMeaningDescription())))));

          // All the other annotation properties
          for (Field f : pv.getClass().getDeclaredFields()) {
            if (f.getName().compareTo("_null") != 0) { // Ignore "NULL" field, which is present in some entities
              // Get annotation property
              OWLAnnotationProperty annProperty = factory
                .getOWLAnnotationProperty(IRI.create(ontologyIRI + "#" + f.getName()));
              // Create assertion
              String methodName = "get" + f.getName().substring(0, 1).toUpperCase() + f.getName().substring(1);
              // Get value
              String value = "";
              if (f.getType().equals(String.class)) {
                value = pv.getClass().getMethod(methodName).invoke(pv).toString().trim();
              } else if (typesToExplore.contains(f.getType().getSimpleName().toLowerCase())) {
                Object obj2 = pv.getClass().getMethod(methodName).invoke(pv);
                value = obj2.getClass().getMethod("getValue").invoke(obj2).toString().trim();
              }
              if (value.length() > 0) {
                OWLAnnotationValue annValue = factory.getOWLLiteral(value);
                OWLAnnotation annotation = factory.getOWLAnnotation(annProperty, annValue);
                addAxiom = new AddAxiom(ontology,
                  factory.getOWLAnnotationAssertionAxiom(valueClass.getIRI(), annotation));
                manager.applyChange(addAxiom);
              }
            }
          }

        }

      }

    }

    // Save the ontology to the location where we loaded it from, in the default ontology format
    manager.saveOntology(ontology, ontologySaveIRI);
    System.out.println("Number of value sets created: " + value_sets_count);
    System.out.println("Number of values: " + values_count);

  }

}
