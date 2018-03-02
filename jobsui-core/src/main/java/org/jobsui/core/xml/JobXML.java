package org.jobsui.core.xml;

import org.jobsui.core.runner.JobsUIValidationResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by enrico on 4/7/17.
 */
public interface JobXML extends ValidatingXML{

    String getRunScript();

    String getValidateScript();

    String getId();

    String getName();

    List<SimpleParameterXML> getSimpleParameterXMLs();

    List<ExpressionXML> getExpressionXMLs();

    List<CallXML> getCallXMLs();

    default Collection<ParameterXML> getAllParameters() {
        Collection<ParameterXML> result = new ArrayList<>(getSimpleParameterXMLs());
        result.addAll(getExpressionXMLs());
        result.addAll(getCallXMLs());
        return result;
    }

    ParameterXML getParameter(String key);

//    SimpleParameterXML getSimpleParameter(String key);

    ExpressionXML getExpression(String key);

    String getVersion();

    List<WizardStep> getWizardSteps();

    default Collection<JobDependencyXML> getJobDependencyXmls() {
        Collection<JobDependencyXML> result = new ArrayList<>(getSimpleParameterXMLs());
        result.addAll(getExpressionXMLs());
        return result;
    }

    JobsUIValidationResult removeParameter(ParameterXML parameterXML);
}
