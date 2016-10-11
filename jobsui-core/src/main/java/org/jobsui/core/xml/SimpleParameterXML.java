package org.jobsui.core.xml;

/**
 * Created by enrico on 10/11/16.
 */
public class SimpleParameterXML extends ParameterXML {
    private String parameterValidateScript;
    private String createComponentScript;
    private String onDependenciesChangeScript;
    private boolean visible;
    private boolean optional;

    public SimpleParameterXML(String key, String name, int order) {
        super(key, name, order);
    }

    public void setValidateScript(String parameterValidateScript) {
        this.parameterValidateScript = parameterValidateScript;
    }

    public void setCreateComponentScript(String createComponentScript) {
        this.createComponentScript = createComponentScript;
    }

    public void setOnDependenciesChangeScript(String onDependenciesChangeScript) {
        this.onDependenciesChangeScript = onDependenciesChangeScript;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public String getParameterValidateScript() {
        return parameterValidateScript;
    }

    public String getCreateComponentScript() {
        return createComponentScript;
    }

    public String getOnDependenciesChangeScript() {
        return onDependenciesChangeScript;
    }

    public boolean isVisible() {
        return visible;
    }

    public boolean isOptional() {
        return optional;
    }
}
