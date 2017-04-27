package org.jobsui.ui.swing;

import org.jobsui.core.ui.UIComponent;
import org.jobsui.core.ui.UIContainer;
import org.jobsui.core.ui.UIWidget;
import org.jobsui.core.utils.JobsUIUtils;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Created by enrico on 2/14/16.
 */
public class SwingUIContainer implements UIContainer<JComponent> {
    private final JPanel component = new JPanel();
    private int rows = 0;

    public SwingUIContainer() {
        component.setLayout(new GridBagLayout());
    }

    @Override
    public UIWidget<JComponent> add(String title, UIComponent<JComponent> component) {
        return addRow(title, component);
    }

    @Override
    public UIWidget<JComponent> add(final UIComponent<JComponent> component) {
        {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = rows++;
            gbc.insets.right = 5;
            gbc.insets.top = 5;
            gbc.insets.left = 5;
            gbc.anchor = GridBagConstraints.WEST;
//            gbc.fill = GridBagConstraints.HORIZONTAL;
            this.component.add(component.getComponent(), gbc);
        }

        final JLabel jMessages;
        {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = rows++;
            gbc.insets.right = 5;
            gbc.insets.left = 5;
            gbc.insets.top = 10;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0;
            gbc.fill = GridBagConstraints.NONE;
            jMessages = new JLabel("");
            jMessages.setForeground(Color.RED);
            jMessages.setVisible(false);
            this.component.add(jMessages, gbc);
        }

        return new UIWidget<JComponent>() {
            private boolean enabled = false;
            @Override
            public void setVisible(boolean visible) {
                getComponent().setVisible(visible);
            }

            @Override
            public void setDisable(boolean value) {
                enabled = !value;
                getComponent().setEnabled(!value);
            }

            @Override
            public UIComponent<JComponent> getComponent() {
                return component;
            }

            @Override
            public void setValidationMessages(List<String> messages) {
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
        };
    }

    @Override
    public void add(UIContainer<JComponent> container) {
        {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = rows;
            gbc.insets.right = 5;
            gbc.insets.top = 5;
            gbc.insets.left = 5;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            this.component.add(container.getComponent(), gbc);
        }
        rows++;

    }

    @Override
    public JComponent getComponent() {
        return component;
    }

    void addFiller() {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.gridy = rows++;

        // filler
        component.add(new JLabel(), constraints);
    }

    private UIWidget<JComponent> addRow(String label, final UIComponent<JComponent> component) {
        component.setTitle(label);

        final JLabel jlabel;
        {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = rows++;
            gbc.insets.right = 5;
            gbc.insets.left = 5;
            gbc.insets.top = 10;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0;
            gbc.fill = GridBagConstraints.NONE;
            jlabel = new JLabel(label);
            this.component.add(jlabel, gbc);
        }

        {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = rows++;
            gbc.weightx = 1.0; // why 1.0 does not work ???
            gbc.insets.top = 2;
            gbc.insets.left = 5;
            gbc.insets.right = 5;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            this.component.add(component.getComponent(), gbc);
        }

        final JLabel jMessages;
        {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = rows++;
            gbc.insets.right = 5;
            gbc.insets.left = 5;
            gbc.insets.top = 10;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0;
            gbc.fill = GridBagConstraints.NONE;
            jMessages = new JLabel("");
            jMessages.setForeground(Color.RED);
            jMessages.setVisible(false);
            this.component.add(jMessages, gbc);
        }

        return new UIWidget<JComponent>() {
            private boolean enabled = false;

            @Override
            public void setVisible(boolean visible) {
                component.setVisible(visible);
                jlabel.setVisible(visible);
            }

            @Override
            public void setDisable(boolean value) {
                enabled = !value;
                getComponent().setEnabled(!value);
            }

            @Override
            public UIComponent<JComponent> getComponent() {
                return component;
            }

            @Override
            public void setValidationMessages(List<String> messages) {
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
        };

    }

}