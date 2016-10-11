package org.jobsui.core.xml;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by enrico on 10/11/16.
 */
public class JobXML {
    private final List<SimplePararameterXML> simplePararameterXMLs = new ArrayList<>();
    private final List<ExpressionXML> expressionXMLs = new ArrayList<>();
    private final List<CallXML> callXMLs = new ArrayList<>();

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

    public void add(SimplePararameterXML simplePararameterXML) {
        simplePararameterXMLs.add(simplePararameterXML);
    }

    public void add(ExpressionXML expressionXML) {
        expressionXMLs.add(expressionXML);
    }

    public void add(CallXML callXML) {
        callXMLs.add(callXML);
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

    public List<SimplePararameterXML> getSimplePararameterXMLs() {
        return simplePararameterXMLs;
    }

    public List<ExpressionXML> getExpressionXMLs() {
        return expressionXMLs;
    }

    public List<CallXML> getCallXMLs() {
        return callXMLs;
    }
}
