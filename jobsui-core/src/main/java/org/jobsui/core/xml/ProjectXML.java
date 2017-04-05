package org.jobsui.core.xml;

import org.jobsui.core.groovy.JobParser;
import org.jobsui.core.utils.JobsUIUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.net.URL;
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
    private final Collection<File> groovyFiles = new ArrayList<>();
    private final String id;
    private String name;

    public ProjectXML(File projectFolder, String id, String name) {
        this.projectFolder = projectFolder;
        this.id = id;
        this.name = name;
    }

    public JobParser getParser(String relativePath) throws Exception {
        File path = new File(projectFolder, relativePath);
        return JobParser.getParser(path.getAbsolutePath());
    }

    public URL getRelativeURL(String relativePath) {
        final File path = new File(projectFolder, relativePath);
        try {
            return path.toURI().toURL();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
            // TODO rename to id
            XMLUtils.addAttr(element, "name", entry.getKey());
        }

//        XMLUtils.write(doc, new File(projectFolder, JobParser.PROJECT_FILE_NAME),
//                getClass().getResource("/org/jobsui/project.xsd"));

        for (JobXML jobXML : jobs.values()) {
            jobXML.export(new File(projectFolder, jobXML.getId() + ".xml"));
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

    public void addGroovyFile(File file) {
        groovyFiles.add(file);
    }

    public Collection<File> getGroovyFiles() {
        return groovyFiles;
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

    public URL[] getScriptsURLS() {
        return new URL[]{getRelativeURL("groovy")};
    }
}
