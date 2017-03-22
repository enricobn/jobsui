package org.jobsui.core.groovy;

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
import java.net.URL;
import java.util.Arrays;

/**
 * Created by enrico on 5/4/16.
 */
public class JobParser {
    public static final String PROJECT_FILE_NAME = "project.xml";
    private final Validator jobValidator;
    private final Validator projectValidator;
    private final File folder;

    public static JobParser getParser(String projectRoot) throws SAXException {
        File folder = new File(projectRoot);
        return new JobParser(folder);
    }

    private JobParser(File folder) throws SAXException {
        this.folder = folder;
        String language = XMLConstants.W3C_XML_SCHEMA_NS_URI;
        SchemaFactory factory = SchemaFactory.newInstance(language);
        Schema jobSchema = factory.newSchema(getClass().getResource("/org/jobsui/job.xsd"));
        jobValidator = jobSchema.newValidator();
        Schema projectSchema = factory.newSchema(getClass().getResource("/org/jobsui/project.xsd"));
        projectValidator = projectSchema.newValidator();
    }

    public ProjectXML parse() throws Exception {
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

        return projectXML;
    }

    private ProjectXML parseProject(File projectFolder, InputStream is) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setValidating(false);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

        Document doc = dBuilder.parse(is);

        NodeList projects = doc.getElementsByTagName("Project");
        String projectName = getMandatoryAttribute((Element) projects.item(0), "name");

        ProjectXML projectXML = new ProjectXML(projectFolder, projectName);

        NodeList libraries = doc.getElementsByTagName("Library");

        for (int i = 0; i < libraries.getLength(); i++) {
            Element element = (Element) libraries.item(i);
            String library = getElementContent(element, "#text", false);
            projectXML.addLibrary(library);
        }

        NodeList imports = doc.getElementsByTagName("Import");

        for (int i = 0; i < imports.getLength(); i++) {
            Element element = (Element) imports.item(i);
            String imp = getElementContent(element, "#text", false);
            String name = getMandatoryAttribute(element, "name");
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

            String id = getMandatoryAttribute(doc.getDocumentElement(), "id");
            String name = getMandatoryAttribute(doc.getDocumentElement(), "name");
            String version = getMandatoryAttribute(doc.getDocumentElement(), "version");

            JobXML jobXML = new JobXML(file, id, name, version);

            String runScript = getElementContent(doc.getDocumentElement(), "Run", true);

            jobXML.setRunScript(runScript);

            String validateScript = getElementContent(doc.getDocumentElement(), "Validate", false);

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
            String parameterKey = getMandatoryAttribute(element, "key");
            String parameterName = getMandatoryAttribute(element, "name");
            String evaluateScript = getElementContent(element, "Evaluate", false);

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

            String key = getMandatoryAttribute(element, "key");
            String name = getMandatoryAttribute(element, "name");
            String project = getMandatoryAttribute(element, "project");
            String job = getMandatoryAttribute(element, "job");

            CallXML callXML = new CallXML(key, name);
            callXML.setProject(project);
            callXML.setJob(job);

            NodeList maps = element.getElementsByTagName("Map");
            for (int j = 0; j < maps.getLength(); j++) {
                Element mapElement = (Element) maps.item(j);
                String in = getMandatoryAttribute(mapElement, "in");
                String out = getMandatoryAttribute(mapElement, "out");
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

            String parameterKey = getMandatoryAttribute(element, "key");
            String parameterName = getMandatoryAttribute(element, "name");
            String parameterValidateScript = getElementContent(element, "Validate", false);
            String createComponentScript = getElementContent(element, "CreateComponent", true);
            String onDependenciesChangeScript = getElementContent(element, "OnDependenciesChange", false);
            String visibleString = element.getAttribute("visible");
            String optionalString = element.getAttribute("optional");

            boolean visible = visibleString == null || visibleString.isEmpty() || Boolean.parseBoolean(visibleString);
            boolean optional = optionalString != null && !optionalString.isEmpty() && Boolean.parseBoolean(optionalString);

            SimpleParameterXML simpleParameterXML = new SimpleParameterXML(parameterKey, parameterName);
            simpleParameterXML.setValidateScript(parameterValidateScript);
            simpleParameterXML.setCreateComponentScript(createComponentScript);
            simpleParameterXML.setOnDependenciesChangeScript(onDependenciesChangeScript);
            simpleParameterXML.setVisible(visible);
            simpleParameterXML.setOptional(optional);

            addDependencies(element, simpleParameterXML);

            jobXML.add(simpleParameterXML);
        }
    }

    private static void addDependencies(Element element, ParameterXML parameterXML) throws JobsUIParseException {
        final NodeList dependenciesList = element.getElementsByTagName("Dependency");
        for (int iDep = 0; iDep < dependenciesList.getLength(); iDep++) {
            final Element dependency = (Element) dependenciesList.item(iDep);
            final String depKey = getMandatoryAttribute(dependency, "key");
            parameterXML.addDependency(depKey);
        }
    }

    private static String getElementContent(Element parent, String name, boolean mandatory) throws JobsUIParseException {
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
                throw new JobsUIParseException("Cannot find mandatory element \"" + name + "\" in " + parent);
//            }
        } else {
            return null;
        }
    }

    private static String getMandatoryAttribute(Element parent, String name) throws JobsUIParseException {
        final String attribute = parent.getAttribute(name);

        if (attribute == null || attribute.length() == 0) {
//            if (parent instanceof DeferredNode) {
//                throw new JobsUIParseException("Cannot find mandatory attribute \"" + name + "\" in " + parent + " at line " +
//                        ((DeferredNode)parent).getNodeIndex());
//            } else {
                throw new JobsUIParseException("Cannot find mandatory attribute \"" + name + "\" in " + parent);
//            }
        }
        return attribute;
    }
}
