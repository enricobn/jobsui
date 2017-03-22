package org.jobsui.core;

import javafx.scene.Node;
import org.jobsui.core.groovy.JobParser;
import org.jobsui.core.groovy.ProjectGroovyBuilder;
import org.jobsui.core.job.Job;
import org.jobsui.core.runner.JobUIRunner;
import org.jobsui.core.ui.UI;
import org.jobsui.core.ui.javafx.JavaFXUI;
import org.jobsui.core.ui.swing.SwingUI;
import org.jobsui.core.xml.ProjectXML;

import javax.swing.*;
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

        String projectRoot = args[0];

//        File projectFolder = new File(args[0]);
//
//        if (!projectFolder.exists()) {
//            System.out.println("Folder " + projectFolder + " does not exist.");
//            return;
//        }

        ProjectXML projectXML = JobParser.getParser(projectRoot).parse();
        final Project project = new ProjectGroovyBuilder().build(projectXML);

        String key = args[1];

        final Job<Serializable> job = project.getJob(key);

        if (job == null) {
            System.out.println("Cannot find project with key \"" + key + "\" in folder " + projectRoot + " .");
            return;
        }

        Serializable result;

        if (args.length >= 3 && "swing".equals(args[2].toLowerCase())) {
            UI<JComponent> ui = new SwingUI();
            JobUIRunner<JComponent> runner = new JobUIRunner<>(ui);
            result = runner.run(job);
        } else {
            UI<Node> ui = new JavaFXUI();
            JobUIRunner<Node> runner = new JobUIRunner<>(ui);
            result = runner.run(job);
        }

//        final JobResult<? extends Serializable> result = runner.run(ui, job);
//
//        if (result != null) {
//            result.get();
//        }
    }

}
