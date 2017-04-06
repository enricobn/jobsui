package org.jobsui.core.groovy;

import org.jobsui.core.xml.ProjectFSXMLImpl;
import org.jobsui.core.xml.ProjectParser;
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
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Logger;

import static org.jobsui.core.groovy.XMLUtils.getElementContent;
import static org.jobsui.core.groovy.XMLUtils.getMandatoryAttribute;

/**
 * Created by enrico on 5/4/16.
 */
public class ProjectParserImpl implements ProjectParser {
    private static final Logger LOGGER = Logger.getLogger(ProjectParserImpl.class.getName());
    public static final String PROJECT_FILE_NAME = "project.xml";
    private static final Validator jobValidator;
    private static final Validator projectValidator;
//    private final File folder;

    static {
        String language = XMLConstants.W3C_XML_SCHEMA_NS_URI;
        SchemaFactory factory = SchemaFactory.newInstance(language);
        Schema jobSchema;
        Schema projectSchema;
        try {
            jobSchema = factory.newSchema(ProjectParserImpl.class.getResource("/org/jobsui/job.xsd"));
            projectSchema = factory.newSchema(ProjectParserImpl.class.getResource("/org/jobsui/project.xsd"));
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
        jobValidator = jobSchema.newValidator();
        projectValidator = projectSchema.newValidator();
    }
//
//    public ProjectParserImpl(File folder) throws SAXException {
//        this.folder = folder;
//    }

    @Override
    public ProjectFSXMLImpl parse(URL url) throws Exception {
        LOGGER.info("Parsing " + url);
        URL projectURL = new URL(url + "/" + PROJECT_FILE_NAME);

//        if (!projectFile.exists()) {
//            throw new Exception("Cannot find project file (" + PROJECT_FILE_NAME + ") in " + folder);
//        }

        try (InputStream is = projectURL.openStream()) {
            final StreamSource source = new StreamSource(is);
            try {
                projectValidator.validate(source);
            } catch (Exception e) {
                throw new Exception("Cannot parse file " + projectURL, e);
            }
        }

        ProjectFSXMLImpl projectXML;
        try (InputStream is = projectURL.openStream()) {
            // TODO new File
            projectXML = parseProject(new File(url.getPath()), is);
        }

        LOGGER.info("Parsed " + url);
        return projectXML;
    }

    private ProjectFSXMLImpl parseProject(File projectFolder, InputStream is) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setValidating(false);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

        Document doc = dBuilder.parse(is);

        NodeList projects = doc.getElementsByTagName("Project");
        String subject = "Project";
        String projectId = getMandatoryAttribute((Element) projects.item(0), "id", subject);
        subject = "Project with id='" + projectId + "'";
        String projectName = getMandatoryAttribute((Element) projects.item(0), "name", subject);

        ProjectFSXMLImpl projectXML = new ProjectFSXMLImpl(projectFolder, projectId, projectName);

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

        NodeList jobs = doc.getElementsByTagName("Job");
        for (int i = 0; i < jobs.getLength(); i++) {
            Element element = (Element) jobs.item(i);
            subject = "Job for Project with id='" + projectId + "'";
            String jobFile = getElementContent(element, "#text", false, subject);

            if (jobFile == null || !jobFile.endsWith(".xml")) {
                throw new Exception(jobFile + " is not a valid job file name: it must end with .xml.");
            }

            projectXML.addJob(jobFile);

//            try (InputStream jobis = projectXML.getRelativeURL(jobFile).openStream()) {
//                final StreamSource source = new StreamSource(jobis);
//                try {
//                    jobValidator.validate(source);
//                } catch (Exception e) {
//                    throw new Exception("Cannot parse " + jobFile, e);
//                }
//            }
//
//            try (InputStream jobis = projectXML.getRelativeURL(jobFile).openStream()) {
//                int pos = jobFile.lastIndexOf('.');
//                String id = jobFile.substring(0, pos);
//                parseJob(id, jobis);
//            }
        }

//        for (URL url : projectXML.getScriptsURLS()) {
//            File groovy = new File(url.toURI().getPath());
//            if (groovy.exists()) {
//                File[] files = groovy.listFiles(File::isFile);
//                if (files != null) {
//                    Arrays.stream(files).forEach(projectXML::addGroovyFile);
//                }
//            }
//        }
//
//
//        final File groovy = new File(projectFolder, "groovy");
//        if (groovy.exists()) {
//            File[] files = groovy.listFiles(File::isFile);
//            if (files != null) {
//                Arrays.stream(files).forEach(projectXML::addGroovyFile);
//            }
//        }

        return projectXML;
    }

}
