package org.bef.core.groovy;

import org.bef.core.Job;
import org.bef.core.JobParameterDef;
import org.bef.core.JobParameterDefAbstract;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by enrico on 5/4/16.
 */
public class JobParser {

    public <T> Job<T> parse(InputStream is) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(is);

        //optional, but recommended
        //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
        doc.getDocumentElement().normalize();

        String name = doc.getDocumentElement().getAttribute("name");
        Map<String,JobParameterDef<?>> parameterDefs = new LinkedHashMap<>();

        String runScript = getElementContent(doc.getDocumentElement(), "Run", true);

        NodeList parametersList = doc.getElementsByTagName("Parameter");
        String validateScript = getElementContent(doc.getDocumentElement(), "Validate", false);

        for (int i = 0; i < parametersList.getLength(); i++) {
            Element element = (Element) parametersList.item(i);
            String key = element.getAttribute("key");
            String parameterName = element.getAttribute("name");;
            String typeString = element.getAttribute("type");
            Class<?> type = Class.forName(typeString);

            String parameterValidateScript = getElementContent(element, "Validate", false);

            String createComponentScript = getElementContent(element, "CreateComponent", true);

            String onDependenciesChangeScript = getElementContent(element, "OnDependenciesChange", false);

            JobParameterDefAbstract<?> parameterDef = new JobParameterDefGroovy<>(key, parameterName, type,
                    createComponentScript, onDependenciesChangeScript, parameterValidateScript);
            parameterDefs.put(parameterDef.getKey(), parameterDef);

            final NodeList dependenciesList = element.getElementsByTagName("Dependency");
            for (int iDep = 0; iDep < dependenciesList.getLength(); iDep++) {
                final Element dependency = (Element) dependenciesList.item(iDep);
                final String depKey = dependency.getAttribute("key");
                parameterDef.addDependency(parameterDefs.get(depKey));
            }
        }

        return new JobGroovy<>(name, new ArrayList(parameterDefs.values()), runScript, validateScript);
    }

    private static String getElementContent(Element parent, String name, boolean mandatory) throws BefParseException {
        final NodeList childNodes = parent.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            final String nodeName = childNodes.item(i).getNodeName();
            if (nodeName.equals(name)) {
                return childNodes.item(i).getTextContent();
            }
        }
        if (mandatory) {
            throw new BefParseException("Cannot find " + name + " element in " + parent);
        } else {
            return null;
        }
    }
}
