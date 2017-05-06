package org.jobsui.ui.swing;

import org.jobsui.core.JobsUIMainParameters;
import org.jobsui.core.JobsUIPreferences;
import org.jobsui.core.ui.*;
import org.jobsui.core.utils.JobsUIUtils;

import javax.swing.*;
import java.awt.*;
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
        return new SwingUIWindow(this, title);
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
    public void start(JobsUIMainParameters parameters) {
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

    @Override
    public UIWidget<JComponent> createWidget(String title, UIComponent<JComponent> component) {
        return new SwingUIWidget(title, component);
    }

    private static class SwingUIWidget implements UIWidget<JComponent> {
        private final GridLayout layout;
        private final JPanel panel;
        private final UIComponent<JComponent> component;
        private boolean enabled = false;
        final JLabel jMessages;

        private SwingUIWidget(String title, UIComponent<JComponent> component) {
            this.component = component;

            if (title != null) {
                layout = new GridLayout(3, 1);
            } else {
                layout = new GridLayout(2, 1);
            }

            panel = new JPanel(layout);

            if (title != null) {
                panel.add(new JLabel(title));
            }
            panel.add(component.getComponent());

            jMessages = new JLabel("");
            jMessages.setForeground(Color.RED);
            jMessages.setVisible(false);

            panel.add(jMessages);
        }

        @Override
        public void setVisible(boolean visible) {
            getLayoutComponent().setVisible(visible);
        }

        @Override
        public void setDisable(boolean value) {
            enabled = !value;
            getLayoutComponent().setEnabled(!value);
        }

        @Override
        public UIComponent<JComponent> getComponent() {
            return component;
        }

        @Override
        public void setValidationMessages(java.util.List<String> messages) {
            if (messages.isEmpty()) {
                jMessages.setVisible(false);
            } else {
                jMessages.setVisible(true);
                jMessages.setText(JobsUIUtils.getMessagesAsString(messages));
            }
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }

        @Override
        public JComponent getLayoutComponent() {
            return panel;
        }
    }


}
