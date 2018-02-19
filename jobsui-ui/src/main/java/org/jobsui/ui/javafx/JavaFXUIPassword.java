package org.jobsui.ui.javafx;

import com.jfoenix.controls.JFXPasswordField;
import javafx.scene.Node;
import javafx.scene.control.PasswordField;
import org.jobsui.core.JobsUIPreferences;
import org.jobsui.core.ui.JobsUITheme;
import org.jobsui.core.ui.UIPassword;

/**
 * Created by enrico on 3/30/17.
 */
public class JavaFXUIPassword extends JavaFXUIValueAbstract implements UIPassword<Node> {

    public JavaFXUIPassword(JavaFXUI ui) {
        super(createPasswordField(ui.getPreferences()));
    }

    private static PasswordField createPasswordField(JobsUIPreferences preferences) {
        PasswordField result;
        if (preferences.getTheme() == JobsUITheme.Material) {
            result = new JFXPasswordField();
        } else {
            result = new PasswordField();
        }
        return result;
    }
}
