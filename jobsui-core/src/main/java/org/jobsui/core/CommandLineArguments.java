package org.jobsui.core;

import org.apache.commons.cli.*;
import org.jobsui.core.bookmark.BookmarksStore;
import org.jobsui.core.job.ProjectBuilder;
import org.jobsui.core.utils.JobsUIUtils;
import org.jobsui.core.xml.ProjectFSXML;
import org.jobsui.core.xml.ProjectParser;
import org.jobsui.core.xml.ProjectXML;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * A class for the command line arguments.
 */
public class CommandLineArguments {
    private final UIType uiType;
    private final ProjectBuilder projectBuilder;
    private final BookmarksStore bookmarksStore;
    private StartAction action = StartAction.None;

    private String job;
    private ProjectFSXML projectFSXML;
    private URL projectURL;
    private ProjectXML projectXML;

    private CommandLineArguments(UIType uiType, ProjectBuilder projectBuilder, BookmarksStore bookmarksStore) {
        this.uiType = uiType;
        this.projectBuilder = projectBuilder;
        this.bookmarksStore = bookmarksStore;
    }

    private void setAction(StartAction action) {
        this.action = action;
    }

    public StartAction getAction() {
        return action;
    }

    /**
     * Parses the command line parameters.
     */
    public static boolean parse(String[] args, ProjectParser projectParser, ProjectBuilder projectBuilder,
                                FileSystem fileSystem, Consumer<CommandLineArguments> onSuccess,
                                Consumer<List<String>> onError, BookmarksStore bookmarksStore) {

        Option run = Option.builder("run")
                .numberOfArgs(2)
                .argName("project> <job")
                .build();

        Option edit = Option.builder("edit")
                .hasArg()
                .argName("project").optionalArg(true)
                .build();

        Option ui = Option.builder("ui")
                .hasArg()
                .argName("ui type")
                .build();

        Options options = new Options();
        options.addOption(run);
        options.addOption(edit);
        options.addOption(ui);
        options.addOption("help", "");

        CommandLineParser parser = new DefaultParser();
        try {
            // parse the command line arguments
            CommandLine line = parser.parse(options, args);
            if (!line.getArgList().isEmpty()) {
                onError.accept(Arrays.asList("Unrecognized parameters.", getHelp(options)));
                return false;
            } else if (line.hasOption("help")) {
                onError.accept(Collections.singletonList(getHelp(options)));
                return false;
            } else {
                CommandLineArguments arguments;

                UIType uiType;
                if (line.hasOption("ui")) {
                    String uiTypeString = line.getOptionValue("ui");

                    if ("swing".equals(uiTypeString.toLowerCase(Locale.ENGLISH))) {
                        uiType = UIType.Swing;
                    } else if ("javafx".equals(uiTypeString.toLowerCase(Locale.ENGLISH))) {
                        uiType = UIType.JavaFX;
                    } else {
                        onError.accept(Collections.singletonList(String.format("Unknown ui type '%s'.", uiTypeString)));
                        return false;
                    }
                } else {
                    uiType = UIType.JavaFX;
                }

                arguments = new CommandLineArguments(uiType, projectBuilder, bookmarksStore);

                if (line.hasOption(run.getOpt())) {
                    String[] values = line.getOptionValues(run.getOpt());
                    String projectString = values[0];
                    String jobString = values[1];
                    try {
                        // in case of run either a folder or an URL can be specified
                        URL url = getUrl(projectString);

                        ProjectXML projectXML = projectParser.parse(url);

                        arguments.setProjectURL(url);
                        arguments.setProjectXML(projectXML);
                        arguments.setAction(StartAction.Run);
                        arguments.setJob(jobString);
                    } catch (Exception e) {
                        toError(onError, e);
                        return false;
                    }
                } else if (line.hasOption(edit.getOpt())) {
                    String projectString = line.getOptionValue(edit.getOpt());
                    try {
                        arguments.setAction(StartAction.Edit);

                        if (projectString != null && !projectString.isEmpty()) {
                            Path path = fileSystem.getPath(projectString);
                            File folder = path.toFile();
                            if (!folder.exists() || !folder.isDirectory()) {
                                onError.accept(Collections.singletonList(
                                        String.format("%s is not a file, does not exist or is not a directory.", folder)));
                                return false;
                            }

                            ProjectFSXML projectFSXML = projectParser.parse(folder);
                            arguments.setProjectURL(path.toUri().toURL());
                            arguments.setProjectFSXML(projectFSXML);
                        }
                    } catch (Exception e) {
                        toError(onError, e);
                        return false;
                    }
                }
                onSuccess.accept(arguments);
                return true;
            }
        } catch (ParseException exp) {
            onError.accept(Arrays.asList(exp.getMessage(), getHelp(options)));
            return false;
        }
    }

    private static URL getUrl(String projectString) throws MalformedURLException {
        try {
            return new URL(projectString);
        } catch (MalformedURLException e) {
            return new File(projectString).toURI().toURL();
        }
    }

    private void setProjectXML(ProjectXML projectXML) {
        this.projectXML = projectXML;
    }

    public ProjectXML getProjectXML() {
        return projectXML;
    }

    private static void toError(Consumer<List<String>> onError, Exception e) {
        String message = e.getMessage();
        if (message == null) {
            message = e.toString();
        }
        onError.accept(Arrays.asList(message, JobsUIUtils.toString(e)));
    }

    public UIType getUiType() {
        return uiType;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public ProjectFSXML getProjectFSXML() {
        return projectFSXML;
    }

    private void setProjectFSXML(ProjectFSXML projectFSXML) {
        this.projectFSXML = projectFSXML;
    }

    private void setProjectURL(URL projectURL) {
        this.projectURL = projectURL;
    }

    public URL getProjectURL() {
        return projectURL;
    }

    public BookmarksStore getBookmarksStore() {
        return bookmarksStore;
    }

    public ProjectBuilder getProjectBuilder() {
        return projectBuilder;
    }

    private static String getHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        StringWriter out = new StringWriter();
        PrintWriter pw = new PrintWriter(out);
        formatter.printHelp(
                pw,
                80,
                "jobsUI",
                "",
                options,
                4,
                1,
                "",
                false
        );
        return out.toString();
    }
}
