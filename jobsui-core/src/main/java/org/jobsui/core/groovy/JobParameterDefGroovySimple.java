package org.jobsui.core.groovy;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.jobsui.core.job.JobParameterDefAbstract;
import org.jobsui.core.ui.*;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by enrico on 5/4/16.
 */
public class JobParameterDefGroovySimple extends JobParameterDefAbstract implements JobParameterDefGroovy {
//    private static final String IMPORTS =
//            "import org.jobsui.core.*;\n" +
//            "import org.jobsui.core.ui.*;\n";
//    private final File projectFolder;
//    private final String createComponentScript;
    private final String onDependenciesChangeScript;
    private final String validateScript;
    private final Script onInit;
    private final Script onDependenciesChange;
    private final Script validate;
    private final Binding shellBinding;
    private final String component;
    private final String onInitScript;

    public JobParameterDefGroovySimple(GroovyShell shell, String key, String name, String component,
                                       String onInitScript, String onDependenciesChangeScript,
                                       String validateScript, boolean optional, boolean visible) {
        super(key, name, null, optional, visible);
        this.component = component;
        this.onInitScript = onInitScript;
//        Objects.requireNonNull(createComponentScript);
//        this.projectFolder = projectFolder;
//        this.createComponentScript = createComponentScript;
        this.onDependenciesChangeScript = onDependenciesChangeScript;
        this.validateScript = validateScript;
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

        switch (component) {
            case "Value": uiComponent = ui.create(UIValue.class); break;
            case "Choice": uiComponent = ui.create(UIChoice.class); break;
            case "CheckBox": uiComponent = ui.create(UICheckBox.class); break;
            case "Button": uiComponent = ui.create(UIButton.class); break;
            case "List": uiComponent = ui.create(UIList.class); break;
            default: throw new IllegalArgumentException("Cannot instantiate component " + component);
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
//            onDependenciesChange.setProperty("projectFolder", projectFolder);
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
//        validate.setProperty("projectFolder", projectFolder);
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

    public String getOnDependenciesChangeScript() {
        return onDependenciesChangeScript;
    }

    public String getValidateScript() {
        return validateScript;
    }
}
