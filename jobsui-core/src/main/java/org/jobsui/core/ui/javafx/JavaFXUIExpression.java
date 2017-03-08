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
class JavaFXUIExpression<T extends Serializable> implements UIExpression<T, Node> {
    private final List<Subscriber<? super T>> subscribers = new ArrayList<>();
    private final Observable<T> observable;
    private final Label component = new Label();
    private T value = null;

    JavaFXUIExpression() {
        observable = Observable.create(new Observable.OnSubscribe<T>() {
            @Override
            public void call(final Subscriber<? super T> subscriber) {
                subscriber.onStart();
                subscribers.add(subscriber);
            }
        });
        component.setVisible(false);
    }

    @Override
    public Observable<T> getObservable() {
        return observable;
    }

    @Override
    public void setEnabled(boolean enable) {
        component.setDisable(!enable);
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public Node getComponent() {
        return component;
    }


    @Override
    public void notifySubscribers() {
        for (Subscriber<? super T> subscriber : subscribers) {
            subscriber.onNext(getValue());
        }
    }

    @Override
    public void setVisible(boolean visible) {
        component.setVisible(visible);
    }

    @Override
    public void setValue(T value) {
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
