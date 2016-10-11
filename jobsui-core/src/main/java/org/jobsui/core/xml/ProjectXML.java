package org.jobsui.core.xml;

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
