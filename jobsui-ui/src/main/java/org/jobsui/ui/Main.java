package org.jobsui.ui;

import org.jobsui.core.JobsUIMainParameters;
import org.jobsui.core.JobsUIPreferences;
import org.jobsui.core.JobsUIPreferencesImpl;
import org.jobsui.core.bookmark.BookmarksStoreFSImpl;
import org.jobsui.core.repository.RepositoriesProvider;
import org.jobsui.core.repository.RepositoriesProviderImpl;
import org.jobsui.core.repository.RepositoryURLStreamHandler;
import org.jobsui.core.repository.RepositoryURLStreamHandlerFactory;
import org.jobsui.ui.javafx.JavaFXUI;

import java.net.URL;
import java.util.prefs.Preferences;

/**
 * Created by enrico on 5/5/16.
 */
public class Main {

    public static void main(String[] args) throws Exception {
        JobsUIMainParameters.parse(args,
                Main::run,
                errors -> System.err.println(String.join("\n", errors)));

//        errors -> JavaFXUI.showMessageStatic("Error starting application:\n" +
//                        String.join("\n", errors)));
    }

    private static void run(JobsUIMainParameters parameters) {
        // TODO add repositories
        RepositoryURLStreamHandlerFactory.getInstance();

        JobsUIPreferences preferences =
                JobsUIPreferencesImpl.get(Preferences.userNodeForPackage(Main.class), BookmarksStoreFSImpl.getUser());
        new JavaFXUI(preferences).start();
    }

}
