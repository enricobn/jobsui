package org.bef.core.ui.swing;

import org.bef.core.ui.UIComponent;
import org.bef.core.ui.UIContainer;

import javax.swing.*;
import java.awt.*;

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
    public <T> void add(String title, UIComponent<T, JComponent> component) {
        addRow(title, component.getComponent());
    }

    @Override
    public <T> void add(UIComponent<T, JComponent> component) {
        {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = rows;
            gbc.insets.right = 5;
            gbc.insets.top = 5;
            gbc.insets.left = 5;
            gbc.anchor = GridBagConstraints.WEST;
//            gbc.fill = GridBagConstraints.HORIZONTAL;
            this.component.add(component.getComponent(), gbc);
        }
        rows++;
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

    private void addRow(String label, Component component) {
        {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = rows;
            gbc.insets.right = 5;
            gbc.insets.left = 5;
            gbc.insets.top = 5;
            gbc.anchor = GridBagConstraints.EAST;
            gbc.weightx = 0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            final JLabel jlabel = new JLabel(label);
            this.component.add(jlabel, gbc);
        }

        {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 1;
            gbc.gridy = rows;
            gbc.weightx = 10.0; // why 1.0 does not work ???
            gbc.insets.right = 5;
            gbc.insets.top = 5;
            gbc.insets.left = 5;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            this.component.add(component, gbc);
        }

        rows++;

    }

}
