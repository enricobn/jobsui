package org.jobsui.ui.javafx.edit;

import org.jobsui.core.xml.*;

import java.util.function.Function;

public enum ItemType {
    Project(ProjectXML.class, ProjectXML::getName),
        Libraries(ProjectXML.class, p -> "libraries"),
            Library(ProjectLibraryXML.class, ProjectLibraryXML::toString),
        Scripts(ProjectXML.class, p -> "scripts"),
            ScriptsLocation(String.class, p -> p),
                ScriptFile(String.class, p -> p),
        Job(JobXML.class, JobXML::getName),
            WizardSteps(JobXML.class, p -> "wizards"),
                WizardStep(WizardStep.class, org.jobsui.core.xml.WizardStep::getName),
                    WizardStepDependencies(WizardStep.class, p -> "parameters"),
                        WizardStepDependency(ParameterXML.class, ParameterXML::getName),
            Parameters(JobXML.class, p -> "parameters"),
                Parameter(SimpleParameterXML.class, SimpleParameterXML::getName),
                    Dependencies(ParameterXML.class, p -> "dependencies"),
                        Dependency(ParameterXML.class, ParameterXML::getName),
            Expressions(JobXML.class, p -> "expressions"),
                Expression(ExpressionXML.class, ExpressionXML::getName),
                    //Dependencies
                        //Dependency
            Calls(JobXML.class, p -> "calls"),
                Call(CallXML.class, CallXML::getName);
                    //Dependencies
                        //Dependency

    private final Class<?> payloadType;
    private final Function<Object,String> titleFunction;

    <T> ItemType(Class<T> payloadType, Function<T, String> titleFunction) {
        this.payloadType = payloadType;
        this.titleFunction = (Function<Object, String>) titleFunction;
    }

    public Class<?> getPayloadType() {
        return payloadType;
    }

    public Function<Object, String> getTitleFunction() {
        return titleFunction;
    }
}
