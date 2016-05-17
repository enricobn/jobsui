package org.bef.core.ui.swing;

import org.bef.core.ui.UIComponent;
import org.bef.core.ui.UIContainer;
import org.bef.core.ui.UIWidget;
import org.bef.core.utils.BEFUtils;

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
    public <T> UIWidget<T, JComponent> add(String title, UIComponent<T, JComponent> component) {
        return addRow(title, component);
    }

    @Override
    public <T> UIWidget<T, JComponent> add(final UIComponent<T, JComponent> component) {
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

        return new UIWidget<T, JComponent>() {
            @Override
            public void setVisible(boolean visible) {
                getComponent().setVisible(visible);
            }

            @Override
            public UIComponent<T, JComponent> getComponent() {
                return component;
            }

            @Override
            public void setValidationMessages(List<String> messages) {
                if (messages.isEmpty()) {
                    jMessages.setVisible(false);
                } else {
                    jMessages.setVisible(true);
                    jMessages.setText(BEFUtils.getMessagesAsString(messages));
                }
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

    private <T> UIWidget<T, JComponent> addRow(String label, final UIComponent<T, JComponent> component) {
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

        return new UIWidget<T, JComponent>() {
            @Override
            public void setVisible(boolean visible) {
                component.setVisible(visible);
                jlabel.setVisible(visible);
            }

            @Override
            public UIComponent<T, JComponent> getComponent() {
                return component;
            }

            @Override
            public void setValidationMessages(List<String> messages) {
                if (messages.isEmpty()) {
                    jMessages.setVisible(false);
                } else {
                    jMessages.setVisible(true);
                    jMessages.setText(BEFUtils.getMessagesAsString(messages));
                }
            }
        };

    }

}
