package org.jobsui.ui;

import org.jobsui.core.CommandLineArguments;
import org.jobsui.core.JobsUIPreferences;
import org.jobsui.core.JobsUIPreferencesImpl;
import org.jobsui.core.bookmark.BookmarksStoreFSImpl;
import org.jobsui.core.groovy.ProjectGroovyBuilder;
import org.jobsui.core.repository.RepositoryURLStreamHandlerFactory;
import org.jobsui.core.ui.UI;
import org.jobsui.core.xml.ProjectParserImpl;
import org.jobsui.ui.javafx.JavaFXUI;

import java.nio.file.FileSystems;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * Created by enrico on 5/5/16.
 */
public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        CommandLineArguments.parse(args,
                new ProjectParserImpl(),
                new ProjectGroovyBuilder(),
                FileSystems.getDefault(),
                Main::run,
                errors -> LOGGER.severe(String.join("\n", errors)),
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
        UI ui = new JavaFXUI(preferences);
        ui.start(arguments);
    }

}
