package org.jobsui.core.groovy;

import org.jobsui.core.ui.UIComponentType;
import org.jobsui.core.xml.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;

import static org.jobsui.core.groovy.XMLUtils.getElementContent;
import static org.jobsui.core.groovy.XMLUtils.getMandatoryAttribute;

/**
 * Created by enrico on 4/5/17.
 */
public class JobParserImpl implements JobParser {

    public static JobXML parse(ProjectXML projectXML, String jobResource) throws Exception {
        JobParserImpl jobParser = new JobParserImpl();
        try (InputStream inputStream = projectXML.getRelativeURL(jobResource).openStream()) {
            return jobParser.parse(projectXML.getJobId(jobResource), inputStream);
        }
    }

    @Override
    public JobXML parse(String id, InputStream inputStream) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setValidating(false);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

        Document doc = dBuilder.parse(inputStream);

        //optional, but recommended
        //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
        doc.getDocumentElement().normalize();
        String subject =  "Job with id='" + id + "'";
        String name = getMandatoryAttribute(doc.getDocumentElement(), "name", subject);
        String version = getMandatoryAttribute(doc.getDocumentElement(), "version", subject);

        JobXML jobXML = new JobXML(id, name, version);

        String runScript = getElementContent(doc.getDocumentElement(), "Run", true, subject);

        jobXML.setRunScript(runScript);

        String validateScript = getElementContent(doc.getDocumentElement(), "Validate", false, subject);

        jobXML.setValidateScript(validateScript);

        parseParameters(doc, jobXML);

        parseExpressions(doc, jobXML);

        parseCalls(doc, jobXML);

        return jobXML;

//        projectXML.addJob(jobXML);
    }

    private static void parseExpressions(Document doc, JobXML jobXML)
            throws Exception {
        NodeList expressionsList = doc.getElementsByTagName("Expression");
        for (int i = 0; i < expressionsList.getLength(); i++) {
            Element element = (Element) expressionsList.item(i);
            String subject = "Expression";
            String parameterKey = getMandatoryAttribute(element, "key", subject);
            subject = "Expression with key='" + parameterKey + "'";
            String parameterName = getMandatoryAttribute(element, "name", subject);
            String evaluateScript = element.getTextContent();
            ExpressionXML expressionXML = new ExpressionXML(parameterKey, parameterName);
            expressionXML.setEvaluateScript(evaluateScript);

            jobXML.add(expressionXML);

            addDependencies(element, expressionXML);
        }
    }

    private static void parseCalls(Document doc, JobXML jobXML)
            throws Exception {
        NodeList callsList = doc.getElementsByTagName("Call");
        for (int i = 0; i < callsList.getLength(); i++) {
            Element element = (Element) callsList.item(i);

            String subject = "Call";
            String key = getMandatoryAttribute(element, "key", subject);
            subject = "Call with key='" + key + "'";
            String name = getMandatoryAttribute(element, "name", subject);
            String project = getMandatoryAttribute(element, "project", subject);
            String job = getMandatoryAttribute(element, "job", subject);

            CallXML callXML = new CallXML(key, name);
            callXML.setProject(project);
            callXML.setJob(job);

            NodeList maps = element.getElementsByTagName("Map");
            for (int j = 0; j < maps.getLength(); j++) {
                Element mapElement = (Element) maps.item(j);
                subject = "Map for Call with key='" + key + "'";
                String in = getMandatoryAttribute(mapElement, "in", subject);
                String out = getMandatoryAttribute(mapElement, "out", subject);
                callXML.addMap(in, out);
                callXML.addDependency(in);
            }

            jobXML.add(callXML);
        }
    }

    private static void parseParameters(Document doc, JobXML jobXML)
            throws Exception {
        NodeList parametersList = doc.getElementsByTagName("Parameter");

        for (int i = 0; i < parametersList.getLength(); i++) {
            Element element = (Element) parametersList.item(i);
            String subject = "Parameter";
            String parameterKey = getMandatoryAttribute(element, "key", subject);
            subject = "Parameter with key='" + parameterKey + "'";
            String parameterName = getMandatoryAttribute(element, "name", subject);
//            String component = getMandatoryAttribute(element, "component", subject);
            String component = element.getAttribute("component");
            if (component == null || component.isEmpty()) {
                component = "Value";
            }
            String parameterValidateScript = getElementContent(element, "Validate", false, subject);
            String onInitScript = getElementContent(element, "OnInit", false, subject);
            String onDependenciesChangeScript = getElementContent(element, "OnDependenciesChange", false, subject);
            String visibleString = element.getAttribute("visible");
            String optionalString = element.getAttribute("optional");

            boolean visible = visibleString == null || visibleString.isEmpty() || Boolean.parseBoolean(visibleString);
            boolean optional = optionalString != null && !optionalString.isEmpty() && Boolean.parseBoolean(optionalString);

            SimpleParameterXML simpleParameterXML = new SimpleParameterXML(parameterKey, parameterName);
            simpleParameterXML.setValidateScript(parameterValidateScript);
            simpleParameterXML.setOnInitScript(onInitScript);
            simpleParameterXML.setOnDependenciesChangeScript(onDependenciesChangeScript);
            simpleParameterXML.setVisible(visible);
            simpleParameterXML.setOptional(optional);
            simpleParameterXML.setComponent(UIComponentType.valueOf(component));

            addDependencies(element, simpleParameterXML);

            jobXML.add(simpleParameterXML);
        }
    }

    private static void addDependencies(Element element, ParameterXML parameterXML) throws JobsUIParseException {
        String dependesOn = element.getAttribute("dependsOn");
        if (dependesOn == null || dependesOn.length() == 0) {
            return;
        }

        for (String dependency : dependesOn.split(",")) {
            parameterXML.addDependency(dependency);
        }
    }

}
