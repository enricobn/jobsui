package org.jobsui.core.ui.swing;

import org.jobsui.core.ui.UIList;
import rx.Observable;
import rx.Subscriber;

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
public class SwingUIList<T> implements UIList<T,JComponent> {
    private final JPanel component = new JPanel();
    private final Observable<List<T>> observable;
    private final List<Subscriber<? super List<T>>> subscribers = new ArrayList<>();
    private List<T> items;
    private boolean allowRemove = true;

    public SwingUIList() {
        component.setLayout(new GridBagLayout());
        observable = Observable.create(new Observable.OnSubscribe<List<T>>() {

            @Override
            public void call(Subscriber<? super List<T>> subscriber) {
                subscriber.onStart();
                subscribers.add(subscriber);
            }
        });
    }

    @Override
    public void addItem(T item) {
        items.add(item);
        updateItems();
    }

    @Override
    public void setValue(final List<T> items) {
        this.items = new ArrayList<>(items);
        updateItems();
    }

    @Override
    public void setTitle(String label) {

    }

    private void updateItems() {
        SwingUtilities.invokeLater(() -> {
            component.removeAll();
            int i = 0;
            for (final T item : items) {
                GridBagConstraints constraints = new GridBagConstraints();
                constraints.fill = GridBagConstraints.HORIZONTAL;
                constraints.weightx = 1.0;
                constraints.weighty = 0;
                constraints.gridy = i;
                constraints.gridx = 0;

                component.add(new Item(item), constraints);
                i++;
            }
            component.revalidate();
            notifySubscribers();
        });
    }

    @Override
    public void notifySubscribers() {
        for (Subscriber<? super List<T>> subscriber : subscribers) {
            subscriber.onNext(getValue());
        }
    }

    @Override
    public List<T> getValue() {
        return Collections.unmodifiableList(items);
    }

    @Override
    public JComponent getComponent() {
        return component;
    }

    @Override
    public void setAllowRemove(boolean allowRemove) {
        this.allowRemove = allowRemove;
    }

    @Override
    public Observable<List<T>> getObservable() {
        return observable;
    }

    private class Item extends JPanel {

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
                button.addActionListener(e -> {
                    items.remove(item);
                    updateItems();
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

    @Override
    public void setVisible(boolean visible) {
        component.setVisible(visible);
    }

    @Override
    public void setEnabled(boolean enable) {
        component.setEnabled(enable);
    }
}
