package org.jobsui.core.xml;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.XMLConstants;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.net.URL;

/**
 * Created by enrico on 10/12/16.
 */
public interface XMLUtils {

    static void write(Document doc, File file, URL schemaURL) throws Exception {
        // create a SchemaFactory capable of understanding WXS schemas
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

        // load a WXS schema, represented by a Schema instance
        Source schemaSource = new StreamSource(schemaURL.openStream());
        Schema schema = factory.newSchema(schemaSource);

        // create a Validator instance, which can be used to validate an instance document
        Validator validator = schema.newValidator();

        // validate the DOM tree
        validator.validate(new DOMSource(doc));

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        DOMSource source = new DOMSource(doc);
//        StreamResult result = new StreamResult(file);

        // Output to console for testing
        StreamResult result = new StreamResult(System.out);

        transformer.transform(source, result);
    }

    static void addAttr(Element element, String key, String value) {
        Document doc = element.getOwnerDocument();
        Attr attr = doc.createAttribute(key);
        attr.setValue(value);
        element.setAttributeNode(attr);
    }

    static Element addTextElement(Element parent, String name, String text) {
        Document doc = parent.getOwnerDocument();
        Element child = doc.createElement(name);
        parent.appendChild(child);
        child.appendChild(doc.createTextNode(text));
        return child;
    }

}
