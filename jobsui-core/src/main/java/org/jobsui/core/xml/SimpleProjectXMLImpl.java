package org.jobsui.core.xml;

import org.jobsui.core.utils.JobsUIUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Created by enrico on 4/20/17.
 */
public class SimpleProjectXMLImpl implements SimpleProjectXML {
    private final URL projectURL;
    private final String id;
    private final Set<String> libraries = new HashSet<>();
    private final Map<String, String> imports = new HashMap<>();
    private final List<String> jobs = new ArrayList<>();
    private String name;
    private String version;

    public SimpleProjectXMLImpl(URL projectURL, String id, String name, String version) {
        this.id = id;
        this.name = name;
        this.projectURL = projectURL;
        this.version = version;
    }

    public URL getRelativeURL(String relativePath) {
        String url;
        if (projectURL.toString().endsWith("/")) {
            url = projectURL + relativePath;
        } else {
            url = projectURL + "/" + relativePath;
        }

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

    public Set<String> getLibraries() {
        return libraries;
    }

    public Map<String, String> getImports() {
        return imports;
    }

    public String getName() {
        return name;
    }

    public Collection<String> getJobs() {
        return jobs;
    }

    public void addJob(String job) {
        jobs.add(job);
    }

    public void setName(String name) {
        this.name = name;
    }

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

    public String getId() {
        return id;
    }

    public List<String> getScriptsLocations() {
        return Collections.singletonList("groovy");
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
