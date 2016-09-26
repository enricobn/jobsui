package org.jobsui.core.groovy;

import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.jobsui.core.JobParameterDefAbstract;
import org.codehaus.groovy.control.CompilationFailedException;
import org.jobsui.core.ui.*;

import java.io.File;
import java.util.Collections;
import java.util.Map;

/**
 * Created by enrico on 5/4/16.
 */
public class JobExpressionDefGroovy<T> extends JobParameterDefAbstract<T> {
    private static final String IMPORTS =
            "import org.jobsui.core.*;\n" +
            "import org.jobsui.core.ui.*;\n";
    private final File projectFolder;
    private final Script evaluate;

    public JobExpressionDefGroovy(File projectFolder, GroovyShell shell, String key, String name,
                                  String evaluateScript) {
        super(key, name, null, false, false);
        this.projectFolder = projectFolder;
        try {
            this.evaluate = shell.parse(IMPORTS + evaluateScript);
        } catch (CompilationFailedException e) {
            throw new RuntimeException("Error parsing evaluate for expression with key \"" + key + "\".", e);
        }
    }

    @Override
    public UIComponent createComponent(UI ui) throws UnsupportedComponentException {
        final UIChoice component = (UIChoice) ui.create(UIChoice.class);
        if (getDependencies().isEmpty()) {
            evaluate(component, Collections.<String, Object>emptyMap());
        }
        return component;
    }

    @Override
    public void onDependenciesChange(UIWidget widget, Map<String, Object> values) {
        evaluate(((UIChoice)widget.getComponent()), values);
    }

    private void evaluate(UIChoice component, Map<String, Object> values) {
        evaluate.setProperty("values", values);
        for (Map.Entry<String, Object> entry : values.entrySet()) {
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

}
