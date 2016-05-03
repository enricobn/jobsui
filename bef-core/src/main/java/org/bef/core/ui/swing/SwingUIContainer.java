package org.bef.core.ui.swing;

import org.bef.core.ui.UIComponent;
import org.bef.core.ui.UIContainer;

import javax.swing.*;
import java.awt.*;

/**
 * Created by enrico on 2/14/16.
 */
public class SwingUIContainer extends JPanel implements UIContainer<JComponent> {
//    private final Map<JComboBox, Boolean> choices = new HashMap<>();
    private int rows = 0;

    public SwingUIContainer() {
        setLayout(new GridBagLayout());
    }
//
//    @Override
//    public <T> UIChoice<T,JComponent> addChoice(String title, final T[] items) {
//        SwingUIChoice<T> choice = new SwingUIChoice<>();
//        choice.setItems(items);
//
//        addRow(title, choice.getComponent());
//
//        return choice;
//    }
//
//    @Override
//    public UIButton<JComponent> addButton(String title) {
//        SwingUIButton button = new SwingUIButton();
//        button.setText(title);
//
//        {
//            GridBagConstraints gbc = new GridBagConstraints();
//            gbc.gridy = rows;
//            add(button.getComponent(), gbc);
//        }
//
//        rows++;
//
//        return button;
//    }
//
//    @Override
//    public <T> UIList<T,JComponent> addList(List<T> items, boolean allowRemove) {
//        SwingUIList<T> list = new SwingUIList<>();
//        list.setItems(items);
//        list.setAllowRemove(allowRemove);
//
//        {
//            GridBagConstraints gbc = new GridBagConstraints();
//            gbc.gridy = rows;
//            add(list.getComponent(), gbc);
//        }
//
//        rows++;
//
//        return list;
//    }
//
//    @Override
//    public <T> UIValue<T,JComponent> add(final String title, final StringConverter<T> converter, final T defaultValue) {
//        SwingUIValue<T> uiValue = new SwingUIValue<>();
//        uiValue.setConverter(converter);
//        uiValue.setDefaultValue(defaultValue);
//
//        addRow(title, uiValue.getComponent());
//
//        return uiValue;
//    }

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
            add(component.getComponent(), gbc);
//            textField.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        }
        rows++;
    }


    void addFiller() {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.gridy = rows++;

        // filler
        add(new JLabel(), constraints);
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
//            jlabel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
            add(jlabel, gbc);
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
            add(component, gbc);
//            textField.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        }

        rows++;

    }

}
