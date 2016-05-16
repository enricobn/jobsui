package org.bef.core.ui.swing;

import org.bef.core.ui.UIChoice;
import rx.Observable;
import rx.Subscriber;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by enrico on 5/1/16.
 */
public class SwingUIChoice<T> implements UIChoice<T,JComponent> {
    private final JPanel component = new JPanel();
    private final JComboBox<T> combo = new JComboBox<>();
    private final JButton button = new JButton("...");
    private final List<Subscriber<? super T>> subscribers = new ArrayList<>();
    private final Observable<T> observable;
    private List<T> items = new ArrayList<>();
    private String title;

    public SwingUIChoice() {
        button.setMargin(new Insets(2, 2, 2, 2));
        observable = Observable.create(new Observable.OnSubscribe<T>() {
            @Override
            public void call(final Subscriber<? super T> subscriber) {
                subscriber.onStart();
                combo.addActionListener(new ActionListener() {
                    private T selectedItem = null;

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (!Objects.equals(selectedItem, combo.getSelectedItem())) {
                            selectedItem = getValue();
                            subscriber.onNext(selectedItem);
                        }
                    }
                });
                subscribers.add(subscriber);
            }
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

        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final SwingFilteredList<T> filteredList = new SwingFilteredList<>(title, items, (T) combo.getSelectedItem());
                filteredList.show();
                if (filteredList.isOk()) {
                    setValue(filteredList.getSelectedItem());
                }
            }
        });

    }

    @Override
    public Observable<T> getObservable() {
        return observable;
    }

    @Override
    public void setEnabled(boolean enable) {
        component.setEnabled(enable);
    }

    @Override
    public T getValue() {
        int selectedIndex = combo.getSelectedIndex();
        if (selectedIndex < 0) {
            return null;
        } else {
            return combo.getItemAt(selectedIndex);
        }
    }

    @Override
    public void setItems(final List<T> items) {
        if (items.equals(this.items)) {
            return;
        }
        this.items = items;
        final Object selectedItem = combo.getSelectedItem();

//        SwingUtilities.invokeLater(new Runnable() {
//            @Override
//            public void run() {

                combo.removeAllItems();

                if (items.size() > 1) {
                    combo.addItem(null);
                }

                boolean found = false;
                for (T v : items) {
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
                        combo.setSelectedItem(null);
                    }
                }
//                for (Subscriber<? super T> subscriber : subscribers) {
//                    subscriber.onNext(getValue());
//                }

//            }
//        });
    }

    @Override
    public JComponent getComponent() {
        return component;
    }

    @Override
    public void notifySubscribers() {
        for (Subscriber<? super T> subscriber : subscribers) {
            subscriber.onNext(getValue());
        }
    }

    @Override
    public void setVisible(boolean visible) {
        component.setVisible(visible);
    }

    @Override
    public void setValue(T value) {
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
