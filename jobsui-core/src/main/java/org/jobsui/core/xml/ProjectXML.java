package org.jobsui.core.xml;

import org.jobsui.core.groovy.JobParser;
import org.jobsui.core.utils.JobsUIUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by enrico on 10/6/16.
 */
public class ProjectXML implements ValidatingXML {
    private final File projectFolder;
    private final List<String> libraries = new ArrayList<>();
    private final Map<String, String> imports = new HashMap<>();
    private final Map<String, JobXML> jobs = new HashMap<>();
    private final Collection<File> fileLibraries = new ArrayList<>();
    private final Collection<File> groovyFiles = new ArrayList<>();
    private String name;
    private String id;

    public ProjectXML(File projectFolder, String name) {
        this.projectFolder = projectFolder;
        this.name = name;
    }

    public void export() throws Exception {
        List<String> validate = validate();

        if (!validate.isEmpty()) {
            throw new Exception("Invalid project \"" + name + "\":\n" + validate.stream().collect(Collectors.joining("\n ")));
        }

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        // root elements
        Document doc = docBuilder.newDocument();
        Element rootElement = doc.createElement("Project");
        XMLUtils.addAttr(rootElement, "name", getName());
        doc.appendChild(rootElement);

        for (String library : libraries) {
            XMLUtils.addTextElement(rootElement, "Library", library);
        }

        for (Map.Entry<String, String> entry : imports.entrySet()) {
            Element element = XMLUtils.addTextElement(rootElement, "Import", entry.getValue());
            XMLUtils.addAttr(element, "name", entry.getKey());
        }

        XMLUtils.write(doc, new File(projectFolder, JobParser.PROJECT_FILE_NAME),
                getClass().getResource("/org/jobsui/project.xsd"));

        for (JobXML jobXML : jobs.values()) {
            jobXML.export();
        }

    }

    public void addLibrary(String library) {
        libraries.add(library);
    }

    public void addImport(String name, String uri) {
        imports.put(name, uri);
    }

    public void addJob(JobXML job) {
        jobs.put(job.getId(), job);
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

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public List<String> validate() {
        List<String> messages = new ArrayList<>(0);
        if (JobsUIUtils.isNullOrEmptyOrSpaces(name)) {
            messages.add("Name is mandatory.");
        }

        for (JobXML jobXML : jobs.values()) {
            List<String> validate = jobXML.validate();
            if (!validate.isEmpty()) {
                messages.add("Invalid job \"" + jobXML.getName() + "\":\n" + String.join(", ", validate));
            }
        }
        return messages;
    }

    public String getId() {
        return id;
    }
}
