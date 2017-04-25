package org.jobsui.core.job;

/**
 * Created by enrico on 5/13/16.
 */
public abstract class JobAbstract<T> implements Job<T> {

    @Override
    public JobParameter getParameter(String key) {
        for (JobParameter parameterDef : getParameterDefs()) {
            if (parameterDef.getKey().equals(key)) {
                return parameterDef;
            }
        }
        return null;
    }

    @Override
    public JobExpression getExpression(String key) {
        for (JobExpression jobExpression : getExpressions()) {
            if (jobExpression.getKey().equals(key)) {
                return jobExpression;
            }
        }
        return null;
    }

}
