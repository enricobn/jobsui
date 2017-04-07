package org.jobsui.core.xml;

import org.jobsui.core.utils.JobsUIUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by enrico on 10/11/16.
 */
public class JobXMLImpl implements ValidatingXML, JobXML {
    private final List<SimpleParameterXML> simpleParameterXMLs = new ArrayList<>();
    private final List<ExpressionXML> expressionXMLs = new ArrayList<>();
    private final List<CallXML> callXMLs = new ArrayList<>();
    private final Map<String, ParameterXML> parameters = new HashMap<>();
    private final Map<String, ExpressionXML> expressions = new HashMap<>();

    private final String id;
    private final String version;
    private String name;
    private String runScript;
    private String validateScript;
    private int order;

    public JobXMLImpl(String id, String name, String version) {
        this.name = name;
        this.id = id;
        this.version = version;
    }

    public void export(File file) throws Exception {
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

            if (parameter.getOnInitScript() != null && !parameter.getOnInitScript().isEmpty()) {
                XMLUtils.addTextElement(element, "OnInit", parameter.getOnInitScript());
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
        addCheckedExpression(expressionXML);
        expressionXMLs.add(expressionXML);
    }

    public void add(CallXML callXML) throws Exception {
        addCheckedParameter(callXML);
        callXMLs.add(callXML);
    }

    @Override
    public String getRunScript() {
        return runScript;
    }

    @Override
    public String getValidateScript() {
        return validateScript;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<SimpleParameterXML> getSimpleParameterXMLs() {
        return simpleParameterXMLs;
    }

    @Override
    public List<ExpressionXML> getExpressionXMLs() {
        return expressionXMLs;
    }

    @Override
    public List<CallXML> getCallXMLs() {
        return callXMLs;
    }

    private void addCheckedParameter(ParameterXML parameterXML) throws Exception {
        if (expressions.containsKey(parameterXML.getKey())) {
            throw new Exception("Duplicate key '" + parameterXML.getKey() + "' for parameter " +
                    "with name '" + parameterXML.getName() + "'.");
        }

        if (parameters.put(parameterXML.getKey(), parameterXML) != null) {
            throw new Exception("Duplicate key '" + parameterXML.getKey() + "' for parameter " +
                    "with name '" + parameterXML.getName() + "'.");
        }
        parameterXML.setOrder(order);
        order += 1_000;
    }

    private void addCheckedExpression(ExpressionXML expressionXML) throws Exception {
        if (parameters.containsKey(expressionXML.getKey())) {
            throw new Exception("Duplicate key '" + expressionXML.getKey() + "' for expression " +
                    "with name '" + expressionXML.getName() + "'.");
        }
        if (expressions.put(expressionXML.getKey(), expressionXML) != null) {
            throw new Exception("Duplicate key '" + expressionXML.getKey() + "' for expression " +
                    "with name '" + expressionXML.getName() + "'.");
        }
    }

    @Override
    public ParameterXML getParameter(String key) {
        if (!parameters.containsKey(key)) {
            return expressions.get(key);
        }
        return parameters.get(key);
    }

    @Override
    public ExpressionXML getExpression(String key) {
        return expressions.get(key);
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