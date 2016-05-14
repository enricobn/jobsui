package org.bef.core.groovy;

import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.bef.core.JobParameterDefAbstract;
import org.bef.core.ui.UI;
import org.bef.core.ui.UIComponent;
import org.bef.core.ui.UIWidget;
import org.bef.core.ui.UnsupportedComponentException;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by enrico on 5/4/16.
 */
public class JobParameterDefGroovy<T> extends JobParameterDefAbstract<T> {
    private static final String IMPORTS =
            "import org.bef.core.*;\n" +
            "import org.bef.core.ui.*;\n";
    private final Script createComponent;
    private final Script onDependenciesChange;
    private final Script validate;

    public JobParameterDefGroovy(GroovyShell shell, String key, String name,
                                 String createComponentScript, String onDependenciesChangeScript,
                                 String validateScript, boolean optional, boolean visible) {
        super(key, name, null, optional, visible);
        this.createComponent = shell.parse(IMPORTS + createComponentScript);
        this.onDependenciesChange = shell.parse(IMPORTS + onDependenciesChangeScript);
        this.validate =validateScript == null ? null : shell.parse(IMPORTS + validateScript);
    }

    @Override
    public UIComponent createComponent(UI ui) throws UnsupportedComponentException {
        createComponent.setProperty("ui", ui);
        try {
            return (UIComponent) createComponent.run();
        } catch (Throwable e) {
            throw new RuntimeException("Error in createComponent script for parameter whit key \"" +
                    getKey() + "\"", e);
        }
    }

    @Override
    public void onDependenciesChange(UIWidget widget, Map<String, Object> values) {
        if (onDependenciesChange != null) {
            onDependenciesChange.setProperty("widget", widget);
            onDependenciesChange.setProperty("values", values);
            try {
                onDependenciesChange.run();
            } catch (Throwable e) {
                throw new RuntimeException("Error in onDependenciesChange script for parameter whit key \"" +
                        getKey() + "\"", e);
            }
        }
    }

    @Override
    public List<String> validate(T value) {
        final List<String> validation = super.validate(value);
        if (!validation.isEmpty()) {
            return validation;
        }
        if (this.validate == null) {
            return Collections.emptyList();
        }
        this.validate.setProperty("value", value);
        try {
            return (List<String>) this.validate.run();
        } catch (Throwable e) {
            throw new RuntimeException("Error in validate script for parameter whit key \"" +
                    getKey() + "\"", e);
        }
    }
}
