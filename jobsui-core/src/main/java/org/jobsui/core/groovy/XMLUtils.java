package org.jobsui.core.groovy;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Created by enrico on 4/5/17.
 */
public abstract class XMLUtils {

    private XMLUtils() {

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
