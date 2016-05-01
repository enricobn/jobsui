package org.bef.core.ui.swing;

import org.bef.core.ui.StringConverter;
import org.bef.core.ui.UIValue;
import rx.Observable;
import rx.Subscriber;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * Created by enrico on 5/1/16.
 */
public class SwingUIValue<T> implements UIValue<T> {
    private final JTextField component = new JTextField();
    private final StringConverter<T> converter;
    private final Observable<T> observable;

    public SwingUIValue(final StringConverter<T> converter, final T defaultValue) {
        this.converter = converter;
        if (defaultValue != null) {
            component.setText(converter.toString(defaultValue));
        }
        observable = Observable.create(new Observable.OnSubscribe<T>() {
            @Override
            public void call(final Subscriber<? super T> subscriber) {
                subscriber.onStart();
                component.addFocusListener(new FocusListener() {
                    @Override
                    public void focusGained(FocusEvent e) {

                    }

                    @Override
                    public void focusLost(FocusEvent e) {
                        T value = converter.fromString(component.getText());
                        subscriber.onNext(value);
                    }
                });
                subscriber.onNext(defaultValue);
            }
        });
    }

    @Override
    public Observable<T> getObservable() {
        return observable;
    }

    @Override
    public T getValue() {
        return converter.fromString(component.getText());
    }

    public Component getComponent() {
        return component;
    }
}
