package org.jobsui.core.xml;

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

    ParameterXML getParameter(String key);

//    SimpleParameterXML getSimpleParameter(String key);

    ExpressionXML getExpression(String key);

    String getVersion();
}
