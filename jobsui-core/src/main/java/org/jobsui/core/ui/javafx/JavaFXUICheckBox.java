package org.jobsui.core.ui.javafx;

import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import org.jobsui.core.ui.UICheckBox;
import rx.Observable;
import rx.Subscriber;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by enrico on 5/31/16.
 */
public class JavaFXUICheckBox implements UICheckBox<Node> {
    private final CheckBox component = new CheckBox();
    private final Observable<Serializable> observable;
    private final List<Subscriber<? super Serializable>> subscribers = new ArrayList<>();

    public JavaFXUICheckBox() {
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
        component.setSelected((Boolean) value);
    }

    @Override
    public void setTitle(String label) {
//        component.setText(label);
    }

    @Override
    public void setEnabled(boolean enable) {
        component.setDisable(!enable);
    }
}
