package org.jobsui.core;

import org.apache.commons.cli.*;

import java.io.PrintWriter;
import java.io.StringWriter;
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

    private JobsUIMainParameters(UIType uiType) {
        this.uiType = uiType;
    }

    public enum UIType {
        Swing,
        JavFX
    }

    public static boolean parse(String[] args, Consumer<JobsUIMainParameters> onSuccess, Consumer<List<String>> onError) {
        List<String> validation = new ArrayList<>();

//        Option run = Option.builder("run")
//                .numberOfArgs(2)
//                .argName("project> <job" )
//                .build();

        Option ui = Option.builder("ui")
                .hasArg()
                .argName("ui type")
                .build();

        Options options = new Options();
//        options.addOption(run);
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
                UIType uiType = UIType.JavFX;
                if (line.hasOption("ui")) {
                    String uiTypeString = line.getOptionValue("ui");

                    if ("swing".equals(uiTypeString.toLowerCase())) {
                        uiType = UIType.Swing;
                    } else if ("javafx".equals(uiTypeString.toLowerCase())) {
                        uiType = UIType.JavFX;
                    } else {
                        uiType = null;
                        validation.add(String.format("Unknown ui type '%s'.",uiTypeString));
                    }
                }

                if (line.hasOption("run")) {
                    String[] values = line.getOptionValues("run");
                    String projectString = values[0];
                    String jobString = values[1];
                    System.out.println(String.format("Project %s Job %s.", projectString, jobString));
                }

                if (validation.isEmpty()) {
                    JobsUIMainParameters parameters = new JobsUIMainParameters(uiType);
                    onSuccess.accept(parameters);
                    return true;
                } else {
                    onError.accept(validation);
                    return false;
                }

            }
        } catch( ParseException exp ) {
            onError.accept(Arrays.asList(exp.getMessage(), getHelp(options)));
            return false;
        }
    }

    public UIType getUiType() {
        return uiType;
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
