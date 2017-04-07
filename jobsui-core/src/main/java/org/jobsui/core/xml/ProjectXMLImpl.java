package org.jobsui.core.xml;

import org.jobsui.core.utils.JobsUIUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Created by enrico on 10/6/16.
 */
class ProjectXMLImpl implements ProjectXML {
    private final URL projectURL;
    private final Set<String> libraries = new HashSet<>();
    private final Map<String, String> imports = new HashMap<>();
//    private final Map<String, JobXML> jobs = new HashMap<>();
    private final Set<String> jobs = new HashSet<>();
//    private final Collection<File> groovyFiles = new ArrayList<>();
    private final String id;
    private String name;

    ProjectXMLImpl(URL projectURL, String id, String name) {
        this.projectURL = projectURL;
        this.id = id;
        this.name = name;
    }

//    public JobParser getParser(String relativePath) throws Exception {
//        File path = new File(projectURL, relativePath);
//        return JobParser.getParser(path.getAbsolutePath());
//    }

    @Override
    public URL getRelativeURL(String relativePath) {
        String url;
        if (projectURL.toString().endsWith("/")) {
            url = projectURL + relativePath;
        } else {
            url = projectURL + "/" + relativePath;
        }
//        final File path = new File(projectURL, relativePath);
//        try {
//            return path.toURI().toURL();
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public void addLibrary(String library) {
        libraries.add(library);
    }

    public void addImport(String name, String uri) {
        imports.put(name, uri);
    }

    public void addJob(String job) {
        if (!jobs.add(job)) {
            throw new IllegalArgumentException("Job \"" + job + "\" already added.");
        }
    }

    @Override
    public Set<String> getLibraries() {
        return libraries;
    }

    @Override
    public Map<String, String> getImports() {
        return imports;
    }

    @Override
    public String getName() {
        return name;
    }

//    public void addGroovyFile(File file) {
//        groovyFiles.add(file);
//    }
//
//    public Collection<File> getGroovyFiles() {
//        return groovyFiles;
//    }
//
//    public Map<String, JobXML> getJobs() {
//        return jobs;
//    }


    public Collection<String> getJobs() {
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

//        for (JobXML jobXML : jobs.values()) {
//            List<String> validate = jobXML.validate();
//            if (!validate.isEmpty()) {
//                messages.add("Invalid job \"" + jobXML.getName() + "\":\n" + String.join(", ", validate));
//            }
//        }
        return messages;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public List<String> getScriptsLocations() {
        return Collections.singletonList("groovy");
    }
}
