package org.bef.core.ui.swing;

import org.bef.core.ui.StringConverter;
import org.bef.core.ui.UIValue;
import rx.Observable;
import rx.Subscriber;

import javax.swing.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * Created by enrico on 5/1/16.
 */
public class SwingUIValue<T> implements UIValue<T,JComponent> {
    private final JTextField component = new JTextField();
    private final Observable<T> observable;
    private StringConverter<T> converter;
    private T defaultValue;

    public SwingUIValue() {
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

    public JComponent getComponent() {
        return component;
    }

    public void setDefaultValue(T value) {
        this.defaultValue = value;
        component.setText(converter.toString(value));
    }

    public void setConverter(StringConverter<T> converter) {
        this.converter = converter;
    }
}
