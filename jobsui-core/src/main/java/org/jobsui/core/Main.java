package org.jobsui.core;

import org.jobsui.core.groovy.JobParser;
import org.jobsui.core.groovy.ProjectGroovyBuilder;
import org.jobsui.core.ui.UI;
import org.jobsui.core.ui.javafx.JavaFXUI;
import org.jobsui.core.ui.swing.SwingUI;
import org.jobsui.core.xml.ProjectXML;

import java.io.File;
import java.io.Serializable;

/**
 * Created by enrico on 5/5/16.
 */
public class Main {

    public static void main(String[] args) throws Exception {
        if (args.length < 2 || args.length > 3) {
            System.out.println("Usage: jobsui projectFolder jobkey [ui]");
            return;
        }

        File projectFolder = new File(args[0]);

        if (!projectFolder.exists()) {
            System.out.println("Folder " + projectFolder + " does not exist.");
            return;
        }

        ProjectXML projectXML = new JobParser().loadProject(projectFolder);
        final Project project = new ProjectGroovyBuilder().build(projectXML);

        String key = args[1];

        final Job<Serializable> job = project.getJob(key);

        if (job == null) {
            System.out.println("Cannot find project with key \"" + key + "\" in folder " + projectFolder + " .");
            return;
        }

        JobRunner runner = new JobRunner();

        UI ui;
        if (args.length < 3) {
            ui = new JavaFXUI();
        } else if ("swing".equals(args[2].toLowerCase())) {
            ui = new SwingUI();
        } else {
            ui = new JavaFXUI();
        }

        JobValues values = runner.getValues(ui, job);
        System.out.println(values);

//        final JobFuture<? extends Serializable> future = runner.run(ui, job);
//
//        if (future != null) {
//            future.get();
//        }
    }

}
