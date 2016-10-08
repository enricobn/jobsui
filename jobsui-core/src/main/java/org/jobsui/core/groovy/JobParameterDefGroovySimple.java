package org.jobsui.core.groovy;

import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.jobsui.core.JobParameterDefAbstract;
import org.jobsui.core.ui.UI;
import org.jobsui.core.ui.UIComponent;
import org.jobsui.core.ui.UIWidget;
import org.jobsui.core.ui.UnsupportedComponentException;
import org.codehaus.groovy.control.CompilationFailedException;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by enrico on 5/4/16.
 */
public class JobParameterDefGroovySimple<T> extends JobParameterDefAbstract<T> implements JobParameterDefGroovy<T> {
    private static final String IMPORTS =
            "import org.jobsui.core.*;\n" +
            "import org.jobsui.core.ui.*;\n";
    private final File projectFolder;
    private final Script createComponent;
    private final Script onDependenciesChange;
    private final Script validate;

    public JobParameterDefGroovySimple(File projectFolder, GroovyShell shell, String key, String name,
                                       String createComponentScript, String onDependenciesChangeScript,
                                       String validateScript, boolean optional, boolean visible) {
        super(key, name, null, optional, visible);
        this.projectFolder = projectFolder;
        try {
            this.createComponent = shell.parse(IMPORTS + createComponentScript);
        } catch (CompilationFailedException e) {
            throw new RuntimeException("Error parsing createComponent for parameter with key \"" + key + "\".", e);
        }
        try {
            this.onDependenciesChange = shell.parse(IMPORTS + onDependenciesChangeScript);
        } catch (CompilationFailedException e) {
            throw new RuntimeException("Error parsing onDependenciesChange for parameter with key \"" + key + "\".", e);
        }
        try {
            this.validate = validateScript == null ? null : shell.parse(IMPORTS + validateScript);
        } catch (CompilationFailedException e) {
            throw new RuntimeException("Error parsing validate for parameter with key \"" + key + "\".", e);
        }
    }

    @Override
    public <C> UIComponent<T, C> createComponent(UI<C> ui) throws UnsupportedComponentException {
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
            for (Map.Entry<String, Object> entry : values.entrySet()) {
                onDependenciesChange.setProperty(entry.getKey(), entry.getValue());
            }
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

    @Override
    public void init(ProjectGroovy projectGroovy) {

    }
}
