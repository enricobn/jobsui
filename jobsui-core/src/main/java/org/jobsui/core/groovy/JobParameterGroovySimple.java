package org.jobsui.core.groovy;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.jobsui.core.job.JobParameterAbstract;
import org.jobsui.core.ui.*;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by enrico on 5/4/16.
 */
public class JobParameterGroovySimple extends JobParameterAbstract implements JobParameterGroovy {
    private final Script onInit;
    private final Script onDependenciesChange;
    private final Script validate;
    private final Binding shellBinding;
    private final UIComponentType componentType;

    public JobParameterGroovySimple(GroovyShell shell, String key, String name, UIComponentType componentType,
                                    String onInitScript, String onDependenciesChangeScript,
                                    String validateScript, boolean optional, boolean visible) {
        super(key, name, null, optional, visible);
        this.componentType = componentType;
        Objects.requireNonNull(componentType);
        try {
            this.onInit = onInitScript == null ? null : shell.parse(onInitScript);
        } catch (Exception e) {
            throw new RuntimeException("Error parsing onInit for parameter with key \"" + key + "\".", e);
        }
        try {
            this.onDependenciesChange = onDependenciesChangeScript == null ? null : shell.parse(onDependenciesChangeScript);
        } catch (Exception e) {
            throw new RuntimeException("Error parsing onDependenciesChange for parameter with key \"" + key + "\".", e);
        }
        try {
            this.validate = validateScript == null ? null : shell.parse(validateScript);
        } catch (Exception e) {
            throw new RuntimeException("Error parsing validate for parameter with key \"" + key + "\".", e);
        }
        shellBinding = shell.getContext();
    }

    @Override
    public <C> UIComponent<C> createComponent(UI<C> ui) throws UnsupportedComponentException {
        UIComponent uiComponent;

        switch (componentType) {
            case Value: uiComponent = ui.create(UIValue.class); break;
            case Password: uiComponent = ui.create(UIPassword.class); break;
            case Choice: uiComponent = ui.create(UIChoice.class); break;
            case CheckBox: uiComponent = ui.create(UICheckBox.class); break;
            case Button: uiComponent = ui.create(UIButton.class); break;
            case List: uiComponent = ui.create(UIList.class); break;
            default: throw new IllegalArgumentException("Cannot instantiate component " + componentType);
        }

        if (onInit != null) {
            // I reset the bindings otherwise I get "global" or previous bindings
            onInit.setBinding(new Binding(shellBinding.getVariables()));
            onInit.setProperty("ui", ui);
            onInit.setProperty("component", uiComponent);
            try {
                onInit.run();
            } catch (Throwable e) {
                throw new RuntimeException("Error in onInit script for parameter whit key \"" +
                        getKey() + "\"", e);
            }
        }
        return uiComponent;
    }

    @Override
    public void onDependenciesChange(UIWidget widget, Map<String, Serializable> values) {
        if (onDependenciesChange != null) {
            // I reset the bindings otherwise I get "global" or previous bindings
            onDependenciesChange.setBinding(new Binding());
            onDependenciesChange.setProperty("widget", widget);
            onDependenciesChange.setProperty("values", values);
            for (Map.Entry<String, Serializable> entry : values.entrySet()) {
                onDependenciesChange.setProperty(entry.getKey(), entry.getValue());
            }
            try {
                onDependenciesChange.run();
            } catch (Exception e) {
                throw new RuntimeException("Error in onDependenciesChange script for parameter whit key \"" +
                        getKey() + "\"", e);
            }
        }
    }

    @Override
    public List<String> validate(Map<String, Serializable> values, Serializable value) {
        if (validate == null) {
            final List<String> validation = super.validate(values, value);
            if (!validation.isEmpty()) {
                return validation;
            }
            return Collections.emptyList();
        }
        // I reset the bindings otherwise I get "global" or previous bindings
        validate.setBinding(new Binding());
        validate.setProperty("value", value);
        validate.setProperty("values", values);
        try {
            @SuppressWarnings("unchecked")
            List<String> result = (List<String>) validate.run();
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Error in validate script for parameter whit key \"" +
                    getKey() + "\"", e);
        }
    }

    @Override
    public void init(ProjectGroovy projectGroovy) {
    }

}
