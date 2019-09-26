package org.jobsui.core.xml;

import org.jobsui.core.groovy.JobsUIParseException;
import org.jobsui.core.ui.UIComponentRegistry;
import org.jobsui.core.ui.UIComponentRegistryImpl;
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
    private static final URL jobXsd = ProjectParserImpl.class.getResource("/org/jobsui/job.xsd");
    private static final URL jobXsd_100 = ProjectParserImpl.class.getResource("/org/jobsui/1.0.0/job.xsd");
    private static final URL jobXsd_000 = ProjectParserImpl.class.getResource("/org/jobsui/0.0.0/job.xsd");
    private static final Schema jobSchema_current;
    private static final Schema jobSchema_000;
    private static final Schema jobSchema_100;

    static {
        String language = XMLConstants.W3C_XML_SCHEMA_NS_URI;
        SchemaFactory factory = SchemaFactory.newInstance(language);
        try {
            jobSchema_current = factory.newSchema(jobXsd);
            jobSchema_100 = factory.newSchema(jobXsd_100);
            jobSchema_000 = factory.newSchema(jobXsd_000);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
    }

    public static JobXML parse(ProjectXML projectXML, String id) throws Exception {
        JobParserImpl jobParser = new JobParserImpl();
        return jobParser.parse(id,
                projectXML.getRelativeURL(JobXMLImpl.getFileName(id)),
                projectXML.getUiComponentRegistry());
    }

    @Override
    public JobXML parse(String id, URL url, UIComponentRegistry uiComponentRegistry) throws Exception {
        String jobsUIVersion = XMLJobsUIVersionParser.getInJob(url);

        Schema jobSchema = getSchema(jobsUIVersion);

        try (InputStream inputStream = url.openStream()) {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setValidating(false);

            // to get default values in the xsd
            dbFactory.setSchema(jobSchema);
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            Document doc = dBuilder.parse(inputStream);

            //optional, but recommended
            //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();
            String subject = "Job with id='" + id + "'";
            String name = getMandatoryAttribute(doc.getDocumentElement(), "name", subject);
            String version = getMandatoryAttribute(doc.getDocumentElement(), "version", subject);

            validate(url, jobSchema);

            return parse(id, uiComponentRegistry, doc, subject, name, jobsUIVersion, version);
        }
    }

    private JobXML parse(String id, UIComponentRegistry uiComponentRegistry, Document doc, String subject, String name,
                         String jobsUIVersion, String version) throws Exception {
        JobXMLImpl jobXML = new JobXMLImpl(id, name, version);

        String runScript = getElementContent(doc.getDocumentElement(), "Run", true, subject,
                useCData(jobsUIVersion));

        jobXML.setRunScript(runScript);

        String validateScript = getElementContent(doc.getDocumentElement(), "Validate", false, subject,
                useCData(jobsUIVersion));

        jobXML.setValidateScript(validateScript);

        parseParameters(doc, jobXML, uiComponentRegistry, jobsUIVersion);

        parseExpressions(doc, jobXML);

        parseCalls(doc, jobXML);

        parsePages(doc, jobXML, jobsUIVersion);

        return jobXML;
    }

    private void validate(URL url, Schema schema) throws Exception {
        Validator jobValidator = schema.newValidator();

        try (InputStream inputStream = url.openStream()) {
            final StreamSource source = new StreamSource(inputStream);
            try {
                jobValidator.validate(source);
            } catch (Exception e) {
                throw new Exception("Cannot parse job \"" + url + "\".", e);
            }
        }
    }

    private void parsePages(Document doc, JobXMLImpl jobXML, String jobsUIVersion) throws JobsUIParseException {
        String pageElementName = getPageElementName(jobsUIVersion);

        NodeList pagesList = doc.getElementsByTagName(pageElementName);
        for (int i = 0; i < pagesList.getLength(); i++) {
            Element element = (Element) pagesList.item(i);
            JobPageImpl page = new JobPageImpl();
            String name = getMandatoryAttribute(element, "name", "Page");
            page.setName(name);
            String subject = "Page '" + name + "'";
            String dependsOn = getMandatoryAttribute(element, "dependsOn", subject);
            for (String dependency : dependsOn.split(",")) {
                page.addDependency(dependency);
            }

            String validateScript = getElementContent(element, "Validate", false, subject,
                    useCData(jobsUIVersion));
            if (validateScript != null) {
                page.setValidateScript(validateScript);
            }
            jobXML.add(page);
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

    private static void parseParameters(Document doc, JobXMLImpl jobXML, UIComponentRegistry uiComponentRegistry,
                                        String jobsUIVersion)
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
            String parameterValidateScript = getElementContent(element, "Validate", false, subject,
                    useCData(jobsUIVersion));
            String onInitScript = getElementContent(element, "OnInit", false, subject,
                    useCData(jobsUIVersion));
            String onDependenciesChangeScript = getElementContent(element, "OnDependenciesChange", false, subject,
                    useCData(jobsUIVersion));
            String visibleString = element.getAttribute("visible");
            String optionalString = element.getAttribute("optional");

            boolean visible = visibleString == null || visibleString.isEmpty() || Boolean.parseBoolean(visibleString);
            boolean optional = optionalString != null && !optionalString.isEmpty() && Boolean.parseBoolean(optionalString);

            // TODO throw Exception?
            UIComponentType componentType = uiComponentRegistry
                    .getComponentType(component)
                    .orElse(UIComponentRegistryImpl.Value);

            SimpleParameterXML simpleParameterXML = new SimpleParameterXML(parameterKey, parameterName);
            simpleParameterXML.setComponent(componentType);
            simpleParameterXML.setValidateScript(parameterValidateScript);
            simpleParameterXML.setOnInitScript(onInitScript);
            simpleParameterXML.setOnDependenciesChangeScript(onDependenciesChangeScript);
            simpleParameterXML.setVisible(visible);
            simpleParameterXML.setOptional(optional);

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

    private Schema getSchema(String jobsUIVersion) {
        if (jobsUIVersion.compareTo("1.0.0") < 0) {
            return jobSchema_000;
        } else if (jobsUIVersion.compareTo("1.1.0") < 0) {
            return jobSchema_100;
        }
        return jobSchema_current;
    }

    private String getPageElementName(String jobsUIVersion) {
        String pageElementName;
        if (jobsUIVersion.compareTo("1.1.0") < 0) {
            pageElementName = "WizardStep";
        } else {
            pageElementName = "Page";
        }
        return pageElementName;
    }

    private static boolean useCData(String jobsUIVersion) {
        return jobsUIVersion.compareTo("1.0.0") >= 0;
    }

}
