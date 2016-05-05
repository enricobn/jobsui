package org.bef.core.groovy;

import groovy.lang.GroovyShell;
import org.bef.core.JobParameterDefAbstract;
import org.bef.core.ui.UI;
import org.bef.core.ui.UIComponent;
import org.bef.core.ui.UnsupportedComponentException;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by enrico on 5/4/16.
 */
public class JobParameterDefGroovy<T> extends JobParameterDefAbstract<T> {
    private static final String CREATE_COMPONENT_IMPORTS =
            "import org.bef.core.ui.*;\n";
    private final GroovyShell shell;
    private final String createComponentScript;
    private final String onDependenciesChangeScript;
    private final String validateScript;

    public JobParameterDefGroovy(GroovyShell shell, String key, String name, Class<T> type,
                                 String createComponentScript, String onDependenciesChangeScript, String validateScript) {
        super(key, name, type, null);
        this.shell = shell;
        this.createComponentScript = CREATE_COMPONENT_IMPORTS + createComponentScript;
        this.onDependenciesChangeScript = onDependenciesChangeScript;
        this.validateScript = validateScript;
    }

    @Override
    public UIComponent createComponent(UI ui) throws UnsupportedComponentException {
        shell.setProperty("ui", ui);
        return (UIComponent) shell.evaluate(createComponentScript);
    }

    @Override
    public void onDependenciesChange(UIComponent component, Map<String, Object> values) {
        if (onDependenciesChangeScript != null) {
            shell.setProperty("component", component);
            shell.setProperty("values", values);
            shell.evaluate(onDependenciesChangeScript);
        }
    }

    @Override
    public List<String> validate(T value) {
        if (validateScript == null) {
            return Collections.emptyList();
        }
        shell.setProperty("value", value);
        return (List<String>) shell.evaluate(validateScript);
    }
}
