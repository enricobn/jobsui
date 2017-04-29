package org.jobsui.core.xml;

import org.jobsui.core.ui.UIComponentType;
import org.jobsui.core.utils.JobsUIUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by enrico on 4/8/17.
 */
class JobXMLExporter {

    public void export(JobXML jobXML, File file) throws Exception {
        List<String> validate = jobXML.validate();

        if (!validate.isEmpty()) {
            throw new Exception("Invalid job \"" + jobXML.getName() + "\":\n" +
                    validate.stream().collect(Collectors.joining("\n ")));
        }

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        // root elements
        Document doc = docBuilder.newDocument();
        Element rootElement = doc.createElement("Job");
        doc.appendChild(rootElement);

        XMLUtils.addAttr(rootElement, "name", jobXML.getName());
        XMLUtils.addAttr(rootElement, "version", jobXML.getVersion());

        for (SimpleParameterXML parameter : jobXML.getSimpleParameterXMLs()) {
            Element element = createParameterElement(doc, rootElement, parameter, "Parameter");

            if (!parameter.getComponent().equals(UIComponentType.Value)) {
                XMLUtils.addAttr(element, "component", parameter.getComponent().name());
            }

            if (parameter.getOnInitScript() != null && !parameter.getOnInitScript().isEmpty()) {
                XMLUtils.addTextElement(element, "OnInit", parameter.getOnInitScript(), true);
            }

            if (!JobsUIUtils.isNullOrEmptyOrSpaces(parameter.getValidateScript())) {
                XMLUtils.addTextElement(element, "Validate", parameter.getValidateScript(), true);
            }

            if (parameter.getOnDependenciesChangeScript() != null && !parameter.getOnDependenciesChangeScript().isEmpty()) {
                XMLUtils.addTextElement(element, "OnDependenciesChange", parameter.getOnDependenciesChangeScript(), true);
            }
        }

        for (ExpressionXML expressionXML : jobXML.getExpressionXMLs()) {
            Element element = createParameterElement(doc, rootElement, expressionXML, "Expression");

            if (expressionXML.getEvaluateScript() != null && !expressionXML.getEvaluateScript().isEmpty()) {
                XMLUtils.addTextNode(element, expressionXML.getEvaluateScript(), true);
            }
        }

        // TODO Calls

        if (!JobsUIUtils.isNullOrEmptyOrSpaces(jobXML.getValidateScript())) {
            XMLUtils.addTextElement(rootElement, "Validate", jobXML.getValidateScript(), true);
        }

        if (!JobsUIUtils.isNullOrEmptyOrSpaces(jobXML.getRunScript())) {
            XMLUtils.addTextElement(rootElement, "Run", jobXML.getRunScript(), true);
        }

        try {
            XMLUtils.write(doc, file, JobXMLImpl.class.getResource("/org/jobsui/job.xsd"));
        } catch (Exception e) {
            throw new Exception("Error exporting job \"" + jobXML.getName() + "\".", e);
        }
    }

    private Element createParameterElement(Document doc, Element rootElement, ParameterXML parameter, String elementName) {
        Element element = doc.createElement(elementName);
        rootElement.appendChild(element);
        XMLUtils.addAttr(element, "key", parameter.getKey());
        XMLUtils.addAttr(element, "name", parameter.getName());

        if (!parameter.getDependencies().isEmpty()) {
            String dependencies = parameter.getDependencies().stream().collect(Collectors.joining(","));
            XMLUtils.addAttr(element, "dependsOn", dependencies);
        }

        return element;
    }

}
