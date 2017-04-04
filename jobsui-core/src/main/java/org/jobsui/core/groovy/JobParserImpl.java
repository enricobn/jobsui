package org.jobsui.core.groovy;

import org.jobsui.core.ui.UIComponentType;
import org.jobsui.core.xml.*;
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
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.logging.Logger;

/**
 * Created by enrico on 5/4/16.
 */
public class JobParserImpl implements JobParser {
    private static final Logger LOGGER = Logger.getLogger(JobParserImpl.class.getName());
    public static final String PROJECT_FILE_NAME = "project.xml";
    private static final Validator jobValidator;
    private static final Validator projectValidator;
    private final File folder;

    static {
        String language = XMLConstants.W3C_XML_SCHEMA_NS_URI;
        SchemaFactory factory = SchemaFactory.newInstance(language);
        Schema jobSchema;
        Schema projectSchema;
        try {
            jobSchema = factory.newSchema(JobParserImpl.class.getResource("/org/jobsui/job.xsd"));
            projectSchema = factory.newSchema(JobParserImpl.class.getResource("/org/jobsui/project.xsd"));
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
        jobValidator = jobSchema.newValidator();
        projectValidator = projectSchema.newValidator();
    }

    JobParserImpl(File folder) throws SAXException {
        this.folder = folder;
    }

    @Override
    public ProjectXML parse() throws Exception {
        LOGGER.info("Parsing " + folder);
        File projectFile = new File(folder, PROJECT_FILE_NAME);

        if (!projectFile.exists()) {
            throw new Exception("Cannot find project file (" + PROJECT_FILE_NAME + ") in " + folder);
        }

        try (FileInputStream is = new FileInputStream(projectFile)) {
            final StreamSource source = new StreamSource(is);
            try {
                projectValidator.validate(source);
            } catch (Exception e) {
                throw new Exception("Cannot parse file " + projectFile, e);
            }
        }

        ProjectXML projectXML;
        try (FileInputStream is = new FileInputStream(projectFile)) {
            projectXML = parseProject(folder, is);
        }

        final File[] files = folder.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.getName().endsWith(".job")) {

                    try (InputStream is = new FileInputStream(file)) {
                        final StreamSource source = new StreamSource(is);
                        try {
                            jobValidator.validate(source);
                        } catch (Exception e) {
                            throw new Exception("Cannot parse file " + file, e);
                        }
                    }

                    parseJob(file, projectXML);
                }
            }
        }
        LOGGER.info("Parsed " + folder);
        return projectXML;
    }

    private ProjectXML parseProject(File projectFolder, InputStream is) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setValidating(false);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

        Document doc = dBuilder.parse(is);

        NodeList projects = doc.getElementsByTagName("Project");
        String subject = "Project";
        String projectId = getMandatoryAttribute((Element) projects.item(0), "id", subject);
        subject = "Project with id='" + projectId + "'";
        String projectName = getMandatoryAttribute((Element) projects.item(0), "name", subject);

        ProjectXML projectXML = new ProjectXML(projectFolder, projectId, projectName);

        NodeList libraries = doc.getElementsByTagName("Library");

        for (int i = 0; i < libraries.getLength(); i++) {
            Element element = (Element) libraries.item(i);
            subject = "Library for Project with id='" + projectId + "'";
            String library = getElementContent(element, "#text", false, subject);
            projectXML.addLibrary(library);
        }

        NodeList imports = doc.getElementsByTagName("Import");

        for (int i = 0; i < imports.getLength(); i++) {
            Element element = (Element) imports.item(i);
            subject = "Import for Project with id='" + projectId + "'";
            String imp = getElementContent(element, "#text", false, subject);
            String name = getMandatoryAttribute(element, "name", subject);
            projectXML.addImport(name, imp);
        }

        final File lib = new File(projectFolder, "lib");
        if (lib.exists()) {
            final File[] libFiles = lib.listFiles();
            if (libFiles != null) {
                for (File file : libFiles) {
                    projectXML.addFileLibrary(file);
                }
            }
        }

        final File groovy = new File(projectFolder, "groovy");
        if (groovy.exists()) {
            File[] files = groovy.listFiles(File::isFile);
            if (files != null) {
                Arrays.stream(files).forEach(projectXML::addGroovyFile);
            }
        }

        return projectXML;
    }

    private void parseJob(File file, ProjectXML projectXML) throws Exception {
        try (InputStream is = new FileInputStream(file)) {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setValidating(false);
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            Document doc = dBuilder.parse(is);

            //optional, but recommended
            //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();
            String subject = "Job";
            String id = getMandatoryAttribute(doc.getDocumentElement(), "id", subject);
            subject = "Job with id='" + id + "'";
            String name = getMandatoryAttribute(doc.getDocumentElement(), "name", subject);
            String version = getMandatoryAttribute(doc.getDocumentElement(), "version", subject);

            JobXML jobXML = new JobXML(file, id, name, version);

            String runScript = getElementContent(doc.getDocumentElement(), "Run", true, subject);

            jobXML.setRunScript(runScript);

            String validateScript = getElementContent(doc.getDocumentElement(), "Validate", false, subject);

            jobXML.setValidateScript(validateScript);

            parseParameters(doc, jobXML);

            parseExpressions(doc, jobXML);

            parseCalls(doc, jobXML);

            projectXML.addJob(jobXML);

        } catch (Throwable th) {
            throw new Exception("Cannot parse file " + file, th);
        }
    }

    private void parseExpressions(Document doc, JobXML jobXML)
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

    private void parseCalls(Document doc, JobXML jobXML)
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

    private void parseParameters(Document doc, JobXML jobXML)
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

    private static String getElementContent(Element parent, String name, boolean mandatory, String subject) throws JobsUIParseException {
        final NodeList childNodes = parent.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            final String nodeName = childNodes.item(i).getNodeName();
            if (nodeName.equals(name)) {
                return childNodes.item(i).getTextContent();
            }
        }
        if (mandatory) {
//            if (parent.getUserData("lineNumber") != null) {
//                throw new JobsUIParseException("Cannot find mandatory element \"" + name + "\" in " + parent +
//                        parent.getUserData("lineNumber"));
//            } else {
                throw new JobsUIParseException("Cannot find mandatory element \"" + name + "\" in " + subject);
//            }
        } else {
            return null;
        }
    }

    private static String getMandatoryAttribute(Element element, String name, String subject) throws JobsUIParseException {
        final String attribute = element.getAttribute(name);

        if (attribute == null || attribute.length() == 0) {
//            if (parent instanceof DeferredNode) {
//                throw new JobsUIParseException("Cannot find mandatory attribute \"" + name + "\" in " + parent + " at line " +
//                        ((DeferredNode)parent).getNodeIndex());
//            } else {
                throw new JobsUIParseException("Cannot find mandatory attribute \"" + name + "\" in " + subject);
//            }
        }
        return attribute;
    }
}
