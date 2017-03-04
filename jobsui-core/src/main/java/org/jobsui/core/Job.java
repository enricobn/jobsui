package org.jobsui.core;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by enrico on 4/29/16.
 */
public interface Job<T> {

    String getName();

    List<JobParameterDef<? extends Serializable>> getParameterDefs();

    JobFuture<T> run(Map<String,Serializable> values);

    default JobFuture<T> run(JobValues values) {
        Map<String, Serializable> groovyValues = new HashMap<>();
        for (JobParameterDef<? extends Serializable> parameterDef : getParameterDefs()) {
            groovyValues.put(parameterDef.getKey(), values.getValue(parameterDef));
        }
        return run(groovyValues);
//        return () -> {
//            // I reset the bindings otherwise I get "global" or previous bindings
//            run.setBinding(new Binding(shellBinding.getVariables()));
//
//            Map<String, Serializable> groovyValues = new HashMap<>();
//            for (JobParameterDef<? extends Serializable> parameterDef : getParameterDefs()) {
//                run.setProperty(parameterDef.getKey(), values.getValue(parameterDef));
//                groovyValues.put(parameterDef.getKey(), values.getValue(parameterDef));
//            }
//
//            run.setProperty("values", groovyValues);
////            run.setProperty("projectFolder", projectFolder);
//
//            try {
//                @SuppressWarnings("unchecked")
//                T result = (T) this.run.run();
//                return result;
//            } catch (Exception e) {
//                throw new RuntimeException("Cannot execute run for job with key \"" + getKey() + "\".", e);
//            }
//        };
    }

    List<String> validate(Map<String,Object> values);

    JobParameterDef<?> getParameter(String key);
}
