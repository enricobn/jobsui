package org.jobsui.core.groovy;

import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.codehaus.groovy.control.CompilationFailedException;
import org.jobsui.core.JobParameterDefAbstract;
import org.jobsui.core.ui.*;

import java.io.File;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

/**
 * Created by enrico on 5/4/16.
 */
public class JobExpressionDefGroovy<T extends Serializable> extends JobParameterDefAbstract<T> implements JobParameterDefGroovy<T> {
    private static final String IMPORTS =
            "import org.jobsui.core.*;\n" +
            "import org.jobsui.core.ui.*;\n";
    private final File projectFolder;
    private final String evaluateScript;
    private final Script evaluate;

    public JobExpressionDefGroovy(File projectFolder, GroovyShell shell, String key, String name,
                                  String evaluateScript) {
        super(key, name, null, false, false);
        this.projectFolder = projectFolder;
        this.evaluateScript = evaluateScript;
        try {
            this.evaluate = shell.parse(IMPORTS + evaluateScript);
        } catch (CompilationFailedException e) {
            throw new RuntimeException("Error parsing evaluate for expression with key \"" + key + "\".", e);
        }
    }

    @Override
    public <C> UIComponent<T, C> createComponent(UI<C> ui) throws UnsupportedComponentException {
        final UIChoice component = ui.create(UIChoice.class);
        if (getDependencies().isEmpty()) {
            evaluate(component, Collections.emptyMap());
        }
        return component;
    }

    @Override
    public void onDependenciesChange(UIWidget widget, Map<String, Serializable> values) {
        evaluate(((UIChoice)widget.getComponent()), values);
    }

    private void evaluate(UIChoice component, Map<String, Serializable> values) {
        evaluate.setProperty("values", values);
        for (Map.Entry<String, Serializable> entry : values.entrySet()) {
            evaluate.setProperty(entry.getKey(), entry.getValue());
        }

        evaluate.setProperty("projectFolder", projectFolder);
        try {
            Object value = evaluate.run();
            if (value == null) {
                component.setItems(Collections.emptyList());
            } else {
                component.setItems(Collections.singletonList(value));
            }
        } catch (Throwable e) {
            throw new RuntimeException("Error in evaluate script for expression whit key \"" +
                    getKey() + "\"", e);
        }

    }

    @Override
    public void init(ProjectGroovy projectGroovy) {

    }

    public String getEvaluateScript() {
        return evaluateScript;
    }
}
