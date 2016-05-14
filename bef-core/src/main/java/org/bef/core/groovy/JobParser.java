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
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

/**
 * Created by enrico on 5/4/16.
 */
public class JobParser {

    private final Validator validator;

    public JobParser() throws SAXException {
        String language = XMLConstants.W3C_XML_SCHEMA_NS_URI;
        SchemaFactory factory = SchemaFactory.newInstance(language);
        Schema schema = factory.newSchema(getClass().getResource("/org/bef/befjob.xsd"));
        validator = schema.newValidator();
    }

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
                    final StreamSource source = new StreamSource(is);
                    try {
                        validator.validate(source);
                    } catch (Exception e) {
                        throw new Exception("Cannot parse file " + file, e);
                    }
                }

                try (InputStream is = new FileInputStream(file)) {
                    JobGroovy<?> job;
                    try {
                        job = parse(shell, is, folder);
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

    private <T> JobGroovy<T> parse(GroovyShell shell, InputStream is, File projectFolder) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setValidating(false);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

        Document doc = dBuilder.parse(is);

        //optional, but recommended
        //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
        doc.getDocumentElement().normalize();

        String name = getMandatoryAttribute(doc.getDocumentElement(), "name");
        String key = getMandatoryAttribute(doc.getDocumentElement(), "key");
        Map<String,JobParameterDef<?>> parameterDefs = new LinkedHashMap<>();

        String runScript = getElementContent(doc.getDocumentElement(), "Run", true);

        NodeList parametersList = doc.getElementsByTagName("Parameter");
        String validateScript = getElementContent(doc.getDocumentElement(), "Validate", false);

        for (int i = 0; i < parametersList.getLength(); i++) {
            Element element = (Element) parametersList.item(i);
            String parameterKey = getMandatoryAttribute(element, "key");
            String parameterName = getMandatoryAttribute(element, "name");
//            String typeString = getMandatoryAttribute(element, "type");
            String visibleString = element.getAttribute("visible");
//            Class<?> type = Class.forName(typeString, false, shell.getClassLoader());

            String parameterValidateScript = getElementContent(element, "Validate", false);

            String createComponentScript = getElementContent(element, "CreateComponent", true);

            String onDependenciesChangeScript = getElementContent(element, "OnDependenciesChange", false);

            boolean visible = visibleString == null || visibleString.isEmpty() || Boolean.parseBoolean(visibleString);

            JobParameterDefAbstract<?> parameterDef = new JobParameterDefGroovy<>(shell, parameterKey, parameterName,
                    createComponentScript, onDependenciesChangeScript, parameterValidateScript, visible);
            parameterDefs.put(parameterDef.getKey(), parameterDef);

            final NodeList dependenciesList = element.getElementsByTagName("Dependency");
            for (int iDep = 0; iDep < dependenciesList.getLength(); iDep++) {
                final Element dependency = (Element) dependenciesList.item(iDep);
                final String depKey = getMandatoryAttribute(dependency, "key");
                final JobParameterDef<?> jobParameterDefDep = parameterDefs.get(depKey);
                if (jobParameterDefDep == null) {
                    throw new IllegalStateException("Cannot find dependency with key \"" + depKey + "\" for " +
                    "parameter with key \"" + parameterKey + "\".");
                }
                parameterDef.addDependency(jobParameterDefDep);
            }
        }

        return new JobGroovy<>(shell, key, name, new ArrayList<>(parameterDefs.values()), runScript, validateScript,
                projectFolder);
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
//            if (parent.getUserData("lineNumber") != null) {
//                throw new BefParseException("Cannot find mandatory element \"" + name + "\" in " + parent +
//                        parent.getUserData("lineNumber"));
//            } else {
                throw new BefParseException("Cannot find mandatory element \"" + name + "\" in " + parent);
//            }
        } else {
            return null;
        }
    }

    private static String getMandatoryAttribute(Element parent, String name) throws BefParseException {
        final String attribute = parent.getAttribute(name);

        if (attribute == null || attribute.length() == 0) {
//            if (parent instanceof DeferredNode) {
//                throw new BefParseException("Cannot find mandatory attribute \"" + name + "\" in " + parent + " at line " +
//                        ((DeferredNode)parent).getNodeIndex());
//            } else {
                throw new BefParseException("Cannot find mandatory attribute \"" + name + "\" in " + parent);
//            }
        }
        return attribute;
    }
}
