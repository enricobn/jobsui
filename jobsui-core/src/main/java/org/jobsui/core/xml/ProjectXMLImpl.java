package org.jobsui.core.xml;

import org.jobsui.core.ui.UIComponentRegistry;
import org.jobsui.core.ui.UIComponentRegistryComposite;
import org.jobsui.core.ui.UIComponentRegistryImpl;
import org.jobsui.core.ui.UIComponentType;
import org.jobsui.core.utils.JobsUIUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Created by enrico on 10/6/16.
 */
public class ProjectXMLImpl implements ProjectXML {
    private final URL projectURL;
    private final Set<ProjectLibraryXML> libraries = new HashSet<>();
    private final Map<String, String> imports = new HashMap<>();
    private final UIComponentRegistryComposite uiComponentRegistry = new UIComponentRegistryComposite();
    private String id;
    private String name;
    private String version;

    private final Map<String, JobXML> jobXMLs = new HashMap<>();

    ProjectXMLImpl(URL projectURL, String id, String name, String version) {
        this.projectURL = projectURL;
        this.id = id;
        this.name = name;
        this.version = version;
        /*
         * The idea is that new types of UIComponent, or different implementations, can be created by users.
         * TODO find a way to customize it
         */
        UIComponentRegistry customUiComponentRegistry = new UIComponentRegistry() {
            @Override
            public Optional<UIComponentType> getComponentType(String name) {
                return Optional.empty();
            }

            @Override
            public Collection<UIComponentType> getComponentTypes() {
                return Collections.emptyList();
            }
        };
        uiComponentRegistry.add(customUiComponentRegistry);
        uiComponentRegistry.add(new UIComponentRegistryImpl());
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

    public void addLibrary(String library) throws Exception {
        libraries.add(ProjectLibraryXML.of(library));
    }

    public void addImport(String name, String uri) {
        imports.put(name, uri);
    }

    public Set<ProjectLibraryXML> getLibraries() {
        return libraries;
    }

    public Map<String, String> getImports() {
        return imports;
    }

    public String getName() {
        return name;
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

    @Override
    public UIComponentRegistry getUiComponentRegistry() {
        return uiComponentRegistry;
    }

    public void setVersion(String version) {
        this.version = version;
    }


    @Override
    public void addJob(JobXML jobXML) {
        if (jobXMLs.containsKey(jobXML.getId())) {
            throw new IllegalArgumentException("Job with id \"" + jobXML.getId()+ "\" already added.");
        }
        jobXMLs.put(jobXML.getId(), jobXML);
    }

    @Override
    public Collection<JobXML> getJobs() {
        return jobXMLs.values();
    }

    @Override
    public JobXML getJobXMLById(String id) {
        return jobXMLs.get(id);
    }

    public void setId(String id) {
        this.id = id;
    }


}
