package org.jobsui.core.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
}
