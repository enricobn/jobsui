package org.jobsui.core.xml;

import org.jobsui.core.groovy.JobParser;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.util.*;

/**
 * Created by enrico on 10/11/16.
 */
public class JobXML {
    private final List<SimpleParameterXML> simpleParameterXMLs = new ArrayList<>();
    private final List<ExpressionXML> expressionXMLs = new ArrayList<>();
    private final List<CallXML> callXMLs = new ArrayList<>();
    private final Map<String, ParameterXML> parameters = new HashMap<>();

    private final File file;
    private final String key;
    private final String name;
    private String runScript;
    private String validateScript;

    public JobXML(File file, String key, String name) {
        this.file = file;
        this.key = key;
        this.name = name;
    }

    public void export() throws Exception {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        // root elements
        Document doc = docBuilder.newDocument();
        Element rootElement = doc.createElement("Job");
        doc.appendChild(rootElement);

        Attr attr = doc.createAttribute("key");
        attr.setValue(key);
        rootElement.setAttributeNode(attr);

        attr = doc.createAttribute("name");
        attr.setValue(name);
        rootElement.setAttributeNode(attr);

        for (SimpleParameterXML parameter : simpleParameterXMLs) {
            Element element = createParameterElement(doc, rootElement, parameter);

            Element createComponent = doc.createElement("CreateComponent");
            element.appendChild(createComponent);
            createComponent.appendChild(doc.createTextNode(parameter.getCreateComponentScript()));

            if (validateScript != null && !validateScript.isEmpty()) {
                Element validateComponent = doc.createElement("Validate");
                element.appendChild(validateComponent);
                validateComponent.appendChild(doc.createTextNode(parameter.getValidateScript()));
            }

            for (String dependency : parameter.getDependencies()) {
                Element dependencyComponent = doc.createElement("Dependency");
                element.appendChild(dependencyComponent);
                XMLUtils.addAttr(doc, dependencyComponent, "key", dependency);
            }

            if (parameter.getOnDependenciesChangeScript() != null && !parameter.getOnDependenciesChangeScript().isEmpty()) {
                Element onDependenciesChange = doc.createElement("OnDependenciesChange");
                element.appendChild(onDependenciesChange);
                onDependenciesChange.appendChild(doc.createTextNode(parameter.getOnDependenciesChangeScript()));
            }
        }

        XMLUtils.write(doc, file);
    }

    private Element createParameterElement(Document doc, Element rootElement, ParameterXML parameter) {
        Element element = doc.createElement("Parameter");
        rootElement.appendChild(element);
        XMLUtils.addAttr(doc, element, "key", parameter.getKey());
        XMLUtils.addAttr(doc, element, "name", parameter.getName());
        return element;
    }

    public void setRunScript(String runScript) {
        this.runScript = runScript;
    }

    public void setValidateScript(String validateScript) {
        this.validateScript = validateScript;
    }

    public void add(SimpleParameterXML simpleParameterXML) throws Exception {
        simpleParameterXMLs.add(simpleParameterXML);
        addCheckedParameter(simpleParameterXML);
    }

    public void add(ExpressionXML expressionXML) throws Exception {
        expressionXMLs.add(expressionXML);
        addCheckedParameter(expressionXML);
    }

    public void add(CallXML callXML) throws Exception {
        callXMLs.add(callXML);
        addCheckedParameter(callXML);
    }

    public String getRunScript() {
        return runScript;
    }

    public String getValidateScript() {
        return validateScript;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public List<SimpleParameterXML> getSimpleParameterXMLs() {
        return simpleParameterXMLs;
    }

    public List<ExpressionXML> getExpressionXMLs() {
        return expressionXMLs;
    }

    public List<CallXML> getCallXMLs() {
        return callXMLs;
    }

    private void addCheckedParameter(ParameterXML parameterXML) throws Exception {
        if (parameters.put(parameterXML.getKey(), parameterXML) != null) {
            throw new Exception("Duplicate parameter key '" + parameterXML.getKey() + "' for parameter " +
                    "with name '" + parameterXML.getName() + "'.");
        }
    }

    public List<String> getSortedParameters() throws Exception {
        Map<String, List<String>> toSort = new LinkedHashMap<>();

        parameters.values().stream()
            .sorted(Comparator.comparing(ParameterXML::getOrder))
            .forEach(parameterXML -> toSort.put(parameterXML.getKey(), new ArrayList<>(parameterXML.getDependencies())));

        List<String> sorted = new ArrayList<>();

        while (!toSort.isEmpty()) {
            boolean found = false;
            for (Map.Entry<String, List<String>> entry : toSort.entrySet()) {
                if (entry.getValue().isEmpty()) {
                    sorted.add(entry.getKey());
                    toSort.remove(entry.getKey());
                    for (List<String> dependencies : toSort.values()) {
                        dependencies.remove(entry.getKey());
                    }
                    found = true;
                    break;
                }
            }
            if (!found) {
                StringBuilder sb = new StringBuilder("Unresolved dependencies:\n");
                toSort.entrySet().stream()
                        .forEach(entry -> sb.append(entry.getKey()).append(":").append(entry.getValue()).append('\n'));
                throw new Exception(sb.toString());
            }
        }
        return sorted;
    }

    public ParameterXML getParameter(String key) {
        return parameters.get(key);
    }
}
