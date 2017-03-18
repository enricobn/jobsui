package org.jobsui.core.ui.swing;

import org.jobsui.core.ui.StringConverter;
import org.jobsui.core.ui.UIValue;
import rx.Observable;
import rx.Subscriber;

import javax.swing.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by enrico on 5/1/16.
 */
public class SwingUIValue implements UIValue<JComponent> {
    private final JTextField component = new JTextField();
    private final Observable<Serializable> observable;
    private final List<Subscriber<? super Serializable>> subscribers = new ArrayList<>();
    private StringConverter<Serializable> converter;
    private Serializable defaultValue;

    public SwingUIValue() {
        observable = Observable.create(subscriber -> {
            subscriber.onStart();
            component.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {

                }

                @Override
                public void focusLost(FocusEvent e) {
                    Serializable value = converter.fromString(component.getText());
                    subscriber.onNext(value);
                }
            });
//                subscriber.onNext(defaultValue);
            subscribers.add(subscriber);
        });
    }

    @Override
    public Observable<Serializable> getObservable() {
        return observable;
    }

    @Override
    public Serializable getValue() {
        return converter.fromString(component.getText());
    }

    public JComponent getComponent() {
        return component;
    }

    public void setDefaultValue(Serializable value) {
        this.defaultValue = value;
        setValue(value);
    }

    public void setConverter(StringConverter<Serializable> converter) {
        this.converter = converter;
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
        component.setText(converter.toString(value));
    }

    @Override
    public void setTitle(String label) {

    }

    @Override
    public void setEnabled(boolean enable) {
        component.setEnabled(enable);
    }
}
