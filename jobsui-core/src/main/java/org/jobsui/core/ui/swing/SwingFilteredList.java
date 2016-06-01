package org.jobsui.core.ui.swing;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by enrico on 5/16/16.
 */
public class SwingFilteredList<T> {
    private final List<T> items;
    private final JDialog dialog;
    private final OKCancelHandler okCancelHandler;
//    private T selectedItem;
    private final JList<T> list;

    public SwingFilteredList(final String title, final List<T> items, final T selectedItem) {
        this.items = items;
        dialog = new JDialog((Frame) null, true);
        if (title != null) {
            dialog.setTitle(title);
        }
        dialog.getContentPane().setLayout(new GridBagLayout());

        dialog.setSize(300, 500);
        dialog.setLocationRelativeTo(null);

        final JPanel content = new JPanel();
        content.setLayout(new GridBagLayout());

        {
            final GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets.top = 5;
            gbc.insets.left = 5;
            content.add(new JLabel("Search"), gbc);
        }

        final JTextField search = new JTextField();
        {
            final GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets.top = 5;
            gbc.insets.left = 5;
            gbc.insets.right = 5;
            gbc.gridx = 1;
            gbc.weightx = 1.0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            content.add(search, gbc);
        }

        {
            final GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets.left = 5;
            gbc.insets.top = 5;
            gbc.insets.right = 5;
            gbc.insets.bottom = 5;
            gbc.gridx = 0;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            gbc.gridwidth = 2;
            gbc.fill = GridBagConstraints.BOTH;

            list = new JList<>();
            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

//            list.setModel(new AbstractListModel<T>() {
//                @Override
//                public int getSize() {
//                    return items.size();
//                }
//
//                @Override
//                public T getElementAt(int index) {
//                    return items.get(index);
//                }
//            });

            list.addListSelectionListener(new ListSelectionListener() {
                @Override
                public void valueChanged(ListSelectionEvent e) {
                    if (e.getFirstIndex() == -1) {
                        okCancelHandler.setOKButtonEnabled(false);
                    } else {
                        okCancelHandler.setOKButtonEnabled(true);
                    }
                }
            });

            list.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        okCancelHandler.triggerOK();
                    }
                }
            });

            content.add(list, gbc);
        }

        okCancelHandler = new OKCancelHandler(dialog, (JComponent) dialog.getContentPane(), content);

        list.setListData((T[]) items.toArray());

        if (selectedItem != null) {
            list.setSelectedIndex(items.indexOf(selectedItem));
        } else {
            okCancelHandler.setOKButtonEnabled(false);
        }

        search.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (search.getText().isEmpty()) {
                            list.setListData((T[]) items.toArray());
                        } else {
                            List<T> found = new ArrayList<T>();
                            for (T item : items) {
                                if (item != null && item.toString().toLowerCase().contains(search.getText().toLowerCase())) {
                                    found.add(item);
                                }
                            }
                            list.setListData((T[]) found.toArray());
                            if (found.size() == 1) {
                                list.setSelectedIndex(0);
                            }
                        }
                    }
                });
            }
        });
    }

    public void show() {
        dialog.setVisible(true);
    }

    public boolean isOk() {
        return okCancelHandler.isOk();
    }

    public T getSelectedItem() {
        return list.getSelectedValue();
    }
}
