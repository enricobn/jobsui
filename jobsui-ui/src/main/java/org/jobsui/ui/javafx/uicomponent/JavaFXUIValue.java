package org.jobsui.ui.javafx.uicomponent;

import com.jfoenix.controls.JFXTextField;
import javafx.scene.control.TextField;
import org.jobsui.core.JobsUIPreferences;
import org.jobsui.core.ui.JobsUITheme;
import org.jobsui.ui.javafx.JavaFXUI;

/**
 * Created by enrico on 3/30/17.
 */
public class JavaFXUIValue extends JavaFXUIValueAbstract {

    public JavaFXUIValue(JavaFXUI ui) {
        super(createTextField(ui.getPreferences()));
    }

    private static TextField createTextField(JobsUIPreferences preferences) {
        TextField result;
        if (preferences.getTheme() == JobsUITheme.Material) {
            result = new JFXTextField();
        } else {
            result = new TextField();
        }
        return result;
    }
}
