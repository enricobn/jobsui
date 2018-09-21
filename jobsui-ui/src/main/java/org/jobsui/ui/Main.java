package org.jobsui.ui;

import org.jobsui.core.*;
import org.jobsui.core.bookmark.BookmarksStoreFSImpl;
import org.jobsui.core.groovy.ProjectGroovyBuilder;
import org.jobsui.core.job.Job;
import org.jobsui.core.job.Project;
import org.jobsui.core.repository.RepositoryURLStreamHandlerFactory;
import org.jobsui.core.ui.UI;
import org.jobsui.core.xml.ProjectParserImpl;
import org.jobsui.ui.javafx.JavaFXUI;
import org.jobsui.ui.swing.SwingUI;
import org.joubsui.textui.TextUI;

import java.io.Serializable;
import java.nio.file.FileSystems;
import java.util.prefs.Preferences;

/**
 * Created by enrico on 5/5/16.
 */
public class Main {

    public static void main(String[] args) throws Exception {
        CommandLineArguments.parse(args,
                new ProjectParserImpl(),
                new ProjectGroovyBuilder(),
                FileSystems.getDefault(),
                Main::run,
                errors -> System.err.println(String.join("\n", errors)),
                BookmarksStoreFSImpl.getUserStore());

//        errors -> JavaFXUI.showMessageStatic("Error starting application:\n" +
//                        String.join("\n", errors)));
    }

    private static void run(CommandLineArguments arguments) {
        // TODO add repositories
        RepositoryURLStreamHandlerFactory.getInstance();

        JobsUIPreferences preferences =
                JobsUIPreferencesImpl.get(
                        Preferences.userNodeForPackage(Main.class),
                        BookmarksStoreFSImpl.getUserStore()
                );

        // TODO hard wired UI
        UI ui;
        try {
            switch (arguments.getUiType()) {
                case Swing: ui = new SwingUI(); break;
                case Text: ui = new TextUI(preferences); break;
                default: ui = new JavaFXUI(preferences); break;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        JobsUIApplication application = ui.start(arguments);

        if (arguments.getAction() == StartAction.Run) {
            Project project;
            Job<Serializable> job;
            try {
                project = arguments.getProjectBuilder().build(arguments.getProjectXML(),
                        arguments.getBookmarksStore(), ui);
                job = project.getJob(arguments.getJob());
                if (job == null) {
                    throw new Exception(String.format("Cannot find job %s.", arguments.getJob()));
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            ui.getPreferences().registerOpenedProject(arguments.getProjectURL(),
                    project.getName());
            application.gotoRun(project, job);
        } else if (arguments.getAction() == StartAction.Edit) {
            if (arguments.getProjectFSXML() != null) {
                ui.getPreferences().registerOpenedProject(arguments.getProjectURL(),
                        arguments.getProjectFSXML().getName());
                application.gotoEdit(arguments.getProjectFSXML());
            } else {
                application.gotoNew();
            }
        } else  {
            application.gotoMain();
        }


    }

}
