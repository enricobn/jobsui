package org.jobsui.core.xml;

import org.jobsui.core.groovy.JobParser;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.*;

/**
 * Created by enrico on 10/6/16.
 */
public class ProjectXML {
    private final File projectFolder;
    private final String name;
    private final List<String> libraries = new ArrayList<>();
    private final Map<String, String> imports = new HashMap<>();
    private final Map<String, JobXML> jobs = new HashMap<>();
    private final Collection<File> fileLibraries = new ArrayList<>();
    private final Collection<File> groovyFiles = new ArrayList<>();

    public ProjectXML(File projectFolder, String name) {
        this.projectFolder = projectFolder;
        this.name = name;
    }

    public void export() throws Exception {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        // root elements
        Document doc = docBuilder.newDocument();
        Element rootElement = doc.createElement("Project");
        doc.appendChild(rootElement);

        for (String library : libraries) {
            XMLUtils.addTextElement(rootElement, "Library", library);
        }

        for (Map.Entry<String, String> entry : imports.entrySet()) {
            Element element = XMLUtils.addTextElement(rootElement, "Import", entry.getValue());
            XMLUtils.addAttr(element, "name", entry.getKey());
        }

        XMLUtils.write(doc, new File(projectFolder, JobParser.PROJECT_FILE_NAME));

        for (JobXML jobXML : jobs.values()) {
            jobXML.export();
        }

    }

    public void addLibrary(String library) {
        libraries.add(library);
    }

    public void addImport(String name, String uri) throws Exception {
        imports.put(name, uri);
    }

    public void addJob(JobXML job) {
        jobs.put(job.getKey(), job);
    }

    public List<String> getLibraries() {
        return libraries;
    }

    public Map<String, String> getImports() {
        return imports;
    }

    public String getName() {
        return name;
    }

    public void addFileLibrary(File file) {
        fileLibraries.add(file);
    }

    public Collection<File> getFileLibraries() {
        return fileLibraries;
    }

    public void addGroovyFile(File file) {
        groovyFiles.add(file);
    }

    public Collection<File> getGroovyFiles() {
        return groovyFiles;
    }

    public File getProjectFolder() {
        return projectFolder;
    }

    public Map<String, JobXML> getJobs() {
        return jobs;
    }
}
