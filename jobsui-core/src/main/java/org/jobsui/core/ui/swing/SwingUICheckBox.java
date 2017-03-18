package org.jobsui.core.ui.swing;

import org.jobsui.core.ui.UICheckBox;
import rx.Observable;
import rx.Subscriber;

import javax.swing.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by enrico on 5/31/16.
 */
public class SwingUICheckBox implements UICheckBox<JComponent> {
    private final JCheckBox component = new JCheckBox();
    private final Observable<Serializable> observable;
    private final List<Subscriber<? super Serializable>> subscribers = new ArrayList<>();

    public SwingUICheckBox() {
        observable = Observable.create(subscriber -> {
            subscriber.onStart();
            component.addActionListener(e -> subscriber.onNext(getValue()));
            subscribers.add(subscriber);
        });
    }

    @Override
    public Observable<Serializable> getObservable() {
        return observable;
    }

    @Override
    public JComponent getComponent() {
        return component;
    }

    @Override
    public Boolean getValue() {
        return component.isSelected();
    }

    @Override
    public void notifySubscribers() {
        for (Subscriber<? super Boolean> subscriber : subscribers) {
            subscriber.onNext(getValue());
        }
    }

    @Override
    public void setVisible(boolean visible) {
        component.setVisible(visible);
    }

    @Override
    public void setValue(Serializable value) {
        component.setSelected((Boolean)value);
    }

    @Override
    public void setTitle(String label) {
//        component.setText(label);
    }

    @Override
    public void setEnabled(boolean enable) {
        component.setEnabled(enable);
    }
}
