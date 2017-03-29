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
import org.xml.sax.SAXException;

import javax.swing.*;
import java.io.Serializable;
import java.util.Collection;

/**
 * Created by enrico on 5/5/16.
 */
public class Main {

    public static void main(String[] args) throws Exception {
        JobsUIMainParameters.parse(args, (mainParameters) -> {
            try {
                parseSuccess(mainParameters);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, Main::parseFail);
    }

    private static void parseFail(Collection<String> errors) {
        errors.forEach(System.out::println);
    }

    private static void parseSuccess(JobsUIMainParameters mainParameters) throws Exception {
//        Logger rootLogger = Logger.getLogger("");
//        rootLogger.setLevel(Level.FINE);
//        rootLogger.getHandlers()[0].setLevel(Level.FINE);

//        File projectFolder = new File(mainParameters.getProjectRoot());
//
//        if (!projectFolder.exists()) {
//            System.out.println("Folder " + projectFolder + " does not exist.");
//            return;
//        }

        ProjectXML projectXML = JobParser.getParser(mainParameters.getProjectRoot()).parse();
        Project project = new ProjectGroovyBuilder().build(projectXML);

        final Job<Serializable> job = project.getJob(mainParameters.getJobKey());

        if (job == null) {
            System.out.println("Cannot find project with key \"" + mainParameters.getJobKey() + "\" in folder " + mainParameters.getProjectRoot() + " .");
            return;
        }

        Serializable result;

        if (mainParameters.getUiType() == JobsUIMainParameters.UIType.Swing) {
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
