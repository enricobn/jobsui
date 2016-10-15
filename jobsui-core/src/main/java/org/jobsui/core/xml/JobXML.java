package org.jobsui.core.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
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
    private int order;

    public JobXML(File file, String name) {
        this.file = file;
        this.name = name;

        String fileName = file.getName();
        int pos = fileName.lastIndexOf(".");
        if (pos > 0) {
            fileName = fileName.substring(0, pos);
        }
        this.key = fileName;
    }

    public void export() throws Exception {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        // root elements
        Document doc = docBuilder.newDocument();
        Element rootElement = doc.createElement("Job");
        doc.appendChild(rootElement);

        XMLUtils.addAttr(rootElement, "name", name);

        for (SimpleParameterXML parameter : simpleParameterXMLs) {
            Element element = createParameterElement(doc, rootElement, parameter);

            XMLUtils.addTextElement(element, "CreateComponent", parameter.getCreateComponentScript());

            if (validateScript != null && !validateScript.isEmpty()) {
                XMLUtils.addTextElement(element, "Validate", parameter.getValidateScript());
            }

            for (String dependency : parameter.getDependencies()) {
                Element dependencyComponent = doc.createElement("Dependency");
                element.appendChild(dependencyComponent);
                XMLUtils.addAttr(dependencyComponent, "key", dependency);
            }

            if (parameter.getOnDependenciesChangeScript() != null && !parameter.getOnDependenciesChangeScript().isEmpty()) {
                XMLUtils.addTextElement(element, "OnDependenciesChange", parameter.getOnDependenciesChangeScript());
            }
        }

        XMLUtils.write(doc, file);
    }

    private Element createParameterElement(Document doc, Element rootElement, ParameterXML parameter) {
        Element element = doc.createElement("Parameter");
        rootElement.appendChild(element);
        XMLUtils.addAttr(element, "key", parameter.getKey());
        XMLUtils.addAttr(element, "name", parameter.getName());
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
        parameterXML.setOrder(order);
        order += 1_000;
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
