package org.jobsui.core.groovy;

import org.jobsui.core.Project;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by enrico on 10/6/16.
 */
public class ProjectXML {
    private final File projectFolder;
    private final List<String> libraries = new ArrayList<>();
    private final Map<String, Project> imports = new HashMap<>();

    ProjectXML(File projectFolder) {
        this.projectFolder = projectFolder;
    }

    public void addLibrary(String library) {
        libraries.add(library);
    }

    public void addImport(String name, String uri) throws Exception {
        Project project = new JobParser().loadProject(new File(projectFolder, uri));
        imports.put(name, project);
    }

    public List<String> getLibraries() {
        return libraries;
    }

    public Map<String, Project> getImports() {
        return imports;
    }
}
