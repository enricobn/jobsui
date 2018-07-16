package org.jobsui.core.xml;

import org.jobsui.core.ui.UIComponentType;

/**
 * Created by enrico on 10/11/16.
 */
public class SimpleParameterXML extends ParameterXML {
    private String parameterValidateScript;
    private String onDependenciesChangeScript;
    private boolean visible;
    private boolean optional;
    private String onInitScript;
    private UIComponentType component;

    public SimpleParameterXML(String key, String name) {
        super(key, name);
    }

    public void setValidateScript(String parameterValidateScript) {
        this.parameterValidateScript = XMLUtils.scriptToEditForm(parameterValidateScript);
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

    public String getOnDependenciesChangeScript() {
        return onDependenciesChangeScript;
    }

    public boolean isVisible() {
        return visible;
    }

    @Override
    public ParameterXML copy() {
        SimpleParameterXML copy = new SimpleParameterXML(getKey() + "_copy", getName() + " copy");
        getDependencies().forEach(copy::addDependency);
        copy.parameterValidateScript = parameterValidateScript;
        copy.onDependenciesChangeScript = onDependenciesChangeScript;
        copy.visible = visible;
        copy.optional = optional;
        copy.onInitScript = onInitScript;
        copy.component = component;

        return copy;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOnInitScript(String onInitScript) {
        this.onInitScript = XMLUtils.scriptToEditForm(onInitScript);
    }

    public String getOnInitScript() {
        return onInitScript;
    }

//    public void setComponent(UIComponentType component) {
//        this.component = component;
//    }

    public UIComponentType getComponent() {
        return component;
    }

    public void setComponent(UIComponentType component) {
        this.component = component;
    }
}
