package org.jobsui.ui.javafx.edit;

import org.jobsui.core.xml.*;

import java.util.List;
import java.util.function.Function;

public enum ItemType {
    Project(ProjectXML.class, ProjectXML::getName),
        Libraries(ProjectXML.class, p -> "libraries"),
            Library(ProjectLibraryXML.class, ProjectLibraryXML::toString),
        Scripts(ProjectXML.class, p -> "scripts"),
            ScriptsLocation(String.class, p -> p),
                ScriptFile(String.class, p -> p),
        Job(JobXML.class, JobXML::getName),
            Pages(List.class, p -> "pages"),
                Page(JobPage.class, JobPage::getName),
                    PageDependencies(JobPage.class, p -> "parameters"),
                        PageDependency(ParameterXML.class, ParameterXML::getName),
            Parameters(List.class, p -> "parameters"),
                Parameter(SimpleParameterXML.class, SimpleParameterXML::getName),
                    Dependencies(ParameterXML.class, p -> "dependencies"),
                        Dependency(ParameterXML.class, ParameterXML::getName),
            Expressions(List.class, p -> "expressions"),
                Expression(ExpressionXML.class, ExpressionXML::getName),
                    //Dependencies
                        //Dependency
            Calls(List.class, p -> "calls"),
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
