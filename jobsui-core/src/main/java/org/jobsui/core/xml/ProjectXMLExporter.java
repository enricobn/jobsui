package org.jobsui.core.xml;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

/**
 * Created by enrico on 4/5/17.
 */
public class ProjectXMLExporter {

    public void export(ProjectFSXML projectXML, File folder) throws Exception {
        List<String> validate = projectXML.validate();

        if (!validate.isEmpty()) {
            throw new Exception("Invalid project \"" + projectXML.getName() + "\":\n" + String.join("\n ", validate));
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

        for (ProjectLibraryXML library : projectXML.getLibraries()) {
            XMLUtils.addTextElement(rootElement, "Library", library.toString(), false);
        }

        for (Map.Entry<String, String> entry : projectXML.getImports().entrySet()) {
            Element element = XMLUtils.addTextElement(rootElement, "Import", entry.getValue(), false);
            // TODO rename to id
            XMLUtils.addAttr(element, "name", entry.getKey());
        }

        for (JobXML job : projectXML.getJobs()) {
            XMLUtils.addTextElement(rootElement, "Job", job.getId(), false);
        }

        XMLUtils.write(doc, new File(folder, ProjectParserImpl.PROJECT_FILE_NAME),
                getClass().getResource("/org/jobsui/project.xsd"));

        Charset utf8 = Charset.forName("UTF-8");

        for (String location : projectXML.getScriptsLocations()) {
            File locationRoot = new File(folder, location);
            for (String fileName: projectXML.getScriptFilesNames(location)) {
                File dest = new File(locationRoot, fileName);
                FileUtils.write(dest, projectXML.getScriptContent(location, fileName), utf8);
            }
        }

        JobXMLExporter jobXMLExporter = new JobXMLExporter();

        for (JobXML job : projectXML.getJobs()) {
            jobXMLExporter.export(job, new File(folder, JobXMLImpl.getFileName(job.getId())));
        }

    }

}
