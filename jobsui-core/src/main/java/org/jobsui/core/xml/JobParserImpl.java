package org.jobsui.core.xml;

import org.jobsui.core.groovy.JobsUIParseException;
import org.jobsui.core.ui.UIComponentType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.InputStream;
import java.net.URL;

import static org.jobsui.core.xml.XMLUtils.getElementContent;
import static org.jobsui.core.xml.XMLUtils.getMandatoryAttribute;

/**
 * Created by enrico on 4/5/17.
 */
public class JobParserImpl implements JobParser {
    private static final Validator jobValidator;

    static {
        String language = XMLConstants.W3C_XML_SCHEMA_NS_URI;
        SchemaFactory factory = SchemaFactory.newInstance(language);
        Schema jobSchema;
        try {
            jobSchema = factory.newSchema(ProjectParserImpl.class.getResource("/org/jobsui/job.xsd"));
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
        jobValidator = jobSchema.newValidator();
    }

    public static JobXML parse(SimpleProjectXML projectXML, String jobResource) throws Exception {
        JobParserImpl jobParser = new JobParserImpl();
        return jobParser.parse(projectXML.getJobId(jobResource), projectXML.getRelativeURL(jobResource));
    }

    @Override
    public JobXML parse(String id, URL url) throws Exception {
        try (InputStream inputStream = url.openStream()) {
            final StreamSource source = new StreamSource(inputStream);
            try {
                jobValidator.validate(source);
            } catch (Exception e) {
                throw new Exception("Cannot parse job \"" + url + "\".", e);
            }
        }

        try (InputStream inputStream = url.openStream()) {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setValidating(false);
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            Document doc = dBuilder.parse(inputStream);

            //optional, but recommended
            //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();
            String subject = "Job with id='" + id + "'";
            String name = getMandatoryAttribute(doc.getDocumentElement(), "name", subject);
            String version = getMandatoryAttribute(doc.getDocumentElement(), "version", subject);

            JobXMLImpl jobXML = new JobXMLImpl(id, name, version);

            String runScript = getElementContent(doc.getDocumentElement(), "Run", true, subject);

            jobXML.setRunScript(runScript);

            String validateScript = getElementContent(doc.getDocumentElement(), "Validate", false, subject);

            jobXML.setValidateScript(validateScript);

            parseParameters(doc, jobXML);

            parseExpressions(doc, jobXML);

            parseCalls(doc, jobXML);

            parseWizardSteps(doc, jobXML);

            return jobXML;
        }
    }

    private void parseWizardSteps(Document doc, JobXMLImpl jobXML) throws JobsUIParseException {
        NodeList wizardStepList = doc.getElementsByTagName("WizardStep");
        for (int i = 0; i < wizardStepList.getLength(); i++) {
            Element element = (Element) wizardStepList.item(i);
            WizardStepImpl wizardStep = new WizardStepImpl();
            String name = getMandatoryAttribute(element, "name", "Wizard step");
            wizardStep.setName(name);
            String subject = "Wizard step '" + name + "'";
            String dependsOn = getMandatoryAttribute(element, "dependsOn", subject);
            for (String dependency : dependsOn.split(",")) {
                wizardStep.addDependency(dependency);
            }

            String validateScript = getElementContent(element, "Validate", false, subject);
            if (validateScript != null) {
                wizardStep.setValidateScript(validateScript);
            }
            jobXML.add(wizardStep);
        }
    }

    private static void parseExpressions(Document doc, JobXMLImpl jobXML)
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

    private static void parseCalls(Document doc, JobXMLImpl jobXML)
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

    private static void parseParameters(Document doc, JobXMLImpl jobXML)
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

    private static void addDependencies(Element element, ParameterXML parameterXML) {
        String dependsOn = element.getAttribute("dependsOn");
        if (dependsOn == null || dependsOn.length() == 0) {
            return;
        }

        for (String dependency : dependsOn.split(",")) {
            parameterXML.addDependency(dependency);
        }
    }

}
