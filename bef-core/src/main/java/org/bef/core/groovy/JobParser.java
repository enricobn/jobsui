package org.bef.core.groovy;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;
import groovy.util.GroovyScriptEngine;
import org.bef.core.Job;
import org.bef.core.JobParameterDef;
import org.bef.core.JobParameterDefAbstract;
import org.bef.core.Project;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

/**
 * Created by enrico on 5/4/16.
 */
public class JobParser {

    public Project loadProject(File folder) throws Exception {

        final File[] files = folder.listFiles();

        if (files == null) {
            throw new Exception("Cannot find files in " + folder);
        }

        GroovyClassLoader cl;

        final File groovy = new File(folder, "groovy");
        if (groovy.exists()) {
            GroovyScriptEngine engine = new GroovyScriptEngine(new URL[]{groovy.toURI().toURL()});
            cl = engine.getGroovyClassLoader();
        } else {
            cl = new GroovyClassLoader();
        }

        final File lib = new File(folder, "lib");
        if (lib.exists()) {
            final File[] libFiles = lib.listFiles();
            if (libFiles != null) {
                for (File file : libFiles) {
                    cl.addURL(file.toURI().toURL());
                }
            }
        }

        GroovyShell shell = new GroovyShell(cl);

        final Map<String,Job<?>> jobs = new HashMap<>();

        for (File file : files) {
            if (file.getName().endsWith(".befjob")) {
                try (InputStream is = new FileInputStream(file)) {
                    JobGroovy<?> job;
                    try {
                        job = parse(shell, is);
                    } catch (Exception e) {
                        throw new Exception("Cannot parse file " + file, e);
                    }
                    jobs.put(job.getKey(), job);
                }
            }
        }

        return new Project() {
            @Override
            public <T> Job<T> getJob(String key) {
                return (Job<T>) jobs.get(key);
            }

            @Override
            public Set<String> getKeys() {
                return jobs.keySet();
            }
        };
    }

    public <T> JobGroovy<T> parse(GroovyShell shell, InputStream is) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(is);

        //optional, but recommended
        //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
        doc.getDocumentElement().normalize();

        String name = doc.getDocumentElement().getAttribute("name");
        if (name == null || name.isEmpty()) {
            throw new BefParseException("Cannot find property \"name\"");
        }
        String key = doc.getDocumentElement().getAttribute("key");
        if (key == null || key.isEmpty()) {
            throw new BefParseException("Cannot find property \"key\"");
        }
        Map<String,JobParameterDef<?>> parameterDefs = new LinkedHashMap<>();

        String runScript = getElementContent(doc.getDocumentElement(), "Run", true);

        NodeList parametersList = doc.getElementsByTagName("Parameter");
        String validateScript = getElementContent(doc.getDocumentElement(), "Validate", false);

        for (int i = 0; i < parametersList.getLength(); i++) {
            Element element = (Element) parametersList.item(i);
            String parameterKey = element.getAttribute("key");
            String parameterName = element.getAttribute("name");
            String typeString = element.getAttribute("type");
            Class<?> type = shell.getClassLoader().loadClass(typeString);

            String parameterValidateScript = getElementContent(element, "Validate", false);

            String createComponentScript = getElementContent(element, "CreateComponent", true);

            String onDependenciesChangeScript = getElementContent(element, "OnDependenciesChange", false);

            JobParameterDefAbstract<?> parameterDef = new JobParameterDefGroovy<>(shell, parameterKey, parameterName,
                    type, createComponentScript, onDependenciesChangeScript, parameterValidateScript);
            parameterDefs.put(parameterDef.getKey(), parameterDef);

            final NodeList dependenciesList = element.getElementsByTagName("Dependency");
            for (int iDep = 0; iDep < dependenciesList.getLength(); iDep++) {
                final Element dependency = (Element) dependenciesList.item(iDep);
                final String depKey = dependency.getAttribute("key");
                final JobParameterDef<?> jobParameterDefDep = parameterDefs.get(depKey);
                if (jobParameterDefDep == null) {
                    throw new IllegalStateException("Cannot find dependency with key \"" + depKey + "\" for " +
                    "parameter with key \"" + parameterKey + "\".");
                }
                parameterDef.addDependency(jobParameterDefDep);
            }
        }

        return new JobGroovy<>(shell, key, name, new ArrayList<>(parameterDefs.values()), runScript, validateScript);
    }

    private static String getElementContent(Element parent, String name, boolean mandatory) throws BefParseException {
        final NodeList childNodes = parent.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            final String nodeName = childNodes.item(i).getNodeName();
            if (nodeName.equals(name)) {
                return childNodes.item(i).getTextContent();
            }
        }
        if (mandatory) {
            throw new BefParseException("Cannot find " + name + " element in " + parent);
        } else {
            return null;
        }
    }
}
