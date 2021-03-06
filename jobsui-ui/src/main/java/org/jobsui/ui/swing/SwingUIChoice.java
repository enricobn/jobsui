package org.jobsui.ui.swing;

import org.jobsui.core.ui.UIChoice;
import rx.Observable;
import rx.Subscriber;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by enrico on 5/1/16.
 */
public class SwingUIChoice implements UIChoice<JComponent> {
    private final JPanel component = new JPanel();
    private final JComboBox<Serializable> combo = new JComboBox<>();
    private final List<Subscriber<? super Serializable>> subscribers = new ArrayList<>();
    private final Observable<Serializable> observable;
    private List<? extends Serializable> items = new ArrayList<>();
    private String title;
    private boolean disableListener = false;

    public SwingUIChoice() {
        JButton button = new JButton("...");
        button.setMargin(new Insets(2, 2, 2, 2));
        observable = Observable.create(subscriber -> {
            subscriber.onStart();
            combo.addActionListener(new ActionListener() {
                private Serializable selectedItem = null;

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!disableListener && !Objects.equals(selectedItem, getValue())) {
                        selectedItem = getValue();
                        subscriber.onNext(selectedItem);
                    }
                }
            });
            subscribers.add(subscriber);
        });

        component.setLayout(new GridBagLayout());
        {
            GridBagConstraints gc = new GridBagConstraints();
            gc.fill = GridBagConstraints.HORIZONTAL;
            gc.weightx = 1.0;
            component.add(combo, gc);
        }

        {
            GridBagConstraints gc = new GridBagConstraints();
            gc.fill = GridBagConstraints.NONE;
            gc.weightx = 0.0;
            gc.insets.left = 5;
            component.add(button, gc);
        }

        button.addActionListener(e -> {
            final SwingFilteredList<? extends Serializable> filteredList = new SwingFilteredList(title, items,
                    combo.getSelectedItem());
            filteredList.show();
            if (filteredList.isOk()) {
                setValue(filteredList.getSelectedItem());
            }
        });

    }

    @Override
    public Observable<Serializable> getObservable() {
        return observable;
    }

    @Override
    public void setEnabled(boolean enable) {
        component.setEnabled(enable);
    }

    @Override
    public Serializable getValue() {
        int selectedIndex = combo.getSelectedIndex();
        if (selectedIndex < 0) {
            return null;
        } else {
            return combo.getItemAt(selectedIndex);
        }
    }

    @Override
    public void setItems(final List<? extends Serializable> items) {
//        System.out.println("SwingUIChoice.setItems " + items);
//        if (items.equals(this.items)) {
//            return;
//        }
        this.items = items;
        final Object selectedItem = combo.getSelectedItem();

//        SwingUtilities.invokeLater(new Runnable() {
//            @Override
//            public void run() {

                disableListener = true;
                combo.removeAllItems();
                disableListener = false;

                if (items.size() > 1) {
                    combo.addItem(null);
                }

                boolean found = false;
                for (Serializable v : items) {
                    combo.addItem(v);
                    if (Objects.equals(selectedItem, v)) {
                        found = true;
                    }
                }

                if (found) {
                    combo.setSelectedItem(selectedItem);
                } else {
                    if (items.size() == 1) {
                        combo.setSelectedItem(items.get(0));
                    } else {
                        // when no items are set, I want to be sure that subscribers are notified,
                        // even if the value was already null, but I want to do it only once
                        // so I disable listener
                        disableListener = true;
                        combo.setSelectedItem(null);
                        disableListener = false;
                        notifySubscribers();
                    }
                }

//            }
//        });
    }

    @Override
    public JComponent getComponent() {
        return component;
    }

    @Override
    public void notifySubscribers() {
        for (Subscriber<? super Serializable> subscriber : subscribers) {
            subscriber.onNext(getValue());
        }
    }

    @Override
    public void setVisible(boolean visible) {
        component.setVisible(visible);
    }

    @Override
    public void setValue(Serializable value) {
        if (value != null) {
//            System.out.println("SwingUIChoice.setValue " + System.identityHashCode(value.getClass()));
//            boolean found = false;
//            for (int i = 0; i < component.getItemCount(); i++) {
//                final T item = component.getItemAt(i);
////                if (item != null) {
////                    System.out.println("SwingUIChoice.setValue item " + System.identityHashCode(item.getClass()));
////                }
//                if (Objects.equals(item, value)) {
//                    found = true;
//                    break;
//                }
//            }

            if (!items.contains(value)) {
                throw new RuntimeException("Cannot find item " + value);
            }
        }
        combo.setSelectedItem(value);
        if (!combo.isVisible()) {
            notifySubscribers();
        }
    }

    @Override
    public void setTitle(String label) {
        this.title = label;
    }

}
