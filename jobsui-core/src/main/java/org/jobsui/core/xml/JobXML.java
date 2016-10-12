package org.jobsui.core.xml;

import java.util.*;

/**
 * Created by enrico on 10/11/16.
 */
public class JobXML {
    private final List<SimpleParameterXML> simpleParameterXMLs = new ArrayList<>();
    private final List<ExpressionXML> expressionXMLs = new ArrayList<>();
    private final List<CallXML> callXMLs = new ArrayList<>();
    private final Map<String, ParameterXML> parameters = new HashMap<>();

    private final String key;
    private final String name;
    private String runScript;
    private String validateScript;

    public JobXML(String key, String name) {
        this.key = key;
        this.name = name;
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
