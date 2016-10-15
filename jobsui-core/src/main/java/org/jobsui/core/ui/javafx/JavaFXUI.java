package org.jobsui.core.ui.javafx;

import javafx.scene.Node;
import org.jobsui.core.ui.*;

/**
 * Created by enrico on 10/7/16.
 */
public class JavaFXUI implements UI<Node> {
    @Override
    public void showMessage(String message) {

    }

    @Override
    public UIWindow<Node> createWindow(String title) {
        return new JavaFXUIWindow();
    }

    @Override
    public void log(String message) {

    }

    @Override
    public void log(String message, Throwable th) {

    }

    @Override
    @SuppressWarnings("unchecked")
    public <COMP extends UIComponent> COMP create(Class<COMP> componentType) throws UnsupportedComponentException {
        if (componentType == UIChoice.class) {
            return (COMP) new JavaFXUIChoice<>();
        } else if (componentType == UICheckBox.class) {
            return (COMP) new JavaFXUICheckBox();
        }
        throw new UnsupportedComponentException("JavaFX: cannot find component for " + componentType.getName());
    }
}
