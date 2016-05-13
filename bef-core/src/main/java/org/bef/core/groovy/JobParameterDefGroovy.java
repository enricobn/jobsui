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
    private final GroovyShell shell;
    private final Script createComponent;
    private final Script onDependenciesChange;
    private final Script validate;

    public JobParameterDefGroovy(GroovyShell shell, String key, String name, Class<T> type,
                                 String createComponentScript, String onDependenciesChangeScript,
                                 String validateScript, boolean visible) {
        super(key, name, type, null, visible);
        this.shell = shell;
        this.createComponent = shell.parse(IMPORTS + createComponentScript);
        this.onDependenciesChange = shell.parse(IMPORTS + onDependenciesChangeScript);
        this.validate =validateScript == null ? null : shell.parse(IMPORTS + validateScript);
    }

    @Override
    public UIComponent createComponent(UI ui) throws UnsupportedComponentException {
        shell.setProperty("ui", ui);
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
            shell.setProperty("widget", widget);
            shell.setProperty("values", values);
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
            return Collections.emptyList();
        }
        shell.setProperty("value", value);
        try {
            return (List<String>) validate.run();
        } catch (Throwable e) {
            throw new RuntimeException("Error in validate script for parameter whit key \"" +
                    getKey() + "\"", e);
        }
    }
}
