package org.jobsui.core.jobstore;

import com.thoughtworks.xstream.XStream;
import org.jobsui.core.job.CompatibleJobId;
import org.jobsui.core.job.CompatibleProjectId;
import org.jobsui.core.job.Job;
import org.jobsui.core.job.Project;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class JobStoreFSImpl<T extends  JobStoreElement> implements JobStore<T> {
    private static final Logger LOGGER = Logger.getLogger(JobStoreFSImpl.class.getName());
    private final File root;

    protected JobStoreFSImpl(File root) {
        this.root = root;
    }

    @Override
    public void save(Project project, Job job, T value) throws IOException {
        File jobRoot = getJobRoot(project, job);
        if (!jobRoot.exists()) {
            if (!jobRoot.mkdirs()) {
                throw new IOException();
            }
        }

        File file = getFile(jobRoot, value);

        try (FileOutputStream out = new FileOutputStream(file);
                Writer fileWriter = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
            XStream xstream = new XStream();
            if (job.getClassLoader() != null) {
                xstream.setClassLoader(job.getClassLoader());
            }
            xstream.toXML(value, fileWriter);
        }
    }

    @Override
    public List<T> get(Project project, Job job) {
        File jobRoot = getJobRoot(project, job);
        if (!jobRoot.exists()) {
            return Collections.emptyList();
        }

        String[] list = jobRoot.list((dir, name) -> name.endsWith(".xml"));
        if (list == null) {
            return Collections.emptyList();
        }

        List<T> result = new ArrayList<>();

        XStream xstream = new XStream();
        if (job.getClassLoader() != null) {
            // TODO is it needed?
            xstream.setClassLoader(job.getClassLoader());
        }

        for (String fileName : list) {
            File file = new File(jobRoot, fileName);
            try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)){
                result.add((T) xstream.fromXML(reader));
            } catch (Exception e) {
                // TODO show a message in UI
                if (LOGGER.isLoggable(Level.SEVERE)) {
                    LOGGER.log(Level.SEVERE, "Error loading from " + file, e);
                }
            }
        }

        return result;
    }

    protected abstract String getFileNameWithoutExtension(T value);

    @Override
    public boolean delete(Project project, Job job, T value) {
        File jobRoot = getJobRoot(project, job);
        File file = getFile(jobRoot, value);
        if (!file.exists()) {
            return false;
        }
        return file.delete();
    }

    private File getFile(File jobRoot, T value) {
        return new File(jobRoot, getFileNameWithoutExtension(value) + ".xml");
    }

    private File getJobRoot(Project project, Job job) {
        CompatibleProjectId projectId = project.getId().toCompatibleProjectId();
        CompatibleJobId jobId = job.getCompatibleJobId();
        return root.toPath()
                .resolve(projectId.getGroupId())
                .resolve(projectId.getModuleId())
                .resolve(Integer.toString(projectId.getMajorVersion()))
                .resolve(jobId.getId())
                .resolve(Integer.toString(jobId.getMajorVersion()))
                .toFile();
    }
}
