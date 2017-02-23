package org.jobsui.core.xml;

import org.jobsui.core.utils.JobsUIUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by enrico on 10/11/16.
 */
public class JobXML implements ValidatingXML {
    private final List<SimpleParameterXML> simpleParameterXMLs = new ArrayList<>();
    private final List<ExpressionXML> expressionXMLs = new ArrayList<>();
    private final List<CallXML> callXMLs = new ArrayList<>();
    private final Map<String, ParameterXML> parameters = new HashMap<>();

    private final File file;
    private final String id;
    private final String version;
    private String name;
    private String runScript;
    private String validateScript;
    private int order;

    public JobXML(File file, String id, String name, String version) {
        this.file = file;
        this.name = name;

        String fileName = file.getName();
        int pos = fileName.lastIndexOf(".");
        if (pos > 0) {
            fileName = fileName.substring(0, pos);
        }
        this.id = id;
        this.version = version;
    }

    public void export() throws Exception {
        List<String> validate = validate();

        if (!validate.isEmpty()) {
            throw new Exception("Invalid job \"" + name + "\":\n" + validate.stream().collect(Collectors.joining("\n ")));
        }

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        // root elements
        Document doc = docBuilder.newDocument();
        Element rootElement = doc.createElement("Job");
        doc.appendChild(rootElement);

        XMLUtils.addAttr(rootElement, "name", name);

        for (SimpleParameterXML parameter : simpleParameterXMLs) {
            Element element = createParameterElement(doc, rootElement, parameter);

            if (parameter.getCreateComponentScript() != null && !parameter.getCreateComponentScript().isEmpty()) {
                XMLUtils.addTextElement(element, "CreateComponent", parameter.getCreateComponentScript());
            }

            if (!JobsUIUtils.isNullOrEmptyOrSpaces(parameter.getValidateScript())) {
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

        // TODO Expressions
        // TODO Calls

        if (!JobsUIUtils.isNullOrEmptyOrSpaces(validateScript)) {
            XMLUtils.addTextElement(rootElement, "Validate", validateScript);
        }

        if (!JobsUIUtils.isNullOrEmptyOrSpaces(runScript)) {
            XMLUtils.addTextElement(rootElement, "Run", getRunScript());
        }

        try {
            XMLUtils.write(doc, file, getClass().getResource("/org/jobsui/job.xsd"));
        } catch (Exception e) {
            throw new Exception("Error exporting job \"" + getName() + "\".", e);
        }
    }

    private Element createParameterElement(Document doc, Element rootElement, ParameterXML parameter) {
        Element element = doc.createElement("Parameter");
        rootElement.appendChild(element);
        XMLUtils.addAttr(element, "key", parameter.getKey());
        XMLUtils.addAttr(element, "name", parameter.getName());
        return element;
    }

    public void setRunScript(String runScript) {
        this.runScript = XMLUtils.scriptToEditForm(runScript);
    }

    public void setValidateScript(String validateScript) {
        this.validateScript = XMLUtils.scriptToEditForm(validateScript);
    }

    public void add(SimpleParameterXML simpleParameterXML) throws Exception {
        addCheckedParameter(simpleParameterXML);
        simpleParameterXMLs.add(simpleParameterXML);
    }

    public void add(ExpressionXML expressionXML) throws Exception {
        addCheckedParameter(expressionXML);
        expressionXMLs.add(expressionXML);
    }

    public void add(CallXML callXML) throws Exception {
        addCheckedParameter(callXML);
        callXMLs.add(callXML);
    }

    public String getRunScript() {
        return runScript;
    }

    public String getValidateScript() {
        return validateScript;
    }

    public String getId() {
        return id;
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
                toSort.entrySet()
                        .forEach(entry -> sb.append(entry.getKey()).append(":").append(entry.getValue()).append('\n'));
                throw new Exception(sb.toString());
            }
        }
        return sorted;
    }

    public ParameterXML getParameter(String key) {
        return parameters.get(key);
    }

    public void changeParameterKey(ParameterXML parameterXML, String newKey) {
        String oldKey = parameterXML.getKey();
        parameterXML.setKey(newKey);
        parameters.remove(oldKey);
        parameters.put(newKey, parameterXML);
        parameters.values().stream()
                .filter(parameter -> parameter.removeDependency(oldKey))
                .forEach(parameter -> parameter.addDependency(newKey));
    }

    @Override
    public List<String> validate() {
        List<String> messages = new ArrayList<>(0);
        if (JobsUIUtils.isNullOrEmptyOrSpaces(id)) {
            messages.add("Id is mandatory.");
        }

        if (JobsUIUtils.isNullOrEmptyOrSpaces(name)) {
            messages.add("Name is mandatory.");
        }

        if (JobsUIUtils.isNullOrEmptyOrSpaces(version)) {
            messages.add("Version is mandatory.");
        }


        for (ParameterXML parameterXML : parameters.values()) {
            List<String> parameterMessages = parameterXML.validate();
            if (!parameterMessages.isEmpty()) {
                messages.add(parameterXML.getName() + ": " + parameterMessages.stream().collect(Collectors.joining(" ")));
            }
        }

        // TODO dependencies

        if (JobsUIUtils.isNullOrEmptyOrSpaces(runScript)) {
            messages.add("Run script is mandatory.");
        }


        return messages;
    }

    public void setName(String name) {
        this.name = name;
    }
}
