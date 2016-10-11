package org.jobsui.core.xml;

/**
 * Created by enrico on 10/11/16.
 */
public class ExpressionXML extends ParameterXML {
    private String evaluateScript;

    public ExpressionXML(String key, String name, int order) {
        super(key, name, order);
    }

    public void setEvaluateScript(String evaluateScript) {
        this.evaluateScript = evaluateScript;
    }

    public String getEvaluateScript() {
        return evaluateScript;
    }

}
