package org.bef.core.ui.swing;

import org.bef.core.ui.UIChoice;
import rx.Observable;
import rx.Subscriber;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by enrico on 5/1/16.
 */
public class SwingUIChoice<T> implements UIChoice<T,JComponent> {
    private final JComboBox<T> component = new JComboBox<>();
    private final List<Subscriber<? super T>> subscribers = new ArrayList<>();
    private final Observable<T> observable;

    public SwingUIChoice() {
        observable = Observable.create(new Observable.OnSubscribe<T>() {
            @Override
            public void call(final Subscriber<? super T> subscriber) {
                subscriber.onStart();
                component.addActionListener(new ActionListener() {
                    private T selectedItem = null;

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (!Objects.equals(selectedItem, component.getSelectedItem())) {
                            selectedItem = getValue();
                            subscriber.onNext(selectedItem);
                        }
                    }
                });
                subscribers.add(subscriber);
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
        int selectedIndex = component.getSelectedIndex();
        if (selectedIndex < 0) {
            return null;
        } else {
            return component.getItemAt(selectedIndex);
        }
    }

    @Override
    public void setItems(final List<T> items) {
        final Object selectedItem = component.getSelectedItem();

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {

                component.removeAllItems();

                if (items.size() > 1) {
                    component.addItem(null);
                }

                boolean found = false;
                for (T v : items) {
                    component.addItem(v);
                    if (Objects.equals(selectedItem, v)) {
                        found = true;
                    }
                }

                if (found) {
                    component.setSelectedItem(selectedItem);
                } else {
                    if (items.size() == 1) {
                        component.setSelectedItem(items.get(0));
                    } else {
                        component.setSelectedItem(null);
                    }
                }
                for (Subscriber<? super T> subscriber : subscribers) {
                    subscriber.onNext(getValue());
                }

            }
        });
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
}
