package org.jobsui.core.xml;

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
import java.util.function.BiFunction;
import java.util.logging.Logger;

import static org.jobsui.core.xml.XMLUtils.getElementContent;
import static org.jobsui.core.xml.XMLUtils.getMandatoryAttribute;

/**
 * Created by enrico on 5/4/16.
 */
public class ProjectParserImpl implements ProjectParser {
    public static final String PROJECT_FILE_NAME = "project.xml";
    private static final Logger LOGGER = Logger.getLogger(ProjectParserImpl.class.getName());
    private static final Validator projectValidator;

    static {
        String language = XMLConstants.W3C_XML_SCHEMA_NS_URI;
        SchemaFactory factory = SchemaFactory.newInstance(language);
        Schema projectSchema;
        try {
            projectSchema = factory.newSchema(ProjectParserImpl.class.getResource("/org/jobsui/project.xsd"));
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
        projectValidator = projectSchema.newValidator();
    }

    @Override
    public ProjectFSXML parse(File folder) throws Exception {
        File projectFile = new File(folder, PROJECT_FILE_NAME);
        if (!projectFile.exists()) {
            throw new Exception("Cannot find project file (" + PROJECT_FILE_NAME + ") in " + folder);
        }

        ProjectFSXMLImpl projectFSXML = parse(folder.toURI().toURL(), (id, name) -> {
            try {
                return new ProjectFSXMLImpl(folder, id, name);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        projectFSXML.afterLoad();

        return projectFSXML;
    }

    @Override
    public ProjectXML parse(URL url) throws Exception {
        return parse(url, (id,name) -> new ProjectXMLImpl(url, id, name));
    }

    private static  <T extends ProjectXMLImpl> T parse(URL url, BiFunction<String,String,T> supplier) throws Exception {
        LOGGER.info("Parsing " + url);
        URL projectURL = new URL(url + "/" + PROJECT_FILE_NAME);

        try (InputStream is = projectURL.openStream()) {
            final StreamSource source = new StreamSource(is);
            try {
                projectValidator.validate(source);
            } catch (Exception e) {
                throw new Exception("Cannot parse file " + projectURL, e);
            }
        }

        T projectXML;
        try (InputStream is = projectURL.openStream()) {

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setValidating(false);
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            Document doc = dBuilder.parse(is);

            NodeList projects = doc.getElementsByTagName("Project");
            String subject = "Project";
            String projectId = getMandatoryAttribute((Element) projects.item(0), "id", subject);
            subject = "Project with id='" + projectId + "'";
            String projectName = getMandatoryAttribute((Element) projects.item(0), "name", subject);

            projectXML = supplier.apply(projectId, projectName);

            String projectVersion = getMandatoryAttribute((Element) projects.item(0), "version", subject);
            projectXML.setVersion(projectVersion);

            parseProject(doc, projectXML);
        }

        LOGGER.info("Parsed " + url);
        return projectXML;

    }

    private static void parseProject(Document doc, ProjectXMLImpl projectXML) throws Exception {
        String subject;
        NodeList libraries = doc.getElementsByTagName("Library");

        for (int i = 0; i < libraries.getLength(); i++) {
            Element element = (Element) libraries.item(i);
            subject = "Library for Project with id='" + projectXML.getId() + "'";
            String library = getElementContent(element, "#text", false, subject);
            projectXML.addLibrary(library);
        }

        NodeList imports = doc.getElementsByTagName("Import");

        for (int i = 0; i < imports.getLength(); i++) {
            Element element = (Element) imports.item(i);
            subject = "Import for Project with id='" + projectXML.getId() + "'";
            String imp = getElementContent(element, "#text", false, subject);
            String name = getMandatoryAttribute(element, "name", subject);
            projectXML.addImport(name, imp);
        }

        NodeList jobs = doc.getElementsByTagName("Job");
        for (int i = 0; i < jobs.getLength(); i++) {
            Element element = (Element) jobs.item(i);
            subject = "Job for Project with id='" + projectXML.getId() + "'";
            String jobFile = getElementContent(element, "#text", false, subject);

            if (jobFile == null || !jobFile.endsWith(".xml")) {
                throw new Exception(jobFile + " is not a valid job file name: it must end with .xml.");
            }

            projectXML.addJob(jobFile);
        }
    }

}
