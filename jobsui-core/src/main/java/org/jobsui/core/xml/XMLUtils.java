package org.jobsui.core.xml;

import org.jobsui.core.groovy.JobsUIParseException;
import org.jobsui.core.utils.JobsUIUtils;
import org.w3c.dom.*;

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
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by enrico on 10/12/16.
 */
public interface XMLUtils {
    int INDENT_SIZE = 4;

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
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", String.valueOf(INDENT_SIZE));
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(file);

        // Output to console for testing
//        StreamResult result = new StreamResult(System.out);

        transformer.transform(source, result);
    }

    static void addAttr(Element element, String key, String value) {
        Document doc = element.getOwnerDocument();
        Attr attr = doc.createAttribute(key);
        attr.setValue(value);
        element.setAttributeNode(attr);
    }

    static Element addTextElement(Element parent, String name, String text, boolean indent) {
        Document doc = parent.getOwnerDocument();
        Element child = doc.createElement(name);
        parent.appendChild(child);
        addTextNode(child, text, indent);
        return child;
    }

    static void addTextNode(Element parent, String text, boolean indent) {
        Document doc = parent.getOwnerDocument();
        if (indent) {
            int parents = countParents(parent);
            String prefix = String.join("", Collections.nCopies((parents) * INDENT_SIZE, " "));
            String suffix = String.join("", Collections.nCopies((parents - 1) * INDENT_SIZE, " "));
            text = scriptToEditForm(text);
            text = System.lineSeparator() + Arrays.stream(text.split(System.lineSeparator()))
                    .map(s -> prefix + s)
                    .collect(Collectors.joining(System.lineSeparator())) + System.lineSeparator() + suffix;
        }
        parent.appendChild(doc.createTextNode(text));
    }


    static int countParents(Node element) {
        if (element.getParentNode() != null) {
            return 1 + countParents(element.getParentNode());
        }
        return 0;
    }

    static String scriptToEditForm(String script) {
        if (script == null) {
            return null;
        }
        String normalized = script.replaceAll("\\r\\n", "\n");
        List<String> newLines;
        try (StringReader reader = new StringReader(normalized); BufferedReader bufferedReader = new BufferedReader(reader)) {
            List<String> lines = bufferedReader.lines().collect(Collectors.toList());
            newLines = new ArrayList<>(lines.size());
            boolean start = true;
            int spaces = 0;
            for (String line : lines) {
                String trimmed = line.trim();
                if (start) {
                    if (trimmed.isEmpty()) {
                        continue;
                    }
                    spaces = JobsUIUtils.leadingSpaces(line);
                    newLines.add(trimmed);
                    start = false;
                } else {
                    int leadingSpaces = JobsUIUtils.leadingSpaces(line);
                    int count = leadingSpaces - spaces;
                    if (count > 0) {
                        newLines.add(JobsUIUtils.spaces(count) + trimmed);
                    } else {
                        newLines.add(trimmed);
                    }
                }
            }
        } catch (IOException e) {
            return script;
        }

        int i = newLines.size() -1;

        while (i >= 0 && newLines.get(i).isEmpty()) {
            newLines.remove(i);
            i--;
        }

        StringBuilder sb = new StringBuilder();
        newLines.forEach(line -> sb.append(line).append('\n'));
        return sb.toString();
    }

    static String getElementContent(Element parent, String name, boolean mandatory, String subject) throws JobsUIParseException {
        final NodeList childNodes = parent.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            final String nodeName = childNodes.item(i).getNodeName();
            if (nodeName.equals(name)) {
                return childNodes.item(i).getTextContent();
            }
        }
        if (mandatory) {
//            if (parent.getUserData("lineNumber") != null) {
//                throw new JobsUIParseException("Cannot find mandatory element \"" + name + "\" in " + parent +
//                        parent.getUserData("lineNumber"));
//            } else {
            throw new JobsUIParseException("Cannot find mandatory element \"" + name + "\" in " + subject);
//            }
        } else {
            return null;
        }
    }

    static String getMandatoryAttribute(Element element, String name, String subject) throws JobsUIParseException {
        final String attribute = element.getAttribute(name);

        if (attribute == null || attribute.length() == 0) {
//            if (parent instanceof DeferredNode) {
//                throw new JobsUIParseException("Cannot find mandatory attribute \"" + name + "\" in " + parent + " at line " +
//                        ((DeferredNode)parent).getNodeIndex());
//            } else {
            throw new JobsUIParseException("Cannot find mandatory attribute \"" + name + "\" in " + subject);
//            }
        }
        return attribute;
    }


}
