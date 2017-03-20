package org.jobsui.core.groovy;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.codehaus.groovy.control.CompilationFailedException;
import org.jobsui.core.job.JobParameterDefAbstract;
import org.jobsui.core.ui.*;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

/**
 * Created by enrico on 5/4/16.
 */
@Deprecated
public class JobExpressionDefGroovy extends JobParameterDefAbstract implements JobParameterDefGroovy {
    private static final String IMPORTS =
            "import org.jobsui.core.*;\n" +
            "import org.jobsui.core.ui.*;\n";
//    private final File projectFolder;
    private final String evaluateScript;
    private final Script evaluate;
    private final Binding shellBinding;

    public JobExpressionDefGroovy(GroovyShell shell, String key, String name,
                                  String evaluateScript) {
        super(key, name, null, false, false);
//        this.projectFolder = projectFolder;
        this.evaluateScript = evaluateScript;
        try {
            this.evaluate = shell.parse(IMPORTS + evaluateScript);
        } catch (CompilationFailedException e) {
            throw new RuntimeException("Error parsing evaluate for expression with key \"" + key + "\".", e);
        }
        shellBinding = shell.getContext();
    }

    @Override
    public <C> UIComponent<C> createComponent(UI<C> ui) throws UnsupportedComponentException {
        final UIExpression component = ui.create(UIExpression.class);
        if (getDependencies().isEmpty()) {
            evaluate(component, Collections.emptyMap());
        }
        return component;
    }

    @Override
    public void onDependenciesChange(UIWidget widget, Map<String, Serializable> values) {
        evaluate(((UIExpression) widget.getComponent()), values);
    }

    private void evaluate(UIExpression component, Map<String, Serializable> values) {
        // I reset the bindings otherwise I get "global" or previous bindings
        evaluate.setBinding(new Binding(shellBinding.getVariables()));
        evaluate.setProperty("values", values);
        for (Map.Entry<String, Serializable> entry : values.entrySet()) {
            evaluate.setProperty(entry.getKey(), entry.getValue());
        }

//        evaluate.setProperty("projectFolder", projectFolder);
        try {
            Serializable value = (Serializable) evaluate.run();
            component.setValue(value);
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

    @Override
    public boolean isCalculated() {
        return true;
    }
}
