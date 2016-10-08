package org.jobsui.core.ui.javafx;

import javafx.scene.Node;
import org.jobsui.core.ui.UI;
import org.jobsui.core.ui.UIComponent;
import org.jobsui.core.ui.UIWindow;
import org.jobsui.core.ui.UnsupportedComponentException;

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
    public <COMP extends UIComponent> COMP create(Class<COMP> componentType) throws UnsupportedComponentException {
        return (COMP) new JavaFXUIChoice<>();
    }
}
