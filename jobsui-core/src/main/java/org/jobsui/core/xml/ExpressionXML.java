package org.jobsui.core.xml;

import java.util.List;

/**
 * Created by enrico on 10/11/16.
 */
public class ExpressionXML extends ParameterXML {
    private String evaluateScript;

    public ExpressionXML(String key, String name) {
        super(key, name);
    }

    public void setEvaluateScript(String evaluateScript) {
        this.evaluateScript = evaluateScript;
    }

    public String getEvaluateScript() {
        return evaluateScript;
    }

    @Override
    public List<String> validate() {
        List<String> messages = super.validate();
        if (evaluateScript == null || evaluateScript.isEmpty()) {
            messages.add("Evaluate script is mandatory.");
        }
        return messages;
    }
}
