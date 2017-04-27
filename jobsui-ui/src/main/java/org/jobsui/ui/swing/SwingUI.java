package org.jobsui.ui.swing;

import org.jobsui.core.JobsUIPreferences;
import org.jobsui.core.ui.*;

import javax.swing.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

/**
 * Created by enrico on 11/2/15.
 */
public class SwingUI implements UI<JComponent> {

    @Override
    public void showMessage(String message) {
        JOptionPane.showMessageDialog(null, message, "JobsUI", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public UIWindow<JComponent> createWindow(String title) {
        return new SwingUIWindow(title);
    }

    @Override
    public void log(String log) {
        // TODO
    }

    @Override
    public void log(String message, Throwable th) {
        // TODO
    }

    @Override
    @SuppressWarnings("unchecked")
    public <COMP extends UIComponent> COMP create(Class<COMP> componentType) throws UnsupportedComponentException {
        if (componentType == UIButton.class) {
            return (COMP) new SwingUIButton();
        } else if (componentType == UIChoice.class) {
            return (COMP) new SwingUIChoice();
        } else if (componentType == UIList.class) {
            return (COMP) new SwingUIList();
        } else if (componentType == UIValue.class) {
            return (COMP) new SwingUIValue();
        } else if (componentType == UICheckBox.class) {
            return (COMP) new SwingUICheckBox();
        }
        throw new UnsupportedComponentException("SWING: cannot find component for " + componentType.getName());
    }

    @Override
    public void showError(String message, Throwable t) {
        // TODO
        try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
            t.printStackTrace(pw);
            showMessage(message + sw.toString());
        } catch (IOException e) {
            e.printStackTrace();
            showMessage(message);
        }
    }

    @Override
    public void start() {
        // TODO
    }

    @Override
    public Optional<String> askString(String message) {
        // TODO
        return null;
    }

    @Override
    public boolean askOKCancel(String message) {
        // TODO
        return false;
    }

    @Override
    public JobsUIPreferences getPreferences() {
        // TODO
        return null;
    }

}
