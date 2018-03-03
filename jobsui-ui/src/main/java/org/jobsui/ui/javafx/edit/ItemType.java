package org.jobsui.ui.javafx.edit;

import org.jobsui.core.xml.*;

enum ItemType {
    Project(ProjectXML.class),
        Libraries(ProjectXML.class),
            Library(ProjectLibraryXML.class),
        Scripts(String.class),
            ScriptFile(String.class),
        Job(JobXML.class),
            Parameters(JobXML.class),
                Parameter(SimpleParameterXML.class),
                    Dependencies(ParameterXML.class),
                        Dependency(String.class),
            Expressions(JobXML.class),
                Expression(ExpressionXML.class),
                    //Dependencies
                        //Dependency
            Calls(JobXML.class),
                Call(CallXML.class);
                    //Dependencies
                        //Dependency

    private final Class<?> payloadType;

    ItemType(Class<?> payloadType) {
        this.payloadType = payloadType;
    }

    public Class<?> getPayloadType() {
        return payloadType;
    }
}
