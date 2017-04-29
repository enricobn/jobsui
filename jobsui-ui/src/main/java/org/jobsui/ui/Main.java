package org.jobsui.ui;

import org.jobsui.core.JobsUIMainParameters;
import org.jobsui.core.JobsUIPreferences;
import org.jobsui.core.JobsUIPreferencesImpl;
import org.jobsui.core.bookmark.BookmarksStoreFSImpl;
import org.jobsui.ui.javafx.JavaFXUI;

import java.util.prefs.Preferences;
import java.util.stream.Collectors;

/**
 * Created by enrico on 5/5/16.
 */
public class Main {

    public static void main(String[] args) throws Exception {
        JobsUIMainParameters.parse(args,
                Main::run,
                errors -> JavaFXUI.showMessageStatic("Error starting application:\n" +
                        errors.stream().collect(Collectors.joining("\n"))));
    }

    private static void run(JobsUIMainParameters parameters) {
        JobsUIPreferences preferences =
                JobsUIPreferencesImpl.get(Preferences.userNodeForPackage(Main.class), BookmarksStoreFSImpl.getUser());
        new JavaFXUI(preferences).start();
    }

}
