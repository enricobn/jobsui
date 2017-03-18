package org.jobsui.core.ui.javafx;

import javafx.scene.Node;
import javafx.scene.control.Label;
import org.jobsui.core.ui.UIExpression;
import rx.Observable;
import rx.Subscriber;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by enrico on 10/7/16.
 */
class JavaFXUIExpression implements UIExpression<Node> {
    private final List<Subscriber<? super Serializable>> subscribers = new ArrayList<>();
    private final Observable<Serializable> observable;
    private final Label component = new Label();
    private Serializable value = null;

    JavaFXUIExpression() {
        observable = Observable.create(subscriber -> {
            subscriber.onStart();
            subscribers.add(subscriber);
        });
        component.setVisible(false);
    }

    @Override
    public Observable<Serializable> getObservable() {
        return observable;
    }

    @Override
    public void setEnabled(boolean enable) {
        component.setDisable(!enable);
    }

    @Override
    public Serializable getValue() {
        return value;
    }

    @Override
    public Node getComponent() {
        return component;
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
        this.value = value;
        if (value == null) {
            component.setText(null);
        } else {
            component.setText(value.toString());
        }
        notifySubscribers();
    }

    @Override
    public void setTitle(String label) {
    }

}
