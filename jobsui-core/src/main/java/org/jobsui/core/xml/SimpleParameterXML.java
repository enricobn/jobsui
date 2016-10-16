package org.jobsui.core.xml;

import org.jobsui.core.utils.JobsUIUtils;

import java.util.List;

/**
 * Created by enrico on 10/11/16.
 */
public class SimpleParameterXML extends ParameterXML {
    private String parameterValidateScript;
    private String createComponentScript;
    private String onDependenciesChangeScript;
    private boolean visible;
    private boolean optional;

    public SimpleParameterXML(String key, String name) {
        super(key, name);
    }

    public void setValidateScript(String parameterValidateScript) {
        this.parameterValidateScript = XMLUtils.scriptToEditForm(parameterValidateScript);
    }

    public void setCreateComponentScript(String createComponentScript) {
        this.createComponentScript = XMLUtils.scriptToEditForm(createComponentScript);
    }

    public void setOnDependenciesChangeScript(String onDependenciesChangeScript) {
        this.onDependenciesChangeScript = XMLUtils.scriptToEditForm(onDependenciesChangeScript);
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public String getValidateScript() {
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

    @Override
    public List<String> validate() {
        List<String> messages = super.validate();
        if (JobsUIUtils.isNullOrEmptyOrSpaces(createComponentScript)) {
            messages.add("Create component script is mandatory.");
        }
        return messages;
    }
}
