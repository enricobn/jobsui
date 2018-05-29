package org.jobsui.ui;

import org.jobsui.core.CommandLineArguments;
import org.jobsui.core.JobsUIPreferences;
import org.jobsui.core.JobsUIPreferencesImpl;
import org.jobsui.core.bookmark.BookmarksStoreFSImpl;
import org.jobsui.core.groovy.ProjectGroovyBuilder;
import org.jobsui.core.repository.RepositoryURLStreamHandlerFactory;
import org.jobsui.core.xml.ProjectParserImpl;
import org.jobsui.ui.javafx.JavaFXUI;

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

        new JavaFXUI(preferences).start(arguments);
    }

}
