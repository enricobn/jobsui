package org.jobsui.core.ui.swing;

import org.jobsui.core.ui.UIList;
import rx.Observable;
import rx.Subscriber;

import javax.swing.*;
import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by enrico on 2/24/16.
 */
public class SwingUIList implements UIList<JComponent> {
    private final JPanel component = new JPanel();
    private final Observable<Serializable> observable;
    private final List<Subscriber<? super Serializable>> subscribers = new ArrayList<>();
    private ArrayList<Serializable> items;
    private boolean allowRemove = true;

    public SwingUIList() {
        component.setLayout(new GridBagLayout());
        observable = Observable.create(subscriber -> {
            subscriber.onStart();
            subscribers.add(subscriber);
        });
    }

    @Override
    public void addItem(Serializable item) {
        items.add(item);
        updateItems();
    }

    @Override
    public void setValue(final Serializable items) {
        this.items = new ArrayList<>((List<Serializable>)items);
        updateItems();
    }

    @Override
    public void setTitle(String label) {

    }

    private void updateItems() {
        SwingUtilities.invokeLater(() -> {
            component.removeAll();
            int i = 0;
            for (final Serializable item : items) {
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
        for (Subscriber<? super Serializable> subscriber : subscribers) {
            subscriber.onNext(getValue());
        }
    }

    @Override
    public Serializable getValue() {
        return new ArrayList<>(items);
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
    public Observable<Serializable> getObservable() {
        return observable;
    }

    private class Item extends JPanel {

        Item(final Serializable item) {
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
