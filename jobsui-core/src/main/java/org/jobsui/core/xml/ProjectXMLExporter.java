package org.jobsui.core.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by enrico on 4/5/17.
 */
public class ProjectXMLExporter {

    public void export(ProjectFSXML projectXML, File folder, List<JobXML> jobs) throws Exception {
        List<String> validate = projectXML.validate();

        if (!validate.isEmpty()) {
            throw new Exception("Invalid project \"" + projectXML.getName() + "\":\n" + validate.stream().collect(Collectors.joining("\n ")));
        }

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        // root elements
        Document doc = docBuilder.newDocument();
        Element rootElement = doc.createElement("Project");
        XMLUtils.addAttr(rootElement, "id", projectXML.getId());
        XMLUtils.addAttr(rootElement, "name", projectXML.getName());
        XMLUtils.addAttr(rootElement, "version", projectXML.getVersion());
        doc.appendChild(rootElement);

        for (String library : projectXML.getLibraries()) {
            XMLUtils.addTextElement(rootElement, "Library", library);
        }

        for (Map.Entry<String, String> entry : projectXML.getImports().entrySet()) {
            Element element = XMLUtils.addTextElement(rootElement, "Import", entry.getValue());
            // TODO rename to id
            XMLUtils.addAttr(element, "name", entry.getKey());
        }

        for (String job : projectXML.getJobs()) {
            XMLUtils.addTextElement(rootElement, "Job", job);
        }

        XMLUtils.write(doc, new File(folder, ProjectParserImpl.PROJECT_FILE_NAME),
                getClass().getResource("/org/jobsui/project.xsd"));

        JobXMLExporter jobXMLExporter = new JobXMLExporter();

        for (JobXML job : jobs) {
            jobXMLExporter.export(job, new File(folder, job.getId() + ".xml"));
        }

    }

}
