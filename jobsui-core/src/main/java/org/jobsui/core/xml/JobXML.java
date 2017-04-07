package org.jobsui.core.xml;

import java.util.List;

/**
 * Created by enrico on 4/7/17.
 */
public interface JobXML {
    String getRunScript();

    String getValidateScript();

    String getId();

    String getName();

    List<SimpleParameterXML> getSimpleParameterXMLs();

    List<ExpressionXML> getExpressionXMLs();

    List<CallXML> getCallXMLs();

    ParameterXML getParameter(String key);

    ExpressionXML getExpression(String key);
}
