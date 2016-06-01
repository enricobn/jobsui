package org.jobsui.core.ui.swing;

import org.jobsui.core.ui.*;

import javax.swing.*;

/**
 * Created by enrico on 11/2/15.
 */
public class SwingUI implements UI<JComponent> {

    @Override
    public void showMessage(String message) {
        JOptionPane.showMessageDialog(null, message, "JobsUI", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public UIWindow createWindow(String title) {
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
    public <COMP extends UIComponent> COMP create(Class<COMP> componentType) throws UnsupportedComponentException {
        if (componentType == UIButton.class) {
            return (COMP) new SwingUIButton();
        } else if (componentType == UIChoice.class) {
            return (COMP) new SwingUIChoice<>();
        } else if (componentType == UIList.class) {
            return (COMP) new SwingUIList<>();
        } else if (componentType == UIValue.class) {
            return (COMP) new SwingUIValue<>();
        } else if (componentType == UICheckBox.class) {
            return (COMP) new SwingUICheckBox();
        }
        throw new UnsupportedComponentException("SWING: cannot find component for " + componentType.getName());
    }
}
