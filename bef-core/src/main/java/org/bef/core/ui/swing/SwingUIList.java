package org.bef.core.ui.swing;

import org.bef.core.ui.UIList;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Created by enrico on 2/24/16.
 */
public class SwingUIList<T> extends JPanel implements UIList<T> {
    private final boolean allowRemove;
    private List<T> items;


    public SwingUIList(List<T> items, boolean allowRemove) {
        this.allowRemove = allowRemove;
        setLayout(new GridBagLayout());
        setItems(new ArrayList<>(items));
    }

    @Override
    public void addItem(T item) {
        items.add(item);
        updateItems();
    }

    @Override
    public void setItems(final List<T> items) {
        this.items = items;

        updateItems();
    }

    private void updateItems() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                removeAll();
                int i = 0;
                for (final T item : items) {
                    GridBagConstraints constraints = new GridBagConstraints();
                    constraints.fill = GridBagConstraints.HORIZONTAL;
                    constraints.weightx = 1.0;
                    constraints.weighty = 0;
                    constraints.gridy = i;
                    constraints.gridx = 0;

                    add(new Item(item), constraints);
                    i++;
                }
                revalidate();
            }
        });
    }

    @Override
    public List<T> getItems() {
        return Collections.unmodifiableList(items);
    }

    class Item extends JPanel {

        Item(final T item) {
            setLayout(new GridBagLayout());

            GridBagConstraints constraints = new GridBagConstraints();
            if (allowRemove) {
                constraints.fill = GridBagConstraints.NONE;
                constraints.anchor = GridBagConstraints.WEST;
                constraints.weightx = 1.0;
                constraints.weighty = 0;
                constraints.gridx = 0;
                final JButton button = new JButton("-");
                button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        items.remove(item);
                        updateItems();
                    }
                });
                button.setPreferredSize(new Dimension(25, 20));
                button.setMargin(new Insets(2, 2, 2, 2));
                add(button, constraints);
            }

            constraints = new GridBagConstraints();
            constraints.fill = GridBagConstraints.NONE;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.weightx = 1.0;
            constraints.weighty = 0;
            constraints.gridx = allowRemove ? 1 : 0;
            constraints.insets.left = 5;

            final JLabel label = new JLabel(item.toString());
            label.setHorizontalAlignment(SwingConstants.LEFT);
            add(label, constraints);
        }
    }
}
