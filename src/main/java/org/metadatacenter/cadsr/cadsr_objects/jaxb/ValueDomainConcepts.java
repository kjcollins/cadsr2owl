//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.10.14 at 05:38:06 PM PDT 
//


package org.metadatacenter.cadsr.cadsr_objects.jaxb;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "valueDomainConceptsITEM"
})
@XmlRootElement(name = "ValueDomainConcepts")
public class ValueDomainConcepts {

    @XmlAttribute(name = "NULL")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String _null;
    @XmlElement(name = "ValueDomainConcepts_ITEM")
    protected List<ValueDomainConcept> valueDomainConceptsITEM;

    /**
     * Gets the value of the null property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNULL() {
        if (_null == null) {
            return "TRUE";
        } else {
            return _null;
        }
    }

    /**
     * Sets the value of the null property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNULL(String value) {
        this._null = value;
    }

    /**
     * Gets the value of the valueDomainConceptsITEM property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the valueDomainConceptsITEM property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getValueDomainConceptsITEM().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ValueDomainConcept }
     * 
     * 
     */
    public List<ValueDomainConcept> getValueDomainConceptsITEM() {
        if (valueDomainConceptsITEM == null) {
            valueDomainConceptsITEM = new ArrayList<ValueDomainConcept>();
        }
        return this.valueDomainConceptsITEM;
    }

}
