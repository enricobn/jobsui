package org.jobsui.core.xml;

import org.apache.commons.io.FileUtils;
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

    public void export(ProjectFSXML projectXML, File folder) throws Exception {
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

        for (String location : projectXML.getScriptsLocations()) {
            File locationRoot = new File(folder, location);
            for (File file : projectXML.getScriptFiles(location)) {
                File dest = new File(locationRoot, file.getName());
                FileUtils.copyFile(file, dest);
            }
        }

        JobXMLExporter jobXMLExporter = new JobXMLExporter();

        for (JobXML job : projectXML.getJobXMLs()) {
            jobXMLExporter.export(job, new File(folder, job.getId() + ".xml"));
        }

    }

}
