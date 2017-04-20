package org.jobsui.core.xml;

import java.net.URL;
import java.util.*;

/**
 * Created by enrico on 10/6/16.
 */
class ProjectXMLImpl extends SimpleProjectXMLImpl implements ProjectXML {
    private final Map<String, JobXML> jobXMLs = new HashMap<>();

    ProjectXMLImpl(URL projectURL, String id, String name) {
        super(projectURL, id, name);
    }

//    public JobParser getParser(String relativePath) throws Exception {
//        File path = new File(projectURL, relativePath);
//        return JobParser.getParser(path.getAbsolutePath());
//    }

    public void addJob(String job, JobXML jobXML) {
        if (jobXMLs.containsKey(job)) {
            throw new IllegalArgumentException("Job \"" + job + "\" already added.");
        }
        jobXMLs.put(job, jobXML);
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

    @Override
    public Collection<String> getJobs() {
        return jobXMLs.keySet();
    }

    @Override
    public JobXML getJobXML(String job) {
        return jobXMLs.get(job);
    }

}
