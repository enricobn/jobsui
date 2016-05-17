package org.bef.core.groovy;

import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.bef.core.JobParameterDefAbstract;
import org.bef.core.ui.UI;
import org.bef.core.ui.UIComponent;
import org.bef.core.ui.UIWidget;
import org.bef.core.ui.UnsupportedComponentException;

import java.io.File;
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
    private final File projectFolder;
    private final Script createComponent;
    private final Script onDependenciesChange;
    private final Script validate;

    public JobParameterDefGroovy(File projectFolder, GroovyShell shell, String key, String name,
                                 String createComponentScript, String onDependenciesChangeScript,
                                 String validateScript, boolean optional, boolean visible) {
        super(key, name, null, optional, visible);
        this.projectFolder = projectFolder;
        this.createComponent = shell.parse(IMPORTS + createComponentScript);
        this.onDependenciesChange = shell.parse(IMPORTS + onDependenciesChangeScript);
        this.validate =validateScript == null ? null : shell.parse(IMPORTS + validateScript);
    }

    @Override
    public UIComponent createComponent(UI ui) throws UnsupportedComponentException {
        createComponent.setProperty("ui", ui);
        createComponent.setProperty("projectFolder", projectFolder);
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
            onDependenciesChange.setProperty("projectFolder", projectFolder);
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
        if (validate == null) {
            final List<String> validation = super.validate(value);
            if (!validation.isEmpty()) {
                return validation;
            }
            return Collections.emptyList();
        }
        validate.setProperty("value", value);
        validate.setProperty("projectFolder", projectFolder);
        try {
            return (List<String>) validate.run();
        } catch (Throwable e) {
            throw new RuntimeException("Error in validate script for parameter whit key \"" +
                    getKey() + "\"", e);
        }
    }
}
