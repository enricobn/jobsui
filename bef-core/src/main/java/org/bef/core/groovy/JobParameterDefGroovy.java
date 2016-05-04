package org.bef.core.groovy;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.bef.core.JobParameterDefAbstract;
import org.bef.core.ParameterValidator;
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
    private final String createComponentScript;
    private final String onDependenciesChangeScript;
    private final String validateScript;

    public JobParameterDefGroovy(String key, String name, Class<T> type,
                                 String createComponentScript, String onDependenciesChangeScript, String validateScript) {
        super(key, name, type, null);
        this.createComponentScript = createComponentScript;
        this.onDependenciesChangeScript = onDependenciesChangeScript;
        this.validateScript = validateScript;
    }

    @Override
    public UIComponent createComponent(UI ui) throws UnsupportedComponentException {
        Binding binding = new Binding();
        binding.setProperty("ui", ui);
        GroovyShell shell = new GroovyShell(binding);
        return (UIComponent) shell.evaluate(createComponentScript);
    }

    @Override
    public void onDependenciesChange(UIComponent component, Map<String, Object> values) {
        if (onDependenciesChangeScript != null) {
            Binding binding = new Binding();
            binding.setProperty("component", component);
            binding.setProperty("values", values);
            GroovyShell shell = new GroovyShell(binding);
            shell.evaluate(onDependenciesChangeScript);
        }
    }

    @Override
    public List<String> validate(T value) {
        if (validateScript == null) {
            return Collections.emptyList();
        }
        Binding binding = new Binding();
        binding.setProperty("value", value);
        GroovyShell shell = new GroovyShell(binding);
        return (List<String>) shell.evaluate(validateScript);
    }
}
