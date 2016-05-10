package org.bef.core.groovy;

import groovy.lang.GroovyShell;
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
    private final GroovyShell shell;
    private final String createComponentScript;
    private final String onDependenciesChangeScript;
    private final String validateScript;

    public JobParameterDefGroovy(GroovyShell shell, String key, String name, Class<T> type,
                                 String createComponentScript, String onDependenciesChangeScript, String validateScript) {
        super(key, name, type, null);
        this.shell = shell;
        this.createComponentScript = createComponentScript;
        this.onDependenciesChangeScript = onDependenciesChangeScript;
        this.validateScript = validateScript;
    }

    @Override
    public UIComponent createComponent(UI ui) throws UnsupportedComponentException {
        shell.setProperty("ui", ui);
        try {
            return (UIComponent) shell.evaluate(IMPORTS + createComponentScript);
        } catch (Throwable e) {
            throw new RuntimeException("Error in createComponent script for parameter whit key \"" +
                    getKey() + "\"", e);
        }
    }

    @Override
    public void onDependenciesChange(UIWidget widget, Map<String, Object> values) {
        if (onDependenciesChangeScript != null) {
            shell.setProperty("widget", widget);
            shell.setProperty("values", values);
            try {
                shell.evaluate(IMPORTS + onDependenciesChangeScript);
            } catch (Throwable e) {
                throw new RuntimeException("Error in onDependenciesChange script for parameter whit key \"" +
                        getKey() + "\"", e);
            }
        }
    }

    @Override
    public List<String> validate(T value) {
        if (validateScript == null) {
            return Collections.emptyList();
        }
        shell.setProperty("value", value);
        try {
            return (List<String>) shell.evaluate(IMPORTS + validateScript);
        } catch (Throwable e) {
            throw new RuntimeException("Error in validate script for parameter whit key \"" +
                    getKey() + "\"", e);
        }
    }
}
