package org.jobsui.core.xml;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;

/**
 * Created by enrico on 10/12/16.
 */
public interface XMLUtils {

    static void write(Document doc, File file) throws Exception {
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

    static void addAttr(Document doc, Element element, String key, String value) {
        Attr attr = doc.createAttribute(key);
        attr.setValue(value);
        element.setAttributeNode(attr);

    }

}
