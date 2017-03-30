package org.jobsui.core.ui.javafx;

import javafx.scene.Node;
import javafx.scene.control.TextField;
import org.jobsui.core.ui.StringConverter;
import org.jobsui.core.ui.UIValue;
import rx.Observable;
import rx.Subscriber;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by enrico on 3/30/17.
 */
public class JavaFXUIValue implements UIValue<Node> {
    private final TextField component = new TextField();
    private final Observable<Serializable> observable;
    private final List<Subscriber<? super Serializable>> subscribers = new ArrayList<>();
    private StringConverter<Serializable> converter;

    public JavaFXUIValue() {
        observable = Observable.create(subscriber -> {
            subscriber.onStart();
            subscribers.add(subscriber);
        });
        component.textProperty().addListener((obs, oldValue, newValue) -> {
            notifySubscribers();
        });
    }

    @Override
    public void setConverter(StringConverter<Serializable> converter) {
        this.converter = converter;
    }

    @Override
    public void setDefaultValue(Serializable value) {
        setValue(value);
    }

    @Override
    public Observable<Serializable> getObservable() {
        return observable;
    }

    @Override
    public Node getComponent() {
        return component;
    }

    @Override
    public Serializable getValue() {
        return component.getText();
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
        if (value == null) {
            component.setText(null);
        } else {
            String text;
            if (converter != null) {
                text = converter.toString(value);
            } else {
                text = value.toString();
            }
            component.setText(text);
        }
    }

    @Override
    public void setTitle(String label) {
    }

    @Override
    public void setEnabled(boolean enable) {
        component.setDisable(!enable);
    }
}
