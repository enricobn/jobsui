package org.jobsui.core;

import org.apache.commons.cli.*;
import org.jobsui.core.groovy.ProjectGroovy;
import org.jobsui.core.groovy.ProjectGroovyBuilder;
import org.jobsui.core.job.Job;
import org.jobsui.core.job.Project;
import org.jobsui.core.xml.ProjectFSXML;
import org.jobsui.core.xml.ProjectParserImpl;
import org.jobsui.core.xml.ProjectXML;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by enrico on 3/29/17.
 */
public class JobsUIMainParameters {
    private final UIType uiType;
    private StartAction action = StartAction.None;
    private Project project;

    private Job job;
    private ProjectFSXML projectFSXML;
    private URL projectURL;

    private JobsUIMainParameters(UIType uiType) {
        this.uiType = uiType;
    }

    public void setAction(StartAction action) {
        this.action = action;
    }

    public StartAction getAction() {
        return action;
    }

    public static boolean parse(String[] args, Consumer<JobsUIMainParameters> onSuccess, Consumer<List<String>> onError) {
        List<String> validation = new ArrayList<>();

        Option run = Option.builder("run")
                .numberOfArgs(2)
                .argName("project> <job")
                .build();

        Option edit = Option.builder("edit")
                .hasArg()
                .argName("project")
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
                JobsUIMainParameters parameters;

                UIType uiType = UIType.JavFX;
                if (line.hasOption("ui")) {
                    String uiTypeString = line.getOptionValue("ui");

                    if ("swing".equals(uiTypeString.toLowerCase())) {
                        uiType = UIType.Swing;
                    } else if ("javafx".equals(uiTypeString.toLowerCase())) {
                        uiType = UIType.JavFX;
                    } else {
                        uiType = null;
                        validation.add(String.format("Unknown ui type '%s'.", uiTypeString));
                    }
                }

                parameters = new JobsUIMainParameters(uiType);

                if (line.hasOption(run.getOpt())) {
                    String[] values = line.getOptionValues(run.getOpt());
                    String projectString = values[0];
                    String jobString = values[1];
                    try {
                        // in case of run either a folder or an URL can be specified
                        URL url;
                        try {
                            url = new URL(projectString);
                        } catch (MalformedURLException e) {
                            url = new File(projectString).toURI().toURL();
                            System.out.println(url);
                        }

                        ProjectXML projectXML = new ProjectParserImpl().parse(url);
                        ProjectGroovy project = new ProjectGroovyBuilder().build(projectXML);
                        Job<Object> job = project.getJob(jobString);
                        if (job == null) {
                            throw new Exception(String.format("Cannot find job %s.", jobString));
                        }
                        parameters.setProjectURL(url);
                        parameters.setAction(StartAction.Run);
                        parameters.setProject(project);
                        parameters.setJob(job);
                    } catch (Exception e) {
                        onError.accept(Collections.singletonList(e.getMessage()));
                        return false;
                    }
                } else if (line.hasOption(edit.getOpt())) {
                    String projectString = line.getOptionValue(edit.getOpt());
                    try {
                        File folder = new File(projectString);
                        if (!folder.exists() || !folder.isDirectory()) {
                            onError.accept(Collections.singletonList(
                                    String.format("%s is not a file, does not exist or is not a directory.", folder)));
                            return false;
                        }
                        ProjectFSXML projectFSXML = new ProjectParserImpl().parse(folder);
                        parameters.setProjectURL(folder.toURI().toURL());
                        parameters.setAction(StartAction.Edit);
                        parameters.setProjectFSXML(projectFSXML);
                    } catch (Exception e) {
                        onError.accept(Collections.singletonList(e.getMessage()));
                        return false;
                    }
                }

                if (validation.isEmpty()) {
                    onSuccess.accept(parameters);
                    return true;
                } else {
                    onError.accept(validation);
                    return false;
                }

            }
        } catch (ParseException exp) {
            onError.accept(Arrays.asList(exp.getMessage(), getHelp(options)));
            return false;
        }
    }

    public UIType getUiType() {
        return uiType;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public ProjectFSXML getProjectFSXML() {
        return projectFSXML;
    }

    public void setProjectFSXML(ProjectFSXML projectFSXML) {
        this.projectFSXML = projectFSXML;
    }

    public void setProjectURL(URL projectURL) {
        this.projectURL = projectURL;
    }

    public URL getProjectURL() {
        return projectURL;
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
