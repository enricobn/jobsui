package org.jobsui.ui.swing;

import org.jobsui.core.ui.UIContainer;
import org.jobsui.core.ui.UIWidget;

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
    public void add(UIWidget<JComponent> widget) {
        addRow(widget.getLayoutComponent());
    }

    @Override
    public void add(UIContainer<JComponent> container) {
        addRow(container.getComponent());
    }

    private void addRow(JComponent component) {
        {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = rows;
            gbc.insets.right = 5;
            gbc.insets.top = 5;
            gbc.insets.left = 5;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            this.component.add(component, gbc);
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

}
