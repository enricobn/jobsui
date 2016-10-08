package org.jobsui.core;

import org.jobsui.core.groovy.JobParser;
import org.jobsui.core.ui.UI;
import org.jobsui.core.ui.javafx.JavaFXUI;
import org.jobsui.core.ui.swing.SwingUI;

import java.io.File;

/**
 * Created by enrico on 5/5/16.
 */
public class Main {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Usage: jobsui projectfolder jobkey");
            return;
        }

        File projectFolder = new File(args[0]);

        if (!projectFolder.exists()) {
            System.out.println("Folder " + projectFolder + " does not exist.");
            return;
        }

        JobParser parser = new JobParser();
        final Project project = parser.loadProject(projectFolder);

        String key = args[1];

        final Job<?> job = project.getJob(key);

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

        final JobFuture<?> future = runner.run(ui, job);

        if (future != null) {
            future.get();
        }
    }
}
