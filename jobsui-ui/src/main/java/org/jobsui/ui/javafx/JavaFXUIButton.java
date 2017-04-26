package org.jobsui.ui.javafx;

import javafx.scene.Node;
import javafx.scene.control.Button;
import org.jobsui.core.ui.UIButton;
import rx.Observable;
import rx.Subscriber;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by enrico on 5/31/16.
 */
public class JavaFXUIButton implements UIButton<Node> {
    private final Button component;
    private final Observable<Serializable> observable;
    private final List<Subscriber<? super Serializable>> subscribers = new ArrayList<>();

    public JavaFXUIButton(JavaFXUI ui) {
        component = ui.createButton();
        observable = Observable.create(subscriber -> {
            subscriber.onStart();
            subscribers.add(subscriber);
        });
        component.setOnAction(event -> notifySubscribers());
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
        return null;
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
    }

    @Override
    public void setTitle(String label) {
        component.setText(label);
    }

    @Override
    public void setEnabled(boolean enable) {
        component.setDisable(!enable);
    }
}
